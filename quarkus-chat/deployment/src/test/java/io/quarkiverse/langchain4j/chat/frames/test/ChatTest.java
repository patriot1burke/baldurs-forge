package io.quarkiverse.langchain4j.chat.frames.test;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.WebApplicationException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameClient;
import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameClientSession;
import io.quarkiverse.langchain4j.chat.frames.client.ClientChatEvent;
import io.quarkus.test.QuarkusUnitTest;

public class ChatTest {

    @RegisterExtension
    public static QuarkusUnitTest test = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(MyChatService.class, AnotherChatService.class, AnotherChat.class, MockResult.class,
                            Customer.class,
                            ResponseChatService.class, TestMapper.class,
                            TestChatDataService.class));

    static ChatFrameClient client;

    @BeforeAll
    public static void beforeAll() {
        client = new ChatFrameClient("http://localhost:8081/q/chat-frames");
    }

    @Test
    public void testChat() {
        ChatFrameClientSession session = client.session();
        ClientChatEvent clientChatEvent = session.chat("Hello, world!").get(0);
        String text = clientChatEvent.value(String.class);
        Assertions.assertEquals("defaultChat:Hello, world!", text);
        Assertions.assertEquals("StringMessage", clientChatEvent.type());

        session = client.session("io.quarkiverse.langchain4j.chat.frames.test.MyChatService::chatone");
        text = session.chat("Hello, one!").get(0).value(String.class);
        Assertions.assertEquals("one:Hello, one!", text);

        session = client.session("two");
        text = session.chat("Hello, two!").get(0).value(String.class);
        Assertions.assertEquals("two:Hello, two!", text);
    }

    @Test
    public void testStringResult() {
        ChatFrameClientSession session = client.session("string-result");
        String text = session.chat("Hello, world!").get(0).value(String.class);
        Assertions.assertEquals("string-result", text);

        session = client.session("null-string");
        Assertions.assertTrue(session.chat("Hello, world!").isEmpty());
    }

    @Test
    public void testResult() {
        ChatFrameClientSession session = client.session("result");
        String text = session.chat("Hello, world!").get(0).value(String.class);
        Assertions.assertEquals("result", text);

        session = client.session("null-result");
        Assertions.assertTrue(session.chat("Hello, world!").isEmpty());

        session = client.session("null-execution");
        Assertions.assertTrue(session.chat("Hello, world!").isEmpty());
    }

    @Test
    public void testResultToolExecution() {
        ChatFrameClientSession session = client.session("execution");
        ClientChatEvent clientChatEvent = session.chat("Hello, world!").get(0);
        String text = clientChatEvent.value(String.class);
        Assertions.assertEquals("result-with-execution", text);
    }

    @Test
    public void testInterface() {
        ChatFrameClientSession session = client.session("another-chat");
        ClientChatEvent clientChatEvent = session.chat("Hello, world!").get(0);
        String text = clientChatEvent.value(String.class);
        Assertions.assertEquals("hello:Hello, world!", text);
    }

    @Test
    public void testStringResponseMessage() {
        ChatFrameClientSession session = client.session("text");
        ClientChatEvent clientChatEvent = session.chat("Hello, world!").get(0);
        String text = clientChatEvent.value(String.class);
        Assertions.assertEquals("TestText:stringResponse", text);
    }

    @Test
    public void testIntegerResponseMessage() {
        ChatFrameClientSession session = client.session("int");
        ClientChatEvent clientChatEvent = session.chat("Hello, world!").get(0);
        String text = clientChatEvent.value(String.class);
        Assertions.assertEquals("TestInteger:123", text);
    }

    @Test
    public void testMultiResult() {
        ChatFrameClientSession session = client.session("multi-result");
        List<ClientChatEvent> messages = session.chat("Hello, world!");
        Assertions.assertEquals(2, messages.size());
        ClientChatEvent clientChatEvent1 = messages.get(0);
        String text1 = clientChatEvent1.value(String.class);
        Assertions.assertEquals("TestText:textResult", text1);
        ClientChatEvent clientChatEvent2 = messages.get(1);
        String text2 = clientChatEvent2.value(String.class);
        Assertions.assertEquals("TestInteger:123", text2);
    }

    @Test
    public void testFrameData() {
        ChatFrameClientSession session = client.session("frame-data");
        ClientChatEvent clientChatEvent = session.chat("Hello, world!").get(0);
        String text = clientChatEvent.value(String.class);
        Assertions.assertEquals("start", text);

        text = session.chat("Hello, world!").get(0).value(String.class);
        Assertions.assertEquals("test", text);
    }

    @Test
    public void testScopedFrameData() {
        ChatFrameClientSession session = client.session("call-nested");
        ClientChatEvent clientChatEvent = session.chat("dummy").get(0);
        String text = clientChatEvent.value(String.class);
        Assertions.assertEquals("first", text);

        text = session.chat("dummy").get(0).value(String.class);
        Assertions.assertEquals("nested", text);

        text = session.chat("dummy").get(0).value(String.class);
        Assertions.assertEquals("second", text);

        session = client.session("sent-data");
        text = session.chat("dummy", Map.of("sentData", "sentData")).get(0).value(String.class);
        Assertions.assertEquals("sentData", text);
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

    @Test
    public void testEventType() {
        ChatFrameClientSession session = client.session("event-type");
        ClientChatEvent clientChatEvent = session.chat("Hello, world!").get(0);
        String text = clientChatEvent.value(String.class);
        Assertions.assertEquals("event-type", text);
        Assertions.assertEquals("my-event-type", clientChatEvent.type());
    }

    @Test
    public void testEventTypeOnClass() {
        ChatFrameClientSession session = client.session("customer");
        ClientChatEvent clientChatEvent = session.chat("Hello, world!").get(0);
        Customer customer = clientChatEvent.value(Customer.class);
        Assertions.assertEquals("John Doe", customer.name());
        Assertions.assertEquals("john.doe@example.com", customer.email());
        Assertions.assertEquals("customer", clientChatEvent.type());
    }
}
