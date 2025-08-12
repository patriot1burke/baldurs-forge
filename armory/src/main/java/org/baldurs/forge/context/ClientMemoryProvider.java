package org.baldurs.forge.context;

import java.util.function.Supplier;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

public abstract class ClientMemoryProvider implements Supplier<ChatMemoryProvider> {

    protected ChatMemoryStore getMemoryStore() {
        return ClientMemoryStore.beanDelegate();

    }
}
