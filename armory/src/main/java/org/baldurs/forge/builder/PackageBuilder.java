package org.baldurs.forge.builder;

import jakarta.inject.Inject;

import org.baldurs.forge.messages.MarkdownToHtml;
import org.baldurs.forge.messages.PackageModMessage;
import org.baldurs.forge.services.BoostService;
import org.baldurs.forge.services.LibraryService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.Result;
import io.quarkiverse.langchain4j.chatscopes.ChatRoute;
import io.quarkiverse.langchain4j.chatscopes.ChatRouteContext;
import io.quarkiverse.langchain4j.chatscopes.ChatScope;
import io.quarkiverse.langchain4j.chatscopes.ChatScoped;
import io.quarkus.logging.Log;

@ChatScoped
public class PackageBuilder {

    NewModModel newMod;

    @Inject
    ChatRouteContext ctx;

    @Inject
    ObjectMapper mapper;

    @Inject
    PackageBuilderPrompt agent;

    @Inject
    LibraryService library;

    @Inject
    BoostService boostService;

    PackageModel currentPackage = new PackageModel();

    @ChatRoute("buildPackage")
    @MarkdownToHtml
    public Result<String> buildPackage() {
        String currentJson = null;
        try {
            currentJson = mapper.writeValueAsString(currentPackage);
        } catch (JsonProcessingException e) {
            Log.error("Error serializing newEquipment", e);
            throw new RuntimeException("Error serializing newEquipment", e);
        }
        return agent.buildPackage(ctx.request().userMessage(), PackageModel.schema, currentJson);
    }

    @Tool("Update the current package json document.")
    public String updatePackage(PackageModel packageModel) throws Exception {
        if (currentPackage == null) {
            currentPackage = packageModel;
        }
        if (packageModel.name != null) {
            currentPackage.name = packageModel.name;
        }
        if (packageModel.author != null) {
            currentPackage.author = packageModel.author;
        }
        if (packageModel.description != null) {
            currentPackage.description = packageModel.description;
        }
        return mapper.writeValueAsString(currentPackage);
    }

    @Tool(value = "Finish packaging the mod.", returnBehavior = ReturnBehavior.IMMEDIATE)
    public void finishPackage(PackageModel packageModel) {
        NewModModel model = newMod;
        model.name = packageModel.name;
        model.author = packageModel.author;
        model.description = packageModel.description;
        String baseFileName = PakFileExporter.toAlphaNumericUnderscore(model.name);
        ctx.response().event(new PackageModMessage(baseFileName + ".pak", model));
        ChatScope.pop();
    }
}
