package io.quarkiverse.langchain4j.chat.frames.internal;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameExecution;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class ChatFrameRecorder {

    public static ConcurrentHashMap<String, ChatFrameExecution> chatFrames = new ConcurrentHashMap<>();
    public static String defaultChatFrame = null;
    public static volatile BeanContainer CONTAINER = null;
    public static String rootPath = null;

    public void setRootPath(String rootPath) {
        ChatFrameRecorder.rootPath = rootPath;
    }

    public void setContainer(BeanContainer container) {
        CONTAINER = container;
    }

    public void registerChatFrame(String frameName, ChatFrameExecution chatFrame, boolean isDefault) {
        chatFrames.put(frameName, chatFrame);
        if (isDefault) {
            defaultChatFrame = frameName;
        }
    }

    public static Class<?> resultMapperClass = null;

    public void registerResultMapper(Class<?> resultMapperClass) {
        ChatFrameRecorder.resultMapperClass = resultMapperClass;
    }

    public void registerChatFrame(String frameName, Class<?> targetClass, String methodName, boolean isDefault) {
        ChatFrameExecution chatFrameExecution = new ReflectiveChatFrameExecution(targetClass,
                resolveMethod(targetClass, methodName), resultMapperClass);
        chatFrames.put(frameName, chatFrameExecution);
        if (isDefault) {
            defaultChatFrame = frameName;
        }
    }

    protected Method resolveMethod(Class<?> targetClass, String methodName) {
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    public Consumer<Route> routeFunction(Handler<RoutingContext> bodyHandler) {
        return new Consumer<Route>() {
            @Override
            public void accept(Route route) {
                route.handler(bodyHandler);
            }
        };
    }

}
