import jakarta.ws.rs.WebApplicationException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameClient;
import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameClientSession;
import io.quarkiverse.langchain4j.chat.frames.client.ClientStringMessage;
import io.quarkus.test.QuarkusUnitTest;

public class SecurityTest {
    private static final String APP_PROPS = "" +
            "quarkus.http.auth.basic=true\n" +
            "quarkus.http.auth.policy.r1.roles-allowed=allowed\n" +
            "quarkus.http.auth.permission.roles1.paths=/q/chat-frames\n" +
            "quarkus.http.auth.permission.roles1.policy=r1\n";

    @RegisterExtension
    public static QuarkusUnitTest test = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(MyChatService.class, MockResult.class, TestIdentityController.class,
                            TestIdentityProvider.class));

    static ChatFrameClient client;

    @BeforeAll
    public static void beforeAll() {
        client = new ChatFrameClient("http://localhost:8081/q/chat-frames");
        TestIdentityController.resetRoles()
                .add("user1", "password1", "allowed")
                .add("user2", "password2", "denied");
    }

    @Test
    public void testAuthorized() {
        ChatFrameClientSession session = client.session();
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("defaultChat:Hello, world!", text.getString());
    }

    @Test
    public void testUnauthorized() {
        ChatFrameClientSession session = client.session("exception");
        session.basicAuth("user2", "password2");
        try {
            ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
            Assertions.fail("Expected WebApplicationException");
        } catch (WebApplicationException e) {
            Assertions.assertEquals(401, e.getResponse().getStatus());
        }
    }

    @Test
    public void testUnauthenticated() {
        ChatFrameClientSession session = client.session("exception");
        try {
            ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
            Assertions.fail("Expected WebApplicationException");
        } catch (WebApplicationException e) {
            Assertions.assertEquals(401, e.getResponse().getStatus());
        }
    }
}
