package io.quarkiverse.langchain4j.chat.frames.internal;

import java.util.concurrent.ConcurrentHashMap;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameExecution;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ChatFrameRecorder {

    public static ConcurrentHashMap<String, ChatFrameExecution> chatFrames = new ConcurrentHashMap<>();
    public static String defaultChatFrame = null;
    public static volatile BeanContainer CONTAINER = null;

    public void setContainer(BeanContainer container) {
        CONTAINER = container;
    }

    public void registerChatFrame(String frameName, ChatFrameExecution chatFrame, boolean isDefault) {
        chatFrames.put(frameName, chatFrame);
        if (isDefault) {
            defaultChatFrame = frameName;
        }
    }

    public void registerChatFrame(String frameName, Class<?> targetClass, String methodName, boolean isDefault) {
        try {
            ChatFrameExecution chatFrameExecution = new ReflectiveChatFrameExecution(targetClass,
                    targetClass.getMethod(methodName));
            chatFrames.put(frameName, chatFrameExecution);
            if (isDefault) {
                defaultChatFrame = frameName;
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
