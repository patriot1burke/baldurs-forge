package io.quarkiverse.langchain4j.chat.frames.test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameClient;
import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameClientSession;
import io.quarkiverse.langchain4j.chat.frames.client.ClientStringMessage;
import io.quarkus.test.QuarkusUnitTest;

public class ChatTest {

    @RegisterExtension
    public static QuarkusUnitTest test = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(MyChatService.class, AnotherChatService.class, AnotherChat.class));

    @Test
    public void testChat() {
        ChatFrameClient client = new ChatFrameClient("http://localhost:8081/chat");
        ChatFrameClientSession session = client.session();
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("defaultChat:Hello, world!", text.getString());

        session = client.session("io.quarkiverse.langchain4j.chat.frames.test.MyChatService::chatone");
        text = (ClientStringMessage) session.chat("Hello, one!").get(0);
        Assertions.assertEquals("one:Hello, one!", text.getString());

        session = client.session("two");
        text = (ClientStringMessage) session.chat("Hello, two!").get(0);
        Assertions.assertEquals("two:Hello, two!", text.getString());
    }

    @Test
    public void testInterface() {
        ChatFrameClient client = new ChatFrameClient("http://localhost:8081/chat");
        ChatFrameClientSession session = client.session("another-chat");
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("hello:Hello, world!", text.getString());
    }

}
