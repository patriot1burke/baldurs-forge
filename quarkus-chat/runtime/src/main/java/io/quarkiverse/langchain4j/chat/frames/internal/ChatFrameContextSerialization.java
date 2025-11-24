package io.quarkiverse.langchain4j.chat.frames.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChatFrameContextSerialization {

    static void deserialize(ObjectMapper mapper, ChatFrameContextImpl context, ChatFrameMemoryStore memory,
            InputStream entityStream, String defaultChatFrame) throws IOException, JsonProcessingException {
        // we have a static method for unit testing unmarshalling
        JsonNode node = mapper.readTree(entityStream);
        if (node == null || !node.isObject()) {
            return;
        }
        JsonNode userMessageNode = node.get("userMessage");
        if (userMessageNode != null && !userMessageNode.isNull()) {
            context.setUserMessage(userMessageNode.asText());
        }

        JsonNode paramsNode = node.get("params");
        Map<String, Object> params = new HashMap<>();
        if (paramsNode != null && !paramsNode.isNull()) {
            paramsNode.fields().forEachRemaining(field -> {
                JsonNode value = field.getValue();
                if (value != null && !value.isNull()) {
                    params.put(field.getKey(), value);
                }
            });
        }

        JsonNode contextNode = node.get("context");
        if (contextNode == null || contextNode.isNull()) {
            if (defaultChatFrame == null) {
                return;
            }
            context.setFrame(defaultChatFrame);
            context.data().putAll(params);
            return;
        }

        JsonNode currentFrameNode = contextNode.get("frame");
        if (currentFrameNode == null || currentFrameNode.isNull()) {
            if (defaultChatFrame == null) {
                return;
            }
            context.setFrame(defaultChatFrame);
            context.data().putAll(params);
        } else {
            // allow frame name to be null so client can just set context data and post
            ChatFrameData currentFrame = deserializeFrame(currentFrameNode, mapper, true);
            if (currentFrame.name == null) {
                currentFrame.name = defaultChatFrame;
            }
            if (currentFrame.name == null) {
                return;
            }
            context.setCurrent(currentFrame);
        }

        JsonNode memoryNode = contextNode.get("memory");
        if (memoryNode != null && !memoryNode.isNull()) {
            memory.readJson(memoryNode);
        }
    }

    static ChatFrameData deserializeFrame(JsonNode frameNode, ObjectMapper mapper, boolean first) {
        ChatFrameData frame = new ChatFrameData(mapper);

        JsonNode nameNode = frameNode.get("name");
        if (nameNode != null && !nameNode.isNull()) {
            frame.setName(nameNode.asText());
        } else if (first) {
            throw new IllegalArgumentException("Parent frame name not set");
        } else {
            frame.setName(ChatFrameRecorder.defaultChatFrame);
        }
        JsonNode parentNode = frameNode.get("parent");
        if (parentNode != null && !parentNode.isNull()) {
            frame.setParent(deserializeFrame(parentNode, mapper, false));
        }
        JsonNode dataNode = frameNode.get("data");
        if (dataNode != null && !dataNode.isNull()) {
            dataNode.fields().forEachRemaining(field -> {
                JsonNode value = field.getValue();
                if (value != null && !value.isNull()) {
                    frame.data().put(field.getKey(), value);
                }
            });
        }
        return frame;
    }

    static void serialize(ChatFrameContextImpl t, ChatFrameMemoryStore memory, ObjectMapper mapper, Writer writer)
            throws IOException {
        writer.write("{");
        writer.write("\"events\":");
        if (t.events() != null) {
            mapper.writeValue(writer, t.events());
        } else {
            writer.write("null");
        }
        writer.write(",");
        writer.write("\"context\":");
        writer.write("{");
        ChatFrameData currentFrame = t.getCurrent();
        writer.write("\"frame\":");
        serializeFrame(currentFrame, mapper, writer);
        writer.write(",");
        writer.write("\"memory\":");
        memory.writeJson(writer);
        writer.write("}");

        writer.write("}");
    }

    private static void serializeFrame(ChatFrameData frame, ObjectMapper mapper, Writer writer) throws IOException {
        writer.write("{");
        writer.write("\"name\":");
        mapper.writeValue(writer, frame.name());
        writer.write(",");
        writer.write("\"data\":");
        mapper.writeValue(writer, frame.data());
        if (frame.parent() != null) {
            writer.write(",");
            writer.write("\"parent\":");
            serializeFrame(frame.parent(), mapper, writer);
        }
        writer.write("}");
    }
}
