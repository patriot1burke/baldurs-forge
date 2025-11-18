package io.quarkiverse.langchain4j.chat.frames.test;

import java.util.List;

import jakarta.ws.rs.WebApplicationException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameClient;
import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameClientSession;
import io.quarkiverse.langchain4j.chat.frames.client.ClientChatFrameMessage;
import io.quarkiverse.langchain4j.chat.frames.client.ClientStringMessage;
import io.quarkus.test.QuarkusUnitTest;

public class ChatTest {

    @RegisterExtension
    public static QuarkusUnitTest test = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(MyChatService.class, AnotherChatService.class, AnotherChat.class, MockResult.class,
                            ResponseChatService.class, TestTextMessage.class, TestIntegerMessage.class,
                            TestChatDataService.class));

    static ChatFrameClient client;

    @BeforeAll
    public static void beforeAll() {
        client = new ChatFrameClient("http://localhost:8081/chat");
    }

    @Test
    public void testChat() {
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
    public void testStringResult() {
        ChatFrameClientSession session = client.session("string-result");
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("string-result", text.getString());

        session = client.session("null-string");
        Assertions.assertTrue(session.chat("Hello, world!").isEmpty());
    }

    @Test
    public void testResult() {
        ChatFrameClientSession session = client.session("result");
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("result", text.getString());

        session = client.session("null-result");
        Assertions.assertTrue(session.chat("Hello, world!").isEmpty());

        session = client.session("null-execution");
        Assertions.assertTrue(session.chat("Hello, world!").isEmpty());
    }

    @Test
    public void testResultToolExecution() {
        ChatFrameClientSession session = client.session("execution");
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("result-with-execution", text.getString());
    }

    @Test
    public void testInterface() {
        ChatFrameClientSession session = client.session("another-chat");
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("hello:Hello, world!", text.getString());
    }

    @Test
    public void testStringResponseMessage() {
        ChatFrameClientSession session = client.session("text");
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("TestText:stringResponse", text.getString());
    }

    @Test
    public void testIntegerResponseMessage() {
        ChatFrameClientSession session = client.session("int");
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("TestInteger:123", text.getString());
    }

    @Test
    public void testMultiResult() {
        ChatFrameClientSession session = client.session("multi-result");
        List<ClientChatFrameMessage> messages = session.chat("Hello, world!");
        Assertions.assertEquals(2, messages.size());
        ClientStringMessage text1 = (ClientStringMessage) messages.get(0);
        Assertions.assertEquals("TestText:textResult", text1.getString());
        ClientStringMessage text2 = (ClientStringMessage) messages.get(1);
        Assertions.assertEquals("TestInteger:123", text2.getString());
    }

    @Test
    public void testFrameData() {
        ChatFrameClientSession session = client.session("frame-data");
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("start", text.getString());

        text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("test", text.getString());
    }

    @Test
    public void testScopedFrameData() {
        ChatFrameClientSession session = client.session("call-nested");
        ClientStringMessage text = (ClientStringMessage) session.chat("dummy").get(0);
        Assertions.assertEquals("first", text.getString());

        text = (ClientStringMessage) session.chat("dummy").get(0);
        Assertions.assertEquals("nested", text.getString());

        text = (ClientStringMessage) session.chat("dummy").get(0);
        Assertions.assertEquals("second", text.getString());

        session = client.session("sent-data");
        session.context().frame().setData("sentData", "sentData");
        text = (ClientStringMessage) session.chat("dummy").get(0);
        Assertions.assertEquals("sentData", text.getString());
    }

    @Test
    public void testException() {
        ChatFrameClientSession session = client.session("exception");
        try {
            session.chat("dummy");
            Assertions.fail("Expected WebApplicationException");
        } catch (WebApplicationException e) {
            Assertions.assertEquals(500, e.getResponse().getStatus());
        }
    }
}
