package io.quarkiverse.langchain4j.chat.frames.internal;

import jakarta.enterprise.context.ContextNotActiveException;

import io.quarkiverse.langchain4j.spi.DefaultMemoryIdProvider;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InstanceHandle;

public class DefaultChatFrameMemoryIdProvider implements DefaultMemoryIdProvider {

    @Override
    public String getMemoryId() {
        ArcContainer container = Arc.container();
        if (container == null) {
            return null;
        }
        InstanceHandle<ChatFrameContextImpl> instance = container.instance(ChatFrameContextImpl.class);
        if (instance.isAvailable()) {
            try {
                return instance.get().contextPath();
            } catch (ContextNotActiveException ignored) {
                // this means that the session scope was not active, so we can't provide a value
                return null;
            }
        }
        return null;
    }

}
