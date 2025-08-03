package org.baldurs.forge.chat;

import java.util.HashMap;
import java.util.Map;

import org.baldurs.forge.builder.BoostBuilderChat;
import org.baldurs.forge.builder.EquipmentBuilder;
import org.baldurs.forge.builder.EquipmentBuilderAgent;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.context.ClientMemoryStore;

import dev.langchain4j.model.chat.ChatModel;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ChatService {
    public static final String CHAT_FRAME = "chatFrame";

    @Inject
    BaldursForgeChat chat;

    @Inject
    ClientMemoryStore memoryStore;

    @Inject
    EquipmentBuilder equipmentBuilder;

    @Inject
    RenderService render;

    Map<String, BaldursChat> chatFrames = new HashMap<>();

    @PostConstruct
    public void init() {
        chatFrames.put(EquipmentBuilder.class.getName(), equipmentBuilder);
    }

    public void setChatFrame(ChatContext context, Class<? extends BaldursChat> chatFrame) {
        Log.info("Setting chat frame: " + chatFrame.getName());
        context.setShared(CHAT_FRAME, chatFrame.getName());
    }

    public void popChatFrame(ChatContext context) {
        Log.info("Popping chat frame");
        context.setShared(CHAT_FRAME, null);
        Log.info("Deleting messages for memoryId: " + context.memoryId());
        memoryStore.deleteMessages(context.memoryId());
    }

    private MessageAction message(String message) {
        return new MessageAction(render.markdownToHtml(message));
    }

    public void chat(ChatContext context) {
        String chatFrame = context.getShared(CHAT_FRAME, String.class);
        if (chatFrame == null) {
            Log.info("Executing default chat");
            context.response().add(message(chat.chat(context.memoryId(), context.userMessage())));
        } else if (chatFrames.containsKey(chatFrame)) {
            Log.info("Executing chat frame: " + chatFrame);
            context.response().add(message(chatFrames.get(chatFrame).chat(context.memoryId(), context.userMessage())));
        }
        else {
            Log.error("Unknown chat frame: " + chatFrame);
            context.response().add(new MessageAction("I'm having issues at the moment. Can you retry or rephrase your request?"));
        }

    }

}
