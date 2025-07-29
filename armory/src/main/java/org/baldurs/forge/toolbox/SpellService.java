package org.baldurs.forge.toolbox;

import java.util.HashMap;
import java.util.Map;

import org.baldurs.forge.model.Spell;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baldurs.forge.agents.ForgeAgent;
import org.baldurs.forge.agents.MetadataAgent;
import org.baldurs.forge.model.Equipment;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.ArchiveSource;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive;
import org.baldurs.forge.toolbox.BoostService.BoostWriter;
import org.baldurs.forge.util.FilterExpression;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

public class SpellService {
    @Inject
    LibraryService libraryService;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    BoostService boostService;


    Map<String, Spell> spellDB = new HashMap<>();

    public void start(@Observes StartupEvent event) throws Exception {
        //buildSpells();
        //load();
    }

    private void buildSpells() {
        Log.info("Building spell database");
        Map<String, StatsArchive.Stat> spells = libraryService.archive().stats.getByType(StatsArchive.SPELL_TYPE);
        for (StatsArchive.Stat spell : spells.values()) {
            addSpell(spellDB, spell);
        }
        Log.info("Added " + spellDB.size() + " to spell database");
    }

    private void addSpell(Map<String, Spell> db, StatsArchive.Stat item) {
        String id = item.name;
        String displayName = item.getField("DisplayName");
        String name = libraryService.archive().localizations.getLocalization(displayName);
        if (name == null) {
            Log.debug("No name for " + id);
            return;
        }
        String description = boostService.statDescription(item);
        String icon = item.getField("Icon");
        icon = libraryService.icons().get(icon);    
        Spell spell = new Spell(id, name, description, icon);
        db.put(id, spell);
    }
    private void load() throws Exception {

        Log.info("Loading items...");

        Log.info("**************************************************");
        Log.info("Removing all spells from vector db for clean ingestion...");
        Log.info("**************************************************");

        Filter filter = new MetadataFilterBuilder("type").isEqualTo("Spell");
        embeddingStore.removeAll(filter);
        Collection<Spell> spells = spellDB.values();

        ingest(spells);
    }

    private void ingest(Collection<Spell> spells) {
        EmbeddingStoreIngestor ingester = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        List<Document> docs = new ArrayList<>();
        for (Spell item : spells) {
            BoostWriter boostWriter = boostService.text();
            StatsArchive.Stat stat = libraryService.archive().stats.getByName(item.id());
            boostService.stat(stat, boostWriter);
            String boost = boostWriter.toString();
            Metadata metadata = Metadata.from(Map.of(
                    "id", item.id(),
                    "type", "Spell"));
            String content = "Name: " + item.name() + "\n" +
                    "Type: Spell" + "\n" +
                    "Description: " + item.description() + "\n";
            // Log.info("\nid: " + item.id() + "\n" + content);
            Document document = Document.from(content, metadata);
            docs.add(document);
        }
        ingester.ingest(docs);

        Log.info("Ingested " + spells.size() + " items");
    }

    public List<Spell> ragSearch(String queryString) {
        Filter filter = new MetadataFilterBuilder("type").isEqualTo("Spell");
        Log.infof("Querying for: %s", queryString);


        Embedding embedding = embeddingModel.embed(queryString).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(embedding)
                .filter(filter)
                .minScore(0.75)
                .maxResults(5)
                .build();

        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(request);
        Log.info("Search results: " + search.matches().size());
        List<Spell> result = search.matches().stream().map(m -> {
            String id = m.embedded().metadata().getString("id");
            return spellDB.get(id);
        }).toList();

        return result;
    }

}
