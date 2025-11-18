package io.quarkiverse.langchain4j.chat.frames.internal;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameController;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameExecution;
import io.quarkiverse.langchain4j.chat.frames.StringMessage;
import io.quarkus.logging.Log;

@ApplicationScoped
public class ChatFrameControllerService implements ChatFrameController {
    public static final String CHAT_FRAME = "chatFrame";

    @Override
    public void register(String name, ChatFrameExecution chatFrame) {
        ChatFrameRecorder.chatFrames.put(name, chatFrame);
    }

    @Override
    public void setDefaultFrame(String chatFrame) {
        if (!ChatFrameRecorder.chatFrames.containsKey(chatFrame)) {
            throw new IllegalArgumentException("Unknown chat frame: " + chatFrame);
        }
        ChatFrameRecorder.defaultChatFrame = chatFrame;
    }

    @Override
    public ChatFrameExecution getFrame(String name) {
        return ChatFrameRecorder.chatFrames.get(name);
    }

    @Override
    public boolean hasFrame(String name) {
        return ChatFrameRecorder.chatFrames.containsKey(name);
    }

    @Override
    public void chat(ChatFrameContext context) {
        String chatFrame = context.currentFrameId();
        if (chatFrame == null && ChatFrameRecorder.defaultChatFrame != null) {
            chatFrame = ChatFrameRecorder.defaultChatFrame;
            context.setFrame(chatFrame);
        }
        if (chatFrame == null) {
            Log.error("Current frame not set and no default chat frame found");
            context.events()
                    .add(new StringMessage("I'm having issues at the moment. Can you retry or rephrase your request?"));
            return;
        } else if (ChatFrameRecorder.chatFrames.containsKey(chatFrame)) {
            Log.info("Executing chat frame: " + chatFrame);
            ChatFrameRecorder.chatFrames.get(chatFrame).chat();
        } else {
            Log.error("Unknown chat frame: " + chatFrame);
            throw new IllegalArgumentException("Unknown chat frame: " + chatFrame);
        }
    }

}
