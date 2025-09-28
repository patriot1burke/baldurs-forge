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

    Render render;

    ChatFrame defaultChatFrame;

    Map<String, ChatFrame> chatFrames = new HashMap<>();

    public void register(String name, ChatFrame chatFrame) {
        chatFrames.put(name, chatFrame);
    }

    public void setDefaultChatFrame(ChatFrame chatFrame) {
        defaultChatFrame = chatFrame;
    }

    public void setRender(Render render) {
        this.render = render;
    }

    public ChatFrame getChatFrame(String name) {
        return chatFrames.get(name);
    }

    /**
     * Sets the chat frame for the given context. Also delete the messages for the ChatContext's memoryId.
     * @param context
     * @param chatFrame
     */
    public void setChatFrame(ChatContext context, String chatFrame) {
        Log.info("Setting chat frame: " + chatFrame);
        context.setData(CHAT_FRAME, chatFrame);
    }

    /**
     * Clears the chat frame for the given context. Also delete the messages for the ChatContext's memoryId.
     * @param context
     */
    public void popChatFrame(ChatContext context) {
        Log.info("Popping chat frame");
        context.setData(CHAT_FRAME, null);
        Log.info("Deleting messages for memoryId: " + context.memoryId());
        memoryStore.deleteMessages(context.memoryId());
    }

    private ObjectMessage message(String message) {
        return new ObjectMessage(render.render(message));
    }

    public void chat(ChatContext context) {
        String chatFrame = context.getData(CHAT_FRAME, String.class);
        if (chatFrame == null) {
            Log.info("Executing default chat");
            String msg = defaultChatFrame.chat();
            if (!context.popSuppressAIResponse()) {
                if (msg != null) {
                    Log.info("Adding message: " + msg);
                    context.response().add(message(msg));
                }
            } else {
                Log.info("Suppressing AI response");
            }
        } else if (chatFrames.containsKey(chatFrame)) {
            Log.info("Executing chat frame: " + chatFrame);
            String msg = chatFrames.get(chatFrame).chat();
            if (!context.popSuppressAIResponse()) {
                if (msg != null) {
                    Log.info("Adding message: " + msg);
                    context.response().add(message(msg));
                }
            } else {
                Log.info("Suppressing AI response");
            }
        }
        else {
            Log.error("Unknown chat frame: " + chatFrame);
            popChatFrame(context);
            if (!context.popSuppressAIResponse()) {
                context.response().add(message("I'm having issues at the moment. Can you retry or rephrase your request?"));
            }
        }

    }

}
