package io.quarkiverse.langchain4j.chat.frames.internal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    volatile String contextPath = null;

    @Inject
    ObjectMapper mapper;

    @Inject
    ChatFrameController chatFrameController;

    @Inject
    ChatFrameMemoryStore chatMemoryStore;

    public void resolveContextPath() {
        StringBuilder sb = new StringBuilder();
        resolveContextPath(current, sb);
        contextPath = sb.toString();
    }

    private void resolveContextPath(ChatFrameData data, StringBuilder sb) {
        if (data == null) {
            return;
        }
        resolveContextPath(data.parent(), sb);
        sb.append("/").append(data.name);

    }

    public String contextPath() {
        return contextPath;
    }

    public void setCurrent(ChatFrameData current) {
        this.current = current;
        resolveContextPath();
    }

    public ChatFrameData getCurrent() {
        return current;
    }

    @Override
    public String userMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
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
        chatMemoryStore.messages().clear();
        current = new ChatFrameData(mapper);
        current.setName(chatFrame);
        resolveContextPath();
        return this;
    }

    @Override
    public ChatFrameContext pushFrame(String chatFrame) {
        return pushFrame(chatFrame, false);
    }

    @Override
    public ChatFrameContext popFrame() {
        if (current != null) {
            scheduleWipe(); // the case where popFrame is called within a tool and chat memory is added to.
            chatMemoryStore.messages().entrySet().removeIf(entry -> entry.getKey().startsWith(contextPath));
            current = current.parent();
            resolveContextPath();
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
        next.setParent(parent);
        if (parent != null && deleteMemory) {
            scheduleWipe();
        }
        current = next;
        contextPath = contextPath + "/" + chatFrame;
        resolveContextPath();
        return this;
    }

    @Override
    public ChatFrameContext clearMemory() {
        if (current != null) {
            clearMemory(contextPath);
        }
        return this;
    }

    void clearMemory(String path) {
        chatMemoryStore.messages().entrySet().removeIf(entry -> entry.getKey().startsWith(path + "#"));
    }

    class ScheduledWipe {
        String path;
        boolean aborted = false;

        ScheduledWipe(String path) {
            this.path = path;
        }

        void wipe() {
            if (!aborted)
                clearMemory(path);
        }
    }

    List<ScheduledWipe> scheduledWipes = new ArrayList<>();

    public List<ScheduledWipe> scheduledWipes() {
        return scheduledWipes;
    }

    @Override
    public ChatFrameContext scheduleWipe() {
        synchronized (scheduledWipes) {
            ScheduledWipe wipe = scheduledWipes.stream().filter(w -> w.path.equals(contextPath)).findFirst().orElse(null);
            if (wipe == null) {
                wipe = new ScheduledWipe(contextPath);
                scheduledWipes.add(wipe);
            }
        }
        return this;
    }

    @Override
    public ChatFrameContext abortWipe() {
        synchronized (scheduledWipes) {
            ScheduledWipe wipe = scheduledWipes.stream().filter(w -> w.path.equals(contextPath)).findFirst().orElse(null);
            if (wipe != null) {
                wipe.aborted = true;
            }
        }
        return this;
    }

    @Override
    public boolean wipeScheduled() {
        synchronized (scheduledWipes) {
            return scheduledWipes.stream().anyMatch(w -> w.path.equals(contextPath) && !w.aborted);
        }
    }
}
