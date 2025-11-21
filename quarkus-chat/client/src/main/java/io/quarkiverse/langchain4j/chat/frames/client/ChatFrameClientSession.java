package io.quarkiverse.langchain4j.chat.frames.client;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChatFrameClientSession {
    ClientChatFrameContext context;
    ObjectMapper mapper;
    WebTarget target;

    String username;
    String password;
    String bearerToken;

    record FrameRequest(String userMessage, ClientChatFrameContext context) {
    }

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

    record Event(String type, JsonNode value) {
    }

    record FrameResponse(ClientChatFrameContext context, List<Event> events) {
    }

    public List<ClientChatEvent> chat(String userMessage) {
        FrameRequest request = new FrameRequest(userMessage, context);
        String json = null;
        try {
            json = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Builder requestBuilder = target.request();
        setAuthentication(requestBuilder);
        json = requestBuilder.post(Entity.json(json), String.class);
        try {
            System.out.println("*****Client response json: " + json);
            FrameResponse response = mapper.readValue(json, FrameResponse.class);
            context = response.context;
            if (context.frame != null) {
                context.frame.setMapper(mapper);
            }
            List<ClientChatEvent> events = new ArrayList<>();
            for (Event event : response.events) {
                System.out.println("*****Client event: '" + event.type + "' " + event.value);
                ClientChatEvent clientEvent = new ClientChatEvent() {
                    @Override
                    public String type() {
                        return event.type;
                    }

                    @Override
                    public <T> T value(Class<T> type) {
                        try {
                            return mapper.treeToValue(event.value, type);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public <T> T value(Type type) {
                        try {
                            return mapper.treeToValue(event.value, mapper.constructType(type));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                events.add(clientEvent);
            }
            return events;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ClientChatFrameContext context() {
        if (context == null) {
            context = new ClientChatFrameContext();
        }
        return context;
    }
}
