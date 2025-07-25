package org.baldurs.forge.context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

@Provider
@Consumes(MediaType.WILDCARD)
public class ChatContextReader implements MessageBodyReader<ChatContext> {
    @Inject
    ObjectMapper mapper;

    @Inject
    ChatContext context;

    @Inject
    ClientMemoryStore memory;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ChatContext.class.isAssignableFrom(type);
    }

    @Override
    public ChatContext readFrom(Class<ChatContext> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        Log.info("Reading chat context");
        return deserialize(mapper, context, memory, entityStream);
    }

    public static ChatContext deserialize(ObjectMapper mapper, ChatContext context, ClientMemoryStore memory,
            InputStream entityStream) throws IOException, JsonProcessingException {
        // we have a static method for unit testing unmarshalling
        JsonNode node = mapper.readTree(entityStream);
        if (node == null || !node.isObject()) {
            return context;
        }
        JsonNode userMessageNode = node.get("userMessage");
        if (userMessageNode != null && !userMessageNode.isNull()) {
            context.setUserMessage(userMessageNode.asText());
        }
         JsonNode contextNode = node.get("context");
        if (contextNode == null || contextNode.isNull()) {
            return context;
        }
        JsonNode memoryIdNode = contextNode.get("memoryId");
        if (memoryIdNode != null && !memoryIdNode.isNull()) {
            context.setMemoryId(memoryIdNode.asText());
        }
        JsonNode memoryNode = contextNode.get("memory");
        if (memoryNode != null && !memoryNode.isNull()) {
            memory.readJson(memoryNode);
        }

        JsonNode sharedNode = contextNode.get("shared");
        if (sharedNode != null && !sharedNode.isNull()) {
            sharedNode.fields().forEachRemaining(field -> {
                JsonNode value = field.getValue();
                if (value != null && !value.isNull()) {
                    context.sharedContext().put(field.getKey(), value);
                }
            });
        }
        return context;
    }
}
