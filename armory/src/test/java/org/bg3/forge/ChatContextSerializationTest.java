package org.bg3.forge;

import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.UserMessage;
import io.quarkiverse.langchain4j.chat.context.ChatContext;
import io.quarkiverse.langchain4j.chat.context.ChatContextWriter;
import io.quarkiverse.langchain4j.chat.context.ClientMemoryStore;
import io.quarkiverse.langchain4j.chat.frames.ObjectMessage;

public class ChatContextSerializationTest {

    //@Test
    public void testSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ChatContext context = new ChatContext();
        context.response().add(new ObjectMessage("Hello"));
        ClientMemoryStore memory = ClientMemoryStore.pojo(mapper);
        memory.updateMessages("test", List.of(new UserMessage("Hello")));
        
        StringWriter writer = new StringWriter();
        ChatContextWriter.serialize(context, memory, mapper, writer);
        System.out.println(writer.toString());
    }

}
