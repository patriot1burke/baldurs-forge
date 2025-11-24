package org.baldurs.forge.mainmenu;

import jakarta.enterprise.context.SessionScoped;

import org.baldurs.forge.messages.MarkdownStringMessage;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.chat.frames.ChatFrame;
import io.quarkiverse.langchain4j.chat.frames.EventMapper;

@SessionScoped
@RegisterAiService
public interface MainMenuPrompt {

    @SystemMessage(fromResource = "prompts/mainMenuCommands.txt")
    @ToolBox({ MainMenuToolBox.class })
    @ChatFrame("mainMenu")
    @EventMapper(MarkdownStringMessage.class)
    Result<String> chat(@UserMessage String message);
}
