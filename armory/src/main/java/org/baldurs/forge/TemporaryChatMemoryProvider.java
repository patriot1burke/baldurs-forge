package org.baldurs.forge;

import java.util.function.Supplier;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;

/**
 * Should be used with appliations that have a default persistent chat memory store
 * and have @RequestScoped AiServices that want to discard chat memory after the request is complete
 */
public class TemporaryChatMemoryProvider implements Supplier<ChatMemoryProvider> {

    @Override
    public ChatMemoryProvider get() {
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object id) {
                return MessageWindowChatMemory.builder().id(id).chatMemoryStore(new InMemoryChatMemoryStore()).maxMessages(30).build();
            }
        };
    }
}
