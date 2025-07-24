package org.baldurs.forge.context;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.NotImplementedException;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkus.logging.Log;
import jakarta.enterprise.inject.spi.CDI;

public abstract class ClientMemoryProvider implements Supplier<ChatMemoryProvider> {


    protected ChatMemoryStore getMemoryStore() {
        return new ChatMemoryStore() {

            @Override
            public void deleteMessages(Object memoryId) {
                CDI.current().select(ClientMemoryStore.class).get().deleteMessages(memoryId);
                
            }

            @Override
            public List<ChatMessage> getMessages(Object memoryId) {
                return CDI.current().select(ClientMemoryStore.class).get().getMessages(memoryId);
            }

            @Override
            public void updateMessages(Object memoryId, List<ChatMessage> messages) {
                CDI.current().select(ClientMemoryStore.class).get().updateMessages(memoryId, messages);
            }
            
        };


    }
}
