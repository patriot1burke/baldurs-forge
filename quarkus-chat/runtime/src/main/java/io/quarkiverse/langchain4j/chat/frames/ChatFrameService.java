package io.quarkiverse.langchain4j.chat.frames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ChatFrameService implements ChatFrameController {
    public static final String CHAT_FRAME = "chatFrame";

    @Inject
    ClientMemoryStore memoryStore;

    @Inject
    ChatContext context;

    @Override
    public void register(String name, ChatFrameExecution chatFrame) {
        ChatFrameRecorder.chatFrames.put(name, chatFrame);
    }

    @Override
    public void setDefaultFrame(String chatFrame) {
        ChatFrameRecorder.defaultChatFrame = chatFrame;
    }

    @Override
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

    @Override
    public ChatFrameExecution currentFrame() {
        String frameId = currentFrameId();
        if (frameId == null) {
            if (ChatFrameRecorder.defaultChatFrame == null) {
                return null;
            }
            return ChatFrameRecorder.chatFrames.get(ChatFrameRecorder.defaultChatFrame);
        }
        return ChatFrameRecorder.chatFrames.get(frameId);
    }

    @Override
    public ChatFrameExecution getFrame(String name) {
        return ChatFrameRecorder.chatFrames.get(name);
    }

    /**
     * Clears the stack and sets the chat frame for the given context.
     * @param chatFrame
     */
    @Override
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
    @Override
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
    @Override
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

    @Override
    public void chat() {
        String chatFrame = currentFrameId();
        if (chatFrame == null) {
            if (ChatFrameRecorder.defaultChatFrame == null) {
                Log.error("Current frame not set and no default chat frame found");
                context.response().add(new ObjectMessage("I'm having issues at the moment. Can you retry or rephrase your request?"));
                return;
            }
            Log.info("Executing default chat");
            pushFrame(ChatFrameRecorder.defaultChatFrame);
            ChatFrameRecorder.chatFrames.get(ChatFrameRecorder.defaultChatFrame).chat();
        } else if (ChatFrameRecorder.chatFrames.containsKey(chatFrame)) {
            Log.info("Executing chat frame: " + chatFrame);
            ChatFrameRecorder.chatFrames.get(chatFrame).chat();
        } else {
            Log.error("Unknown chat frame: " + chatFrame);
            popFrame();
            context.response().add(new ObjectMessage("I'm having issues at the moment. Can you retry or rephrase your request?"));
        }

    }

}
