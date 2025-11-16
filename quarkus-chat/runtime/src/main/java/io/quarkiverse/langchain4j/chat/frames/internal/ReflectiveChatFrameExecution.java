package io.quarkiverse.langchain4j.chat.frames.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.tool.ToolExecution;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameExecution;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameMessage;
import io.quarkiverse.langchain4j.chat.frames.ResultMessageTypes;
import io.quarkiverse.langchain4j.chat.frames.ObjectMessage;
import io.quarkiverse.langchain4j.chat.frames.StringMessage;
import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.logging.Log;

public class ReflectiveChatFrameExecution implements ChatFrameExecution {
    private final Class<?> beanClass;
    private final Method method;
    protected volatile BeanContainer.Factory<?> factory;
    protected List<ParameterResolver> parameterResolvers = new ArrayList<>();
    protected List<Method> responseConstructors = new ArrayList<>();

    interface ParameterResolver {
        Object resolve(ChatFrameContext context);
    }

    public ReflectiveChatFrameExecution(Class<?> beanClass, Method method) {
        this.beanClass = beanClass;
        this.method = method;

        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(UserMessage.class)) {
                parameterResolvers.add((ctx) -> ctx.userMessage());
            } else if (parameter.isAnnotationPresent(SystemMessage.class)) {
                parameterResolvers.add((ctx) -> ctx.systemMessage());
            } else if (parameter.isAnnotationPresent(MemoryId.class)) {
                parameterResolvers.add((ctx) -> ctx.memoryId());
            } else if (parameter.getType().isAssignableFrom(ChatFrameContext.class)) {
                parameterResolvers.add((ctx) -> ctx);
            } else {
                parameterResolvers.add((ctx) -> ctx.parameter(parameter.getName(), parameter.getParameterizedType()));
            }
        }
        ResultMessageTypes responseMessage = method.getAnnotation(ResultMessageTypes.class);
        if (responseMessage != null) {
            for (Class<? extends ChatFrameMessage> messageClass : responseMessage.value()) {
                for (Method cfm : messageClass.getDeclaredMethods()) {
                    if (Modifier.isPublic(cfm.getModifiers()) && Modifier.isStatic(cfm.getModifiers())
                            && cfm.getName().equals("from") && cfm.getParameterCount() == 1
                            && ChatFrameMessage.class.isAssignableFrom(cfm.getReturnType())) {
                        responseConstructors.add(cfm);
                    }
                }
            }
        }
    }

    @Override
    public void chat() {
        Object instance = Arc.container().instance(beanClass).get();
        try {
            Object returnValue = null;
            ChatFrameContext context = ChatFrameRecorder.CONTAINER.beanInstance(ChatFrameContext.class);
            if (method.getParameterCount() == 0) {
                returnValue = method.invoke(instance);
            } else {
                Object[] parameters = new Object[method.getParameters().length];
                for (int i = 0; i < method.getParameters().length; i++) {
                    parameters[i] = parameterResolvers.get(i).resolve(context);
                }
                returnValue = method.invoke(instance, parameters);
            }
            if (returnValue != null) {
                handleResponse(context, method.getGenericReturnType(), returnValue);
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleResponse(ChatFrameContext context, Type generic, Object returnValue) {
        for (Method from : responseConstructors) {
            Log.info("handleResponse: from " + method.toString());
            Log.info("handleResponse: generic " + generic.getTypeName());
            if (from.getGenericParameterTypes()[0].equals(generic)) {
                try {
                    context.response().add((ChatFrameMessage) from.invoke(null, returnValue));
                    return;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (returnValue instanceof Result) {
            ParameterizedType parameterizedType = (ParameterizedType) generic;
            Result<?> result = (Result<?>) returnValue;
            if (result.content() != null) {
                Type resultType = parameterizedType.getActualTypeArguments()[0];
                handleResponse(context, resultType, result.content());
            } else {
                for (ToolExecution execution : result.toolExecutions()) {
                    if (execution.resultObject() != null) {
                        handleResponse(context, execution.resultObject().getClass(), execution.resultObject());
                    }
                }
            }
            return;
        } else if (returnValue instanceof String) {
            context.response().add(new StringMessage((String) returnValue));
        } else {
            context.response().add(new ObjectMessage(returnValue));
        }
    }

}
