package org.baldurs.forge.chat;

import java.util.HashMap;
import java.util.Map;

import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.context.ClientMemoryStore;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ChatFrameService {
    public static final String CHAT_FRAME = "chatFrame";

    @Inject
    ClientMemoryStore memoryStore;

    @Inject
    ChatContext context;

    ChatFrame defaultChatFrame;

    Map<String, ChatFrame> chatFrames = new HashMap<>();

    public void register(String name, ChatFrame chatFrame) {
        chatFrames.put(name, chatFrame);
    }

    public void setDefaultFrame(ChatFrame chatFrame) {
        defaultChatFrame = chatFrame;
    }

    public String currrentFrameId() {
        return context.getData(CHAT_FRAME, String.class);
    }

    public ChatFrame currentFrame() {
        String frameId = currrentFrameId();
        if (frameId == null) {
            return defaultChatFrame;
        }
        return chatFrames.get(frameId);
    }

    public ChatFrame getFrame(String name) {
        return chatFrames.get(name);
    }

    /**
     * Sets the chat frame for the given context.
     * @param chatFrame
     */
    public void setFrame(String chatFrame) {
        Log.info("Setting chat frame: " + chatFrame);
        context.setData(CHAT_FRAME, chatFrame);
    }

    /**
     * Clears the chat frame for the given context. Also delete the messages for the ChatContext's memoryId.
     */
    public void popFrame() {
        Log.info("Popping chat frame");
        context.setData(CHAT_FRAME, null);
        Log.info("Deleting messages for memoryId: " + context.memoryId());
        memoryStore.deleteMessages(context.memoryId());
    }

    public void chat(ChatContext context) {
        String chatFrame = context.getData(CHAT_FRAME, String.class);
        if (chatFrame == null) {
            Log.info("Executing default chat");
            defaultChatFrame.chat();
        } else if (chatFrames.containsKey(chatFrame)) {
            Log.info("Executing chat frame: " + chatFrame);
            chatFrames.get(chatFrame).chat();
        }
        else {
            Log.error("Unknown chat frame: " + chatFrame);
            popFrame();
            context.response().add(new ObjectMessage("I'm having issues at the moment. Can you retry or rephrase your request?"));
        }

    }

}
