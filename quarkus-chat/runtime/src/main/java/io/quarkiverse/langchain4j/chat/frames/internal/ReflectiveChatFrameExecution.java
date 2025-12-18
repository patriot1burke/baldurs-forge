package io.quarkiverse.langchain4j.chat.frames.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.tool.ToolExecution;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameEvent;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameExecution;
import io.quarkiverse.langchain4j.chat.frames.EventMapper;
import io.quarkiverse.langchain4j.chat.frames.EventType;
import io.quarkiverse.langchain4j.chat.frames.FrameInject;
import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.BeanContainer;

public class ReflectiveChatFrameExecution implements ChatFrameExecution {
    private final Class<?> beanClass;
    private final Method method;
    protected volatile BeanContainer.Factory<?> factory;
    protected List<ParameterResolver> parameterResolvers = new ArrayList<>();
    protected List<Method> resultMappers = new ArrayList<>();
    EventResolver mapper;
    Class<?> resultMapperClass;

    interface ParameterResolver {
        Object resolve(ChatFrameContext context);
    }

    interface EventResolver {
        void resolve(ChatFrameContext context, Object value);
    }

    public ReflectiveChatFrameExecution(Class<?> beanClass, Method method, Class<?> defaultResultMapper) {
        this.beanClass = beanClass;
        this.method = method;

        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(UserMessage.class)) {
                parameterResolvers.add((ctx) -> ctx.userMessage());
            } else if (parameter.getType().isAssignableFrom(ChatFrameContext.class)) {
                parameterResolvers.add((ctx) -> ctx);
            } else if (parameter.isAnnotationPresent(FrameInject.class)) {
                String key = parameter.getAnnotation(FrameInject.class).value();
                String finalKey = key.isEmpty() ? parameter.getName() : key;
                parameterResolvers.add((ctx) -> ctx.getData(finalKey, parameter.getParameterizedType()));
            } else {
                // default to a @FrameInject
                String finalKey = parameter.getName();
                parameterResolvers.add((ctx) -> ctx.getData(finalKey, parameter.getParameterizedType()));
            }
        }
        EventMapper resultMapperAnnotation = method.getAnnotation(EventMapper.class);
        if (resultMapperAnnotation == null) {
            resultMapperAnnotation = beanClass.getAnnotation(EventMapper.class);
        }
        resultMapperClass = resultMapperAnnotation != null ? resultMapperAnnotation.value() : defaultResultMapper;
        resultMappers = resolveResultMappers(resultMapperClass);
        mapper = resolveResultMapper(method.getAnnotation(EventType.class), method.getGenericReturnType(),
                method.getReturnType());
    }

    private static List<Method> resolveResultMappers(Class<?> resultMapperClass) {
        if (resultMapperClass == null) {
            return Collections.EMPTY_LIST;
        }
        List<Method> result = new ArrayList<>();
        for (Method cfm : resultMapperClass.getDeclaredMethods()) {
            if (Modifier.isPublic(cfm.getModifiers())
                    && cfm.getReturnType().equals(ChatFrameEvent.class)
                    && cfm.getName().equals("from") && cfm.getParameterCount() == 1) {
                result.add(cfm);
            }
        }
        return result;
    }

    static Class<?> resolveClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return null;
    }

    private EventResolver resolveResultMapper(EventType eventType, Type type, Class<?> clazz) {
        for (Method cfm : resultMappers) {
            if (type.equals(cfm.getGenericParameterTypes()[0])) {
                if (Modifier.isStatic(cfm.getModifiers())) {
                    return (context, obj) -> {
                        try {
                            ChatFrameEvent event = (ChatFrameEvent) cfm.invoke(null, obj);
                            context.events().add(event);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e.getCause());
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    };
                } else {
                    return (context, obj) -> {
                        try {
                            ChatFrameEvent event = (ChatFrameEvent) cfm.invoke(
                                    Arc.container().instance(resultMapperClass).get(),
                                    obj);
                            context.events().add(event);
                        } catch (InvocationTargetException e) {
                            throw new RuntimeException(e.getCause());
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    };
                }
            }
        }
        if (clazz.equals(Result.class)) {
            if (type != null && type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type resultType = parameterizedType.getActualTypeArguments()[0];
                Class<?> resultClass = resolveClass(resultType);
                EventResolver resultMapper = resolveResultMapper(eventType, resultType, resultClass);
                if (resultMapper != null) {
                    return (context, obj) -> {
                        Result<?> result = (Result<?>) obj;
                        if (result.content() != null) {
                            resultMapper.resolve(context, result.content());
                        } else {
                            for (ToolExecution execution : result.toolExecutions()) {
                                if (execution.resultObject() != null) {
                                    resolveResultMapper(eventType, execution.resultObject().getClass(),
                                            execution.resultObject().getClass()).resolve(context, execution.resultObject());
                                }
                            }
                        }
                    };
                }
            }
            return (context, obj) -> {
                Result<?> result = (Result<?>) obj;
                if (result.content() != null) {
                    resolveResultMapper(eventType, result.content().getClass(), result.content().getClass())
                            .resolve(context, result.content());
                } else {
                    for (ToolExecution execution : result.toolExecutions()) {
                        if (execution.resultObject() != null) {
                            resolveResultMapper(eventType, execution.resultObject().getClass(),
                                    execution.resultObject().getClass())
                                    .resolve(context, execution.resultObject());
                        }
                    }
                }
            };
        }
        EventType eventTypeAnnotation = eventType != null ? eventType : clazz.getAnnotation(EventType.class);
        if (eventTypeAnnotation != null) {
            return (context, obj) -> context.events().add(new ChatFrameEvent(eventTypeAnnotation.value(), obj));
        }

        if (clazz.equals(String.class)) {
            return (context, obj) -> context.events().add(ChatFrameEvent.stringMessage((String) obj));
        } else {
            return (context, obj) -> context.events().add(ChatFrameEvent.objectMessage(obj));
        }
    }

    @Override
    public void chat() {
        Object instance = Arc.container().instance(beanClass).get();
        try {
            Object returnValue = null;
            ChatFrameContext context = ChatFrameRecorder.CONTAINER.beanInstance(ChatFrameContext.class);
            if (parameterResolvers.size() == 0) {
                returnValue = method.invoke(instance);
            } else {
                Object[] parameters = new Object[parameterResolvers.size()];
                for (int i = 0; i < parameterResolvers.size(); i++) {
                    parameters[i] = parameterResolvers.get(i).resolve(context);
                }
                returnValue = method.invoke(instance, parameters);
            }
            if (returnValue != null) {
                mapper.resolve(context, returnValue);
            }
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new RuntimeException(e.getCause());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
