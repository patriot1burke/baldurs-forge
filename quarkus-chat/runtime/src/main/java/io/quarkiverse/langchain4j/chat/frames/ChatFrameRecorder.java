package io.quarkiverse.langchain4j.chat.frames;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ChatFrameRecorder {

    public static ConcurrentHashMap<String, ChatFrameExecution> chatFrames = new ConcurrentHashMap<>();
    public static ChatFrameExecution defaultChatFrame = null;
    public static volatile BeanContainer CONTAINER = null;

    public void setContainer(BeanContainer container) {
        CONTAINER = container;
    }

    public void registerChatFrame(String frameName, ChatFrameExecution chatFrame, boolean isDefault) {
        chatFrames.put(frameName, chatFrame);
        if (isDefault) {
            defaultChatFrame = chatFrame;
        }
    }

    public void registerChatFrame(String frameName, Class<?> targetClass, String methodName, boolean isDefault) {
        try {
            ChatFrameExecution chatFrameExecution = new ReflectiveChatFrameExecution(targetClass, targetClass.getMethod(methodName));
            chatFrames.put(frameName, chatFrameExecution);
            if (isDefault) {
                defaultChatFrame = chatFrameExecution;
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
