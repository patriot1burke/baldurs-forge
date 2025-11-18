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
import io.restassured.RestAssured;

public class SecurityTest {
    private static final String APP_PROPS = "" +
            "quarkus.http.auth.basic=true\n" +
            "quarkus.http.auth.policy.r1.roles-allowed=allowed\n" +
            "quarkus.http.auth.permission.roles1.paths=/q/chat-frames,/message\n" +
            "quarkus.http.auth.permission.roles1.policy=r1\n";

    @RegisterExtension
    public static QuarkusUnitTest test = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(MyChatService.class, MockResult.class, TestIdentityController.class,
                            TestIdentityProvider.class, PathHandler.class)
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
    public void testPath() {
        // just to see if security is working
        RestAssured.given()
                .auth().basic("user1", "password1")
                .when().get("/message")
                .then().statusCode(200);
        RestAssured.given()
                .auth().basic("user2", "password2")
                .when().get("/message")
                .then().statusCode(403);
    }

    @Test
    public void testAuthorized() {
        ChatFrameClientSession session = client.session();
        session.basicAuth("user1", "password1");
        ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
        Assertions.assertEquals("defaultChat:Hello, world!", text.getString());
    }

    @Test
    public void testUnauthorized() {
        ChatFrameClientSession session = client.session();
        session.basicAuth("user2", "password2");
        try {
            ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
            Assertions.fail("Expected WebApplicationException");
        } catch (WebApplicationException e) {
            Assertions.assertEquals(403, e.getResponse().getStatus());
        }
    }

    @Test
    public void testUnauthenticated() {
        ChatFrameClientSession session = client.session();
        try {
            ClientStringMessage text = (ClientStringMessage) session.chat("Hello, world!").get(0);
            Assertions.fail("Expected WebApplicationException");
        } catch (WebApplicationException e) {
            Assertions.assertEquals(401, e.getResponse().getStatus());
        }
    }
}
