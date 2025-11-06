package io.quarkiverse.langchain4j.chat.frames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import io.quarkiverse.langchain4j.chat.context.ChatContext;
import io.quarkiverse.langchain4j.chat.context.ClientMemoryStore;
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

    public String currentFrameId() {
        List<String> frameStack = getFrameStack();
        if (frameStack == null || frameStack.isEmpty()) {
            return null;
        }
        return frameStack.get(frameStack.size() - 1);
    }

    protected List<String> getFrameStack() {
        return context.getData(CHAT_FRAME, new TypeReference<List<String>>() {});
    }

    public ChatFrame currentFrame() {
        String frameId = currentFrameId();
        if (frameId == null) {
            return defaultChatFrame;
        }
        return chatFrames.get(frameId);
    }

    public ChatFrame getFrame(String name) {
        return chatFrames.get(name);
    }

    /**
     * Clears the stack and sets the chat frame for the given context.
     * @param chatFrame
     */
    public void setFrame(String chatFrame) {
        Log.info("Setting chat frame: " + chatFrame);
        List<String> frameStack = new ArrayList<>();
        frameStack.add(chatFrame);
        context.setData(CHAT_FRAME, frameStack);
    }

    /**
     * Pushes the given chat frame onto the frame stack.
     * 
     * @param chatFrame
     */
    public void pushFrame(String chatFrame) {
        List<String> frameStack = getFrameStack();
        if (frameStack == null) {
            frameStack = new ArrayList<>();
        }
        frameStack.add(chatFrame);
        context.setData(CHAT_FRAME, frameStack);
    }

    /**
     * Pops current frame off of the frame stack and also deletes the messages for the ChatContext's memoryId.
     */
    public void popFrame() {
        Log.info("Popping chat frame");
        List<String> frameStack = getFrameStack();
        if (frameStack != null && !frameStack.isEmpty()) {
            frameStack.remove(frameStack.size() - 1);
            if (frameStack.isEmpty()) {
                context.setData(CHAT_FRAME, null);
            } else {
                context.setData(CHAT_FRAME, frameStack);
            }
        }
        Log.info("Deleting messages for memoryId: " + context.memoryId());
        memoryStore.deleteMessages(context.memoryId());
    }

    public void chat(ChatContext context) {
        String chatFrame = currentFrameId();
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
