package io.quarkiverse.langchain4j.chat.frames.client;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ChatFrameClient {

    Client chatClient;
    String endpoint;
    ObjectMapper mapper;
    WebTarget target;
    String username;
    String password;
    String bearerToken;
    Map<String, Type> eventMappings = new ConcurrentHashMap<>();

    public ChatFrameClient basicAuth(String username, String password) {
        this.username = username;
        this.password = password;
        bearerToken = null;
        return this;
    }

    public ChatFrameClient bearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
        username = password = null;
        return this;
    }

    public ChatFrameClient(ObjectMapper mapper, Client chatClient, String endpoint) {
        this.mapper = mapper;
        eventMappings.put("StringMessage", String.class);
        this.chatClient = chatClient;
        this.endpoint = endpoint;
        this.target = chatClient.target(endpoint);
    }

    public ChatFrameClient(ObjectMapper mapper, String endpoint) {
        this(mapper, ClientBuilder.newClient(), endpoint);
    }

    public ChatFrameClient(String endpoint) {
        this(new ObjectMapper(), ClientBuilder.newClient(), endpoint);
    }

    public void registerEventMapping(String eventType, Type type) {
        eventMappings.put(eventType, type);
    }

    public ChatFrameClientSession session(String frame) {
        ChatFrameClientSession session = new ChatFrameClientSession(mapper, target, frame);
        session.username = username;
        session.password = password;
        session.bearerToken = bearerToken;
        return session;
    }

    public ChatFrameClientSession session() {
        return session(null);
    }
}
