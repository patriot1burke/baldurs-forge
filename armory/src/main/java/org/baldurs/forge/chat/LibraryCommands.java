package org.baldurs.forge.chat;

import java.util.List;

import org.baldurs.forge.context.ChatContext;
import org.baldurs.forge.model.StatModel;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.toolbox.LibraryService;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LibraryCommands {

    @Inject
    ChatContext context;

    @Inject
    LibraryService libraryService;

    @Tool("Find a localization by id")
    public void getLocalization(String handle) {
        String localization = libraryService.archive().localizations.getLocalization(handle);
        if (localization != null) {
            context.response().add(new MessageAction(localization));
        } else {
            context.response().add(new MessageAction("Could not find localization: " + handle));
        }
    }


    @Tool("Find a root template by stat name")
    public void getRootTemplateByStatName(String statName) {
        RootTemplate rootTemplate = libraryService.findRootTemplateByStatName(statName);
        if (rootTemplate != null) {
            context.response().add(new MessageAction(rootTemplate));
        } else {
            context.response().add(new MessageAction("Could not find root template"));
        }
    }

    @Tool("Get or find or show a stat by name")
    public void findStatByName(String name, @P(value = "Add parent data?", required = false) Boolean parentData) {
        StatModel stat = libraryService.getStatByName(name, parentData);
        if (stat != null) {
            context.response().add(new MessageAction(stat));
        } else {
            context.response().add(new MessageAction("Could not find stat: " + name));
        }
    }

    @Tool("Find all possible values for a Stat attribute")
    public void findStatAttributeValues(String attributeName) {
        List<String> values = libraryService.getStatAttributeValues(attributeName);
        if (values.isEmpty()) {
            context.response().add(new MessageAction("Could not find any values for attribute: " + attributeName));
        } else {
            context.response().add(new MessageAction(values));
        }
    }

    @Tool("Find all possible boost function signatures")
    public void findAllBoostFunctionSignatures() {
        List<String> values = libraryService.getAllBoostFunctionSignatures();
        if (values.isEmpty()) {
            context.response().add(new MessageAction("Could not find any boost function signatures"));
        } else {
            context.response().add(new MessageAction(values));
        }
    }


}
