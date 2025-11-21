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
import io.quarkiverse.langchain4j.chat.frames.ChatFrameEvent;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameExecution;

@RequestScoped
@Default
public class ChatFrameContextImpl implements ChatFrameContext {

    volatile ChatFrameData current;
    List<ChatFrameEvent> events = new ArrayList<>();

    String userMessage = null;

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
    public ChatFrameContext setData(String key, Object value) {
        current.setData(key, value);
        return this;
    }

    @Override
    public ChatFrameContext removeData(String key) {
        current.data().remove(key);
        return this;
    }

    @Override
    public List<ChatFrameEvent> events() {
        return events;
    }

    @Override
    public ChatFrameContext addEvent(String type, Object value) {
        return addEvent(type, value, false);
    }

    @Override
    public ChatFrameContext addEvent(String type, Object value, boolean replace) {
        if (replace) {
            events.removeIf(event -> event.getType().equals(type));
        }
        events.add(new ChatFrameEvent(type, value));
        return this;
    }

    @Override
    public ChatFrameContext addEvent(String stringMessage) {
        events.add(ChatFrameEvent.stringMessage(stringMessage));
        return this;
    }

    @Override
    public ChatFrameContext addEvent(Object objectMessage) {
        events.add(ChatFrameEvent.objectMessage(objectMessage));
        return this;
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
    public ChatFrameContext setFrame(String chatFrame) {
        if (!chatFrameController.hasFrame(chatFrame)) {
            throw new IllegalArgumentException("Unknown chat frame: " + chatFrame);
        }
        current = new ChatFrameData(mapper);
        current.setName(chatFrame);
        current.setMemoryId(UUID.randomUUID().toString());
        return this;
    }

    @Override
    public ChatFrameContext pushFrame(String chatFrame) {
        return pushFrame(chatFrame, false);
    }

    @Override
    public ChatFrameContext popFrame() {
        if (current != null) {
            chatMemoryStore.deleteMessages(current.memoryId());
            current = current.parent();
        }
        return this;
    }

    @Override
    public ChatFrameContext pushFrame(String chatFrame, boolean deleteMemory) {
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
        return this;
    }

    @Override
    public ChatFrameContext clearMemory() {
        if (current != null) {
            chatMemoryStore.deleteMessages(current.memoryId());
        }
        return this;
    }

    @Override
    public ChatFrameContext scheduleWipe() {
        wipeScheduled = true;
        return this;
    }

    @Override
    public boolean wipeScheduled() {
        return wipeScheduled && !wipeAborted;
    }

    @Override
    public ChatFrameContext abortWipe() {
        wipeScheduled = false;
        wipeAborted = true;
        return this;
    }
}
