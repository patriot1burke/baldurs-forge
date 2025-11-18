package io.quarkiverse.langchain4j.chat.frames.internal;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameController;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.logging.Log;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class ChatFrameEndpoint {

    @Inject
    ChatFrameController chatFrameService;

    @Inject
    ClientMemoryStore memoryStore;

    @Inject
    ChatFrameContextImpl context;

    @Inject
    ObjectMapper mapper;

    @Inject
    Vertx vertx;

    public void handleChat(RoutingContext ctx, String body) {
        Log.debug("Handling chat");
        Log.debugv("Body: {0}", body);
        vertx.executeBlocking(() -> {
            ManagedContext requestContext = Arc.container().requestContext();
            boolean alreadyActive = requestContext.isActive();
            if (!alreadyActive) {
                requestContext.activate();
            }
            try {
                try {
                    ChatFrameContextSerialization.deserialize(mapper, context, memoryStore,
                            new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
                } catch (Exception e) {
                    Log.error("Failed to deserialize chat context", e);
                    ctx.response().setStatusCode(400).end("Failed to deserialize chat context");
                    return null;
                }
                try {
                    chatFrameService.chat(context);
                } finally {
                    if (context.wipeScheduled()) {
                        context.clearMemory();
                    }
                }
                StringWriter writer = new StringWriter();
                try {
                    ChatFrameContextSerialization.serialize(context, memoryStore, mapper, writer);
                } catch (Exception e) {
                    Log.error("Failed to serialize chat context", e);
                    ctx.response().setStatusCode(500).end("Failed to serialize chat context");
                    return null;
                }
                ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(writer.toString());
            } catch (Exception e) {
                Log.error("Failed to execute chat", e);
                ctx.response().setStatusCode(500).end("Failed to execute chat");
            } finally {
                if (!alreadyActive && requestContext.isActive()) {
                    requestContext.terminate();
                }
            }

            return null;
        });
    }
}
