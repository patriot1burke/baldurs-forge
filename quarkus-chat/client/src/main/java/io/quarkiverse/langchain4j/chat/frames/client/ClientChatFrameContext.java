package io.quarkiverse.langchain4j.chat.frames.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ClientChatFrameContext {
    ClientChatFrameData frame;
    JsonNode memory;

    public ClientChatFrameData frame() {
        if (frame == null) {
            frame = new ClientChatFrameData();
        }
        return frame;
    }
}
