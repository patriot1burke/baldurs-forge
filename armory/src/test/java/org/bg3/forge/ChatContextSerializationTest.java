package org.bg3.forge;

import java.io.StringWriter;
import java.util.List;

import org.baldurs.forge.chat.actions.MessageAction;
import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.context.ChatContextWriter;
import org.baldurs.forge.context.ClientMemoryStore;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.UserMessage;

public class ChatContextSerializationTest {

    //@Test
    public void testSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ChatContext context = new ChatContext();
        context.response().add(new MessageAction("Hello"));
        ClientMemoryStore memory = ClientMemoryStore.pojo(mapper);
        memory.updateMessages("test", List.of(new UserMessage("Hello")));
        
        StringWriter writer = new StringWriter();
        ChatContextWriter.serialize(context, memory, mapper, writer);
        System.out.println(writer.toString());
    }

}
