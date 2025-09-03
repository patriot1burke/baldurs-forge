package org.baldurs.forge.chat;

import java.util.HashMap;
import java.util.Map;

import org.baldurs.forge.builder.BodyArmorBuilder;
import org.baldurs.forge.builder.ModPackager;
import org.baldurs.forge.builder.WeaponBuilder;
import org.baldurs.forge.chat.actions.MessageAction;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.context.ClientMemoryStore;

import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ChatService {
    public static final String CHAT_FRAME = "chatFrame";

    @Inject
    MainMenuChat chat;

    @Inject
    ClientMemoryStore memoryStore;

    @Inject
    BodyArmorBuilder bodyArmorBuilder;

    @Inject
    WeaponBuilder weaponBuilder;

    @Inject
    ModPackager modPackager;

    @Inject
    RenderService render;

    Map<String, ChatFrame> chatFrames = new HashMap<>();

    @PostConstruct
    public void init() {
        chatFrames.put(BodyArmorBuilder.class.getName(), bodyArmorBuilder);
        chatFrames.put(WeaponBuilder.class.getName(), weaponBuilder);
        chatFrames.put(ModPackager.class.getName(), modPackager);
    }

    public void setChatFrame(ChatContext context, Class<? extends ChatFrame> chatFrame) {
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
            String msg = chat.chat(context.memoryId(), context.userMessage());
            if (!context.popIgnoreAIResponse()) {
                context.response().add(message(msg));
            }
        } else if (chatFrames.containsKey(chatFrame)) {
            Log.info("Executing chat frame: " + chatFrame);
            String msg = chatFrames.get(chatFrame).chat(context.memoryId(), context.userMessage());
            if (!context.popIgnoreAIResponse()) {
                context.response().add(message(msg));
            }
        }
        else {
            Log.error("Unknown chat frame: " + chatFrame);
            popChatFrame(context);
            if (!context.popIgnoreAIResponse()) {
                context.response().add(message("I'm having issues at the moment. Can you retry or rephrase your request?"));
            }
        }

    }

}
