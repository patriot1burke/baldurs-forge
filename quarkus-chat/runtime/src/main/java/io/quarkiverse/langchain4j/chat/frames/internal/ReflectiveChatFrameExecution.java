package io.quarkiverse.langchain4j.chat.frames.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameExecution;
import io.quarkus.arc.runtime.BeanContainer;

public class ReflectiveChatFrameExecution implements ChatFrameExecution {
    private final Class<?> beanClass;
    private final Method method;
    protected volatile BeanContainer.Factory<?> factory;

    public ReflectiveChatFrameExecution(Class<?> beanClass, Method method) {
        this.beanClass = beanClass;
        this.method = method;
    }

    @Override
    public void chat() {
        if (factory == null) {
            factory = ChatFrameRecorder.CONTAINER.beanInstanceFactory(beanClass);
        }
        Object instance = factory.create().get();
        try {
            method.invoke(instance);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
