package io.quarkiverse.langchain4j.chat.frames.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

public class ChatFrameClient {

    Client chatClient;
    String endpoint;
    ObjectMapper mapper;
    WebTarget target;

    public ChatFrameClient(Client chatClient, String endpoint) {
        this.mapper = new ObjectMapper();
        mapper.registerSubtypes(new NamedType(ClientTextMessage.class, "Text"));
        this.chatClient = chatClient;
        this.endpoint = endpoint;
        this.target = chatClient.target(endpoint);
    }

    public ChatFrameClient(String endpoint) {
        this(ClientBuilder.newClient(), endpoint);
    }

    public void registerMessageType(Class<? extends ClientChatFrameMessage> messageType, String name) {
        mapper.registerSubtypes(new NamedType(messageType, name));
    }

    public ChatFrameResponse chat(String defaultFrame, ChatFrameRequest request) {
        try {
            String json = mapper.writeValueAsString(request);
            return target.queryParam("defaultFrame", defaultFrame).request().post(Entity.json(json), ChatFrameResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ChatFrameResponse chat(ChatFrameRequest request) {
        try {
            String json = mapper.writeValueAsString(request);
            json = target.request().post(Entity.json(json), String.class);
            return mapper.readValue(json, ChatFrameResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
