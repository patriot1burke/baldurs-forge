package org.baldurs.forge.context;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

public class MessageWindowClientMemoryProvider extends ClientMemoryProvider {

    @Override
    public ChatMemoryProvider get() {
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object id) {
                return MessageWindowChatMemory.builder().id(id).chatMemoryStore(getMemoryStore()).maxMessages(30).build();
            }
        };
    }

    
}
