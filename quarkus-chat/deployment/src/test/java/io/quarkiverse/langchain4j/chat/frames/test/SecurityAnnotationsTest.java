package io.quarkiverse.langchain4j.chat.frames.test;

import jakarta.ws.rs.WebApplicationException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameClient;
import io.quarkiverse.langchain4j.chat.frames.client.ChatFrameClientSession;
import io.quarkiverse.langchain4j.chat.frames.client.ClientStringMessage;
import io.quarkus.security.test.utils.TestIdentityController;
import io.quarkus.security.test.utils.TestIdentityProvider;
import io.quarkus.test.QuarkusUnitTest;

public class SecurityAnnotationsTest {
    private static final String APP_PROPS = "" +
            "quarkus.http.auth.basic=true\n"
            + "quarkus.http.auth.permission.roles1.paths=/q/chat-frames\n" +
            "quarkus.http.auth.permission.roles1.policy=authenticated\n";;

    @RegisterExtension
    public static QuarkusUnitTest test = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(SecurityAnnotationsService.class, TestIdentityController.class,
                            TestIdentityProvider.class)
                    .addAsResource(new StringAsset(APP_PROPS), "application.properties"));

    static ChatFrameClient client;

    @BeforeAll
    public static void beforeAll() {
        client = new ChatFrameClient("http://localhost:8081/q/chat-frames");
        TestIdentityController.resetRoles()
                .add("user1", "password1", "allowed")
                .add("user2", "password2", "denied");
    }

    @Test
    public void testAuthenticated() {
        ChatFrameClientSession session = client.session("authenticated");
        session.basicAuth("user2", "password2");
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("authenticated", text.getString());
    }

    @Test
    public void testRole() {
        ChatFrameClientSession session = client.session("roles");
        session.basicAuth("user1", "password1");
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("roles-allowed", text.getString());
    }

    @Test
    public void testAuthenticated401() {
        ChatFrameClientSession session = client.session("authenticated");
        try {
            session.chat("Hello, world!").get(0);
            Assertions.fail("Expected WebApplicationException");
        } catch (WebApplicationException e) {
            Assertions.assertEquals(401, e.getResponse().getStatus());
        }
    }

    @Test
    public void testRole401() {
        ChatFrameClientSession session = client.session("roles");
        try {
            session.chat("Hello, world!").get(0);
            Assertions.fail("Expected WebApplicationException");
        } catch (WebApplicationException e) {
            Assertions.assertEquals(401, e.getResponse().getStatus());
        }
    }

    @Test
    public void testRole403() {
        ChatFrameClientSession session = client.session("roles");
        try {
            session.basicAuth("user2", "password2");
            session.chat("Hello, world!").get(0);
            Assertions.fail("Expected WebApplicationException");
        } catch (WebApplicationException e) {
            Assertions.assertEquals(403, e.getResponse().getStatus());
        }
    }
}
