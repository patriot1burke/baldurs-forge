package org.baldurs.forge.builder;

import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.memory.ChatMemoryService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class JsonChatMemory implements ChatMemory {
    List<ChatMessage> messages = new ArrayList<>();

    @Override
    public void add(ChatMessage message) {
        //Log.info("Adding message for user: " + user);
        //Log.info("Message: " + message);
        messages.add(message);
    }

    @Override
    public void clear() {
        //Log.info("Clearing messages for user: " + user);
        messages.clear();
    }

    @Override
    public Object id() {
        return ChatMemoryService.DEFAULT;
    }

    public void load(String chatHistory) {
        if (chatHistory != null) {
            chatHistory = chatHistory.trim();
            if (!chatHistory.isEmpty()) {
                messages = ChatMessageDeserializer.messagesFromJson(chatHistory);
            }
        }
    }

    public String toJson() {
        return ChatMessageSerializer.messagesToJson(messages);
    }

    @Override
    public List<ChatMessage> messages() {
        //Log.info("Getting messages for user: " + user);
        //for (ChatMessage message : messages) {
            //Log.info("Message: " + message);
        //}
        return messages;
    }
    

}
