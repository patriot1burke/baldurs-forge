package org.baldurs.forge.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService()
public interface ForgeAgent {

    @SystemMessage("""
        Answer a search query for a Baldur's Gate 3 item database.  Use the following JSON data as input to base your answer on.
    

        JSON Input:
        {json}

        Output:
        The output should be in HTML format.  Do not include any markdown formatting. Summarize what you found in a paragraph.
        """)
    String queryEquipment(@UserMessage String question, String json);

}
