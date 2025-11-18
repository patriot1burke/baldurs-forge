package io.quarkiverse.langchain4j.chat.frames.internal;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class ChatFrameHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        ChatFrameRecorder.CONTAINER.beanInstance(ChatFrameEndpoint.class).handleChat(ctx, ctx.body().asString());
    }
}
