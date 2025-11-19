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
    ClientMemoryStore memoryStore;

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
                setCurrentIdentityAssociation(ctx);
                try {
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
