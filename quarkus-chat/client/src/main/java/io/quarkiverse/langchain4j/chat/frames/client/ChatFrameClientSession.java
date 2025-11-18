package io.quarkiverse.langchain4j.chat.frames.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
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

    String username;
    String password;
    String bearerToken;

    protected ChatFrameClientSession(ObjectMapper mapper, WebTarget target, String frame) {
        this.mapper = mapper;
        this.target = target;
        if (frame != null) {
            context().frame().name(frame);
            context.frame.setMapper(mapper);
        }
    }

    private void setAuthentication(Builder requestBuilder) {
        if (username != null && password != null) {
            requestBuilder.header("Authorization", "Basic "
                    + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)));
        } else if (bearerToken != null) {
            requestBuilder.header("Authorization", "Bearer " + bearerToken);
        }
    }

    public ChatFrameClientSession basicAuth(String username, String password) {
        this.username = username;
        this.password = password;
        bearerToken = null;
        return this;
    }

    public ChatFrameClientSession bearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
        username = password = null;
        return this;
    }

    public List<ClientChatEvent> chat(String userMessage) {
        this.userMessage = userMessage;
        String json = null;
        try {
            json = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Builder requestBuilder = target.request();
        setAuthentication(requestBuilder);
        json = requestBuilder.post(Entity.json(json), String.class);
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
