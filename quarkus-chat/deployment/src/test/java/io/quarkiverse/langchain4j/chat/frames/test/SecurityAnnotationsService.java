package io.quarkiverse.langchain4j.chat.frames.test;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;

import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.ChatFrameContext;
import io.quarkus.security.Authenticated;

public class SecurityAnnotationsService {

    @ChatFrame("authenticated")
    @Authenticated
    public void authenticated(ChatFrameContext context) {
        context.addEvent("authenticated");
    }

    @ChatFrame("deny")
    @DenyAll
    public void deny(ChatFrameContext context) {
        context.addEvent("deny");
    }

    @ChatFrame("roles")
    @RolesAllowed("allowed")
    public void rolesAllowed(ChatFrameContext context) {
        context.addEvent("roles-allowed");
    }
}
