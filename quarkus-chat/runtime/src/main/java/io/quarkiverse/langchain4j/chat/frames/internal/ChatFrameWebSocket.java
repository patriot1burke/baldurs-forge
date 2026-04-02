package io.quarkiverse.langchain4j.chat.frames.internal;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameController;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameEvent;
import io.quarkus.logging.Log;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.PathParam;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;

@WebSocket(path = "/chat-frames/{defaultChatFrame}")
public class ChatFrameWebSocket {

    @Inject
    WebSocketConnection connection;

    @Inject
    ChatFrameController chatFrameService;

    @Inject
    ChatFrameMemoryStore memoryStore;

    @Inject
    ChatFrameContextImpl context;

    @Inject
    ObjectMapper mapper;

    static ChatFrameEvent badRequest(String message) {
        return new ChatFrameEvent("400", message);
    }

    static ChatFrameEvent unauthorized(String message) {
        return new ChatFrameEvent("401", message);
    }

    static ChatFrameEvent forbidden(String message) {
        return new ChatFrameEvent("403", message);
    }

    static ChatFrameEvent notFound(String message) {
        return new ChatFrameEvent("404", message);
    }

    static ChatFrameEvent internalServerError(String message) {
        return new ChatFrameEvent("500", message);
    }

    @OnTextMessage
    public void onTextMessage(@PathParam("defaultChatFrame") String defaultChatFrame, String body) {
        System.out.println("Received message: " + body);

        try {
            try {
                defaultChatFrame = defaultChatFrame != null ? defaultChatFrame : ChatFrameRecorder.defaultChatFrame;
                ChatFrameContextSerialization.deserialize(mapper, context, memoryStore,
                        new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), defaultChatFrame);
            } catch (Exception e) {
                Log.error("Failed to deserialize chat context", e);
                connection.sendTextAndAwait(badRequest("Failed to deserialize chat context"));
                return;
            }
            try {
                if (context.currentFrameId() == null) {
                    Log.error("Current frame not set and no default chat frame found");
                    connection.sendTextAndAwait(notFound("Current frame not set and no default chat frame found"));
                    return;
                }
                chatFrameService.chat(context);
            } catch (UnauthorizedException e) {
                Log.error("Unauthorized", e);
                connection.sendTextAndAwait(unauthorized("Unauthorized"));
                return;
            } catch (ForbiddenException e) {
                Log.error("Forbidden", e);
                connection.sendTextAndAwait(forbidden("Forbidden"));
                return;
            } finally {
                context.scheduledWipes().forEach(w -> w.wipe());
            }
            StringWriter writer = new StringWriter();
            try {
                ChatFrameContextSerialization.serialize(context, memoryStore, mapper, writer);
            } catch (Exception e) {
                Log.error("Failed to serialize chat context", e);
                connection.sendTextAndAwait(internalServerError("Failed to serialize chat context"));
                return;
            }
            connection.sendTextAndAwait(writer.toString());
        } catch (Exception e) {
            Log.error("Failed to execute chat", e);
            connection.sendTextAndAwait(internalServerError("Failed to execute chat"));
            return;
        }
    }

}
