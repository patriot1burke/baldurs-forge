package org.baldurs.forge.context;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;


@Provider
@Produces(MediaType.WILDCARD)
public class ChatContextWriter implements MessageBodyWriter<ChatContext> {

    @Inject
    ObjectMapper mapper;

    @Inject
    ClientMemoryStore memory;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ChatContext.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(ChatContext t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
                //Log.info("***** ChatContextWriter.writeTo: " + type.getName());
                OutputStreamWriter writer = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8) {
                    @Override
                    public void close() throws IOException {
                        // do nothing because mappers auto close writers
                    }
                };
                try {
                    serialize(t, memory, mapper, writer);
                } catch (Throwable e) {
                    Log.error("Failed to serialize chat context", e);
                   
                }
                writer.flush();
    }

    public static void serialize(ChatContext t, ClientMemoryStore memory, ObjectMapper mapper, Writer writer)
            throws IOException {
        writer.write("{");
        writer.write("\"response\":");
        if (t.response() != null) {
            mapper.writeValue(writer, t.response());
        } else {
            writer.write("null");
        }
        writer.write(",");        
        writer.write("\"context\":");
        writer.write("{");
        writer.write("\"shared\":");
        mapper.writeValue(writer, t.sharedContext());
        writer.write(",");
        writer.write("\"memory\":");
        memory.writeJson(writer);
        writer.write("}");
   
        writer.write("}");
    }

}

