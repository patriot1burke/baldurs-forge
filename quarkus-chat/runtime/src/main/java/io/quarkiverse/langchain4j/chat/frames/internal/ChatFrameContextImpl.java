package io.quarkiverse.langchain4j.chat.frames.internal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameController;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameExecution;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameMessage;

@RequestScoped
@Default
public class ChatFrameContextImpl implements ChatFrameContext {

    volatile ChatFrameData current;
    List<ChatFrameMessage> response = new ArrayList<>();

    String userMessage = null;
    String systemMessage = null;

    boolean wipeScheduled = false;
    boolean wipeAborted = false;

    @Inject
    ObjectMapper mapper;

    @Inject
    ChatFrameController chatFrameController;

    @Inject
    ChatMemoryStore chatMemoryStore;

    public void setCurrent(ChatFrameData current) {
        this.current = current;
    }

    public ChatFrameData getCurrent() {
        return current;
    }

    @Override
    public String userMessage() {
        return userMessage;
    }

    @Override
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public String systemMessage() {
        return systemMessage;
    }

    @Override
    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
    }

    @Override
    public String memoryId() {
        return current.memoryId();
    }

    @Override
    public Map<String, Object> data() {
        return current.data();
    }

    @Override
    public <T> T getData(String key, Type type) {
        return current.getData(key, type);
    }

    @Override
    public void setData(String key, Object value) {
        current.setData(key, value);
    }

    @Override
    public void removeData(String key) {
        current.data().remove(key);
    }

    /**
     * Arbitrary list of response objects serialized to JSON and sent back to client.
     *
     * @return
     */
    @Override
    public List<ChatFrameMessage> response() {
        return response;
    }

    // ChatFrameController methods

    @Override
    public String currentFrameId() {
        return current != null ? current.name() : null;
    }

    @Override
    public ChatFrameExecution currentFrame() {
        if (current == null) {
            return null;
        }
        return chatFrameController.getFrame(currentFrameId());
    }

    @Override
    public void setFrame(String chatFrame) {
        if (!chatFrameController.hasFrame(chatFrame)) {
            throw new IllegalArgumentException("Unknown chat frame: " + chatFrame);
        }
        current = new ChatFrameData(mapper);
        current.setName(chatFrame);
        current.setMemoryId(UUID.randomUUID().toString());
    }

    @Override
    public void pushFrame(String chatFrame) {
        pushFrame(chatFrame, false);
    }

    @Override
    public void popFrame() {
        if (current != null) {
            chatMemoryStore.deleteMessages(current.memoryId());
            current = current.parent();
        }
    }

    @Override
    public void pushFrame(String chatFrame, boolean deleteMemory) {
        if (!chatFrameController.hasFrame(chatFrame)) {
            throw new IllegalArgumentException("Unknown chat frame: " + chatFrame);
        }
        ChatFrameData parent = current;
        ChatFrameData next = new ChatFrameData(mapper);
        next.setName(chatFrame);
        next.setMemoryId(UUID.randomUUID().toString());
        next.setParent(parent);
        if (parent != null && deleteMemory) {
            chatMemoryStore.deleteMessages(parent.memoryId());
        }
        current = next;
    }

    @Override
    public void clearMemory() {
        if (current != null) {
            chatMemoryStore.deleteMessages(current.memoryId());
        }
    }

    @Override
    public void scheduleWipe() {
        wipeScheduled = true;
    }

    @Override
    public boolean wipeScheduled() {
        return wipeScheduled && !wipeAborted;
    }

    @Override
    public void abortWipe() {
        wipeScheduled = false;
        wipeAborted = true;
    }
}
