package org.baldurs.forge.mainmenu;

import org.baldurs.forge.messages.MarkdownToHtml;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.chatscopes.ChatRoute;
import io.quarkiverse.langchain4j.chatscopes.ChatScoped;

@ChatScoped
@RegisterAiService
public interface MainMenuPrompt {

    @SystemMessage(fromResource = "prompts/mainMenuCommands.txt")
    @ToolBox({ MainMenuToolBox.class })
    @ChatRoute("mainMenu")
    @MarkdownToHtml
    Result<String> chat(@UserMessage String message);
}
