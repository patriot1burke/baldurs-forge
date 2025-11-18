package io.quarkiverse.langchain4j.chat.frames.client;

import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ChatFrameClientSession {
    ClientChatFrameContext context;
    String userMessage = null;

    @JsonIgnore
    ObjectMapper mapper;

    @JsonIgnore
    WebTarget target;

    protected ChatFrameClientSession(ObjectMapper mapper, WebTarget target, String frame) {
        this.mapper = mapper;
        this.target = target;
        if (frame != null) {
            context().frame().name(frame);
            context.frame.setMapper(mapper);
        }
    }

    public List<ClientChatEvent> chat(String userMessage) {
        this.userMessage = userMessage;
        String json = null;
        try {
            json = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        json = target.request().post(Entity.json(json), String.class);
        try {
            ChatFrameResponse response = mapper.readValue(json, ChatFrameResponse.class);
            context = response.context;
            if (context.frame != null) {
                context.frame.setMapper(mapper);
            }
            return response.events != null ? response.events : Collections.EMPTY_LIST;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String userMessage() {
        return userMessage;
    }

    public ClientChatFrameContext context() {
        if (context == null) {
            context = new ClientChatFrameContext();
        }
        return context;
    }
}
