package io.quarkiverse.langchain4j.chat.frames.internal;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.langchain4j.chat.frames.ChatFrameController;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.logging.Log;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class ChatFrameEndpoint {

    @Inject
    ChatFrameController chatFrameService;

    @Inject
    ChatFrameMemoryStore memoryStore;

    @Inject
    ChatFrameContextImpl context;

    CurrentIdentityAssociation association;

    @Inject
    ObjectMapper mapper;

    @Inject
    Vertx vertx;

    @PostConstruct
    public void init() {
        Instance<CurrentIdentityAssociation> association = CDI.current().select(CurrentIdentityAssociation.class);
        this.association = association.isResolvable() ? association.get() : null;
    }

    static class ContextActivator {
        boolean requestAlreadyActive = false;
        boolean sessionAlreadyActive = false;
        ManagedContext requestContext;
        ManagedContext sessionContext;

        static ContextActivator activate() {
            ContextActivator activator = new ContextActivator();
            return activator;
        }

        private ContextActivator() {
            requestContext = Arc.container().requestContext();
            sessionContext = Arc.container().sessionContext();
            requestAlreadyActive = requestContext.isActive();
            sessionAlreadyActive = sessionContext.isActive();
            if (!requestAlreadyActive) {
                requestContext.activate();
            }
            if (!sessionAlreadyActive) {
                sessionContext.activate();
            }
        }

        void deactivateRequest() {
            if (!requestAlreadyActive && requestContext.isActive()) {
                return;
            }
            requestAlreadyActive = false;
            requestContext.terminate();
        }

        void deactivateSession() {
            if (!sessionAlreadyActive && sessionContext.isActive()) {
                return;
            }
            sessionAlreadyActive = false;
            sessionContext.terminate();
        }

        void deactivate() {
            deactivateRequest();
            deactivateSession();
        }
    }

    public void handleChat(RoutingContext ctx, String body) {
        Log.debug("Handling chat");
        Log.debugv("Body: {0}", body);
        vertx.executeBlocking(() -> {
            ContextActivator activator = ContextActivator.activate();
            setCurrentIdentityAssociation(ctx);
            try {
                try {
                    String path = ctx.request().path();
                    String defaultChatFrame = ChatFrameRecorder.defaultChatFrame;
                    int index = path.indexOf(ChatFrameRecorder.rootPath);
                    if (index > -1) {
                        String newDefault = path.substring(index + ChatFrameRecorder.rootPath.length());
                        if (!newDefault.isEmpty()) {
                            defaultChatFrame = newDefault;
                            Log.info("Defaultchat from path: " + defaultChatFrame);
                        }
                    }
                    ChatFrameContextSerialization.deserialize(mapper, context, memoryStore,
                            new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), defaultChatFrame);
                } catch (Exception e) {
                    Log.error("Failed to deserialize chat context", e);
                    ctx.response().setStatusCode(400).end("Failed to deserialize chat context");
                    return null;
                }
                try {
                    if (context.currentFrameId() == null) {
                        Log.error("Current frame not set and no default chat frame found");
                        ctx.response().setStatusCode(404).end("Current frame not set and no default chat frame found");
                        return null;
                    }
                    chatFrameService.chat(context);
                } catch (UnauthorizedException e) {
                    Log.error("Unauthorized", e);
                    ctx.response().setStatusCode(401).end("Unauthorized");
                    return null;
                } catch (ForbiddenException e) {
                    Log.error("Forbidden", e);
                    ctx.response().setStatusCode(403).end("Forbidden");
                    return null;
                } finally {
                    context.scheduledWipes().forEach(w -> w.wipe());
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
                activator.deactivate();
            }

            return null;
        });
    }

    protected void setCurrentIdentityAssociation(RoutingContext routingContext) {
        if (association != null) {
            QuarkusHttpUser existing = (QuarkusHttpUser) routingContext.user();
            if (existing != null) {
                SecurityIdentity identity = existing.getSecurityIdentity();
                association.setIdentity(identity);
            } else {
                association.setIdentity(QuarkusHttpUser.getSecurityIdentity(routingContext, null));
            }
        }
    }
}
