package io.quarkiverse.langchain4j.chat.frames;

import java.util.List;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Wraps the ClientMemoryStore in a ChatMemoryStore.
 * ChatMemoryStore.deleteMessages can be called by QuarkusAiServiceContext.close() out of scope of a request.  Since ClientMemoryStore is request scoped,
 * this wrapper catches any CDI exceptions and eats them to avoid logging errors when this happens.
 * 
 */
@ApplicationScoped
public class ClientMemoryStoreBean implements ChatMemoryStore {
    @Inject
    ClientMemoryStore clientMemoryStore;

    @Override
    public void deleteMessages(Object memoryId) {
        try {
            clientMemoryStore.ping(); // ping forces CDI to reference the instance
        } catch (Exception ignored) {
            return;
        }
        clientMemoryStore.deleteMessages(memoryId);
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return clientMemoryStore.getMessages(memoryId);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        clientMemoryStore.updateMessages(memoryId, messages);
    }

}
