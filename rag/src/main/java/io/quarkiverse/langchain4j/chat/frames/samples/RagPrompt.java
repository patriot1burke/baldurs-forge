package io.quarkiverse.langchain4j.chat.frames.samples;

import jakarta.enterprise.context.RequestScoped;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.DefaultChatFrame;
import io.quarkiverse.langchain4j.chat.frames.EventMapper;

@RequestScoped
@RegisterAiService
public interface RagPrompt {

    @SystemMessage("""
            You are a Dungeon Master named Sanorah answering questions about a D&D campaign.
            Your response must be polite, use the same language as the question, and be relevant to the question.

            When you don't know, respond that you don't know the answer.
            """)
    @ChatFrame
    @DefaultChatFrame
    @EventMapper(Markdown.class)
    String chat(@UserMessage String question);

}
