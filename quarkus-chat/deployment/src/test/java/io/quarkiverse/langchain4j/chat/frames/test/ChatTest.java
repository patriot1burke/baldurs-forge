package io.quarkiverse.langchain4j.chat.frames.test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameClient;
import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameRequest;
import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameResponse;
import io.quarkiverse.langchain4j.chat.frames.client.ClientTextMessage;
import io.quarkus.test.QuarkusUnitTest;

public class ChatTest {

    @RegisterExtension
    public static QuarkusUnitTest test = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(MyChatService.class));

    @Test
    public void testChat() {
        ChatFrameClient client = new ChatFrameClient("http://localhost:8081/chat");
        ChatFrameRequest request = new ChatFrameRequest();
        request.setUserMessage("Hello, world!");
        ChatFrameResponse response = client.chat(request);
        ClientTextMessage text = (ClientTextMessage) response.response().get(0);
        Assertions.assertEquals("defaultChat:Hello, world!", text.getText());
    }

}
