package io.quarkiverse.langchain4j.chat.frames.client;

import java.lang.reflect.Type;

public interface ClientChatEvent {
    String type();

    <T> T value(Class<T> type);

    <T> T value(Type type);
}
