package org.baldurs.forge.services;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baldurs.forge.agents.MetadataAgent;
import org.baldurs.forge.model.Equipment;
import org.baldurs.forge.model.EquipmentModel;
import org.baldurs.forge.model.EquipmentSlot;
import org.baldurs.forge.model.EquipmentType;
import org.baldurs.forge.model.Rarity;
import org.baldurs.forge.scanner.ArchiveSource;
import org.baldurs.forge.scanner.RootTemplate;
import org.baldurs.forge.scanner.StatsArchive;
import org.baldurs.forge.services.BoostService.BoostWriter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class EquipmentDB {
    @Inject
    LibraryService libraryService;

    @Inject
    BoostService boostService;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    MetadataAgent metadataAgent;

    Map<String, Equipment> equipmentDB = new HashMap<>();

    public void start(@Observes StartupEvent event) throws Exception {
        buildEquipment();
        load();
    }

    private void buildEquipment() {
        Log.info("Building equipment database");
        Map<String, StatsArchive.Stat> armors = libraryService.archive().stats.getByType(EquipmentType.Armor.name());
        for (StatsArchive.Stat armor : armors.values()) {
            addEquipment(equipmentDB, armor);
        }
        Map<String, StatsArchive.Stat> weapons = libraryService.archive().stats.getByType(EquipmentType.Weapon.name());
        for (StatsArchive.Stat weapon : weapons.values()) {
            addEquipment(equipmentDB, weapon);
        }
        Log.info("Added " + equipmentDB.size() + " to equipment database");

    }

    private void addEquipment(Map<String, Equipment> db, StatsArchive.Stat item) {
        String id = item.name;
        EquipmentType type = EquipmentType.valueOf(item.type);
        EquipmentSlot slot = EquipmentSlot.fromString(item.getField("Slot"));
        if (slot == EquipmentSlot.Unknown) {
            Log.debug("Unknown slot for " + id);
            return;
        }
        int armorClass = -1;
        if (type == EquipmentType.Armor && slot == EquipmentSlot.Breast) {
            String field = item.getField("ArmorClass");
            if (field != null && !field.isEmpty()) {
                armorClass = Integer.parseInt(field);
            }
        }
        Rarity rarity = Rarity.fromString(item.getField("Rarity"));
        RootTemplate rootTemplate = libraryService.archive().rootTemplates
                .getRootTemplate(item.getField("RootTemplate"));
        if (rootTemplate == null) {
            Log.debug("No root template for " + id);
            return;
        }
        String displayName = rootTemplate.DisplayName;
        if (displayName == null || displayName.isEmpty()) {
            Log.debug("No display name for " + id);
            return;
        }
        String name = libraryService.archive().localizations.getLocalization(displayName);
        if (name == null) {
            Log.debug("No name for " + id);
            return;
        }
        String description = "";
        if (rootTemplate.Description != null) {
            description = libraryService.archive().localizations.getLocalization(rootTemplate.Description);
        } else {
            Log.debug("No description for " + id);
        }
        BoostWriter boostWriter = boostService.html();
        try {
            boostService.stat(item, boostWriter);
        } catch (Exception e) {
            throw new RuntimeException("Error processing boosts for " + id, e);
        }
        String boost = boostWriter.toString();
        // Log.infof("Boosts for %s: %s", id, boost);
        String icon = rootTemplate.resolveIcon();
        icon = libraryService.icons().get(icon);
        String weaponType = null;
        if (type == EquipmentType.Weapon) {
            String proficiencies = item.getField("Proficiency Group");
            if (proficiencies != null) {
                String[] profs = proficiencies.split(";");
                if (profs.length > 0) {
                    weaponType = profs[0].substring(0, profs[0].length() - 1);
                }
            }
        }
        String armorType = null;
        if (type == EquipmentType.Armor) {
            armorType = item.getField("ArmorType");
            if (armorType != null) {
                if (armorType.equals("None")) {
                    armorType = null;
                } else {
                    armorType = addSpacesBetweenCapitals(armorType);
                }
            }
        }
        Set<String> weaponProperties = new HashSet<>();
        if (type == EquipmentType.Weapon) {
            String properties = item.getField("Weapon Properties");
            if (properties != null) {
                String[] props = properties.split(";");
                for (String prop : props) {
                    weaponProperties.add(prop);
                }
            }
        }
        Equipment equipment = new Equipment(id, type, slot, rarity, name, description, boost, icon, weaponType,
                armorType, armorClass, weaponProperties, rootTemplate, item);
        db.put(id, equipment);
    }

    private void load() throws Exception {

        Log.info("Loading items...");

        Log.info("**************************************************");
        Log.info("Removing all items from vector db for clean ingestion...");
        Log.info("**************************************************");

        Filter filter = new MetadataFilterBuilder("type").isNotEqualTo("Spell");
        embeddingStore.removeAll(filter);
        ingest(equipmentDB.values());
    }

    private void ingest(Collection<Equipment> equipment) {
        EmbeddingStoreIngestor ingester = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        List<Document> docs = new ArrayList<>();
        for (Equipment item : equipment) {
            BoostWriter boostWriter = boostService.text();
            StatsArchive.Stat stat = libraryService.archive().stats.getByName(item.id());
            boostService.stat(stat, boostWriter);
            String boost = boostWriter.toString();
            Metadata metadata = Metadata.from(Map.of(
                    "id", item.id(),
                    "type", item.type().name(),
                    "slot", item.slot().name(),
                    "rarity", item.rarity().name()));
            if (item.weaponType() != null) {
                metadata.put("weaponType", item.weaponType());
            }
            if (item.armorType() != null) {
                metadata.put("armorType", item.armorType());
            }
            String content = "Name: " + item.name() + "\n" +
                    "Type: " + item.type() + "\n" +
                    "Slot: " + item.slot() + "\n" +
                    "Rarity: " + item.rarity() + "\n";
            if (item.weaponType() != null) {
                content += "Weapon Type: " + item.weaponType() + "\n";
            }
            if (item.armorType() != null) {
                content += "Armor Type: " + item.armorType() + "\n";
            }
            if (item.weaponProperties() != null) {
                content += "Weapon Properties: " + item.weaponProperties() + "\n";
            }
            content += "Boosts: " + boost;
            // Log.info("\nid: " + item.id() + "\n" + content);
            Document document = Document.from(content, metadata);
            docs.add(document);
        }
        ingester.ingest(docs);

        Log.info("Ingested " + equipment.size() + " items");
    }

    public void uploadMod(Path pak) throws Exception {
        ArchiveSource source = libraryService.uploadMod(pak);
        Log.info("Importing mod " + source.name);
        Map<String, Equipment> newEquipment = new HashMap<>();
        Map<String, StatsArchive.Stat> armors = source.archive.stats.getByType(EquipmentType.Armor.name());
        for (StatsArchive.Stat armor : armors.values()) {
            addEquipment(newEquipment, armor);
        }
        Map<String, StatsArchive.Stat> weapons = source.archive.stats.getByType(EquipmentType.Weapon.name());
        for (StatsArchive.Stat weapon : weapons.values()) {
            addEquipment(newEquipment, weapon);
        }
        Log.info("Added " + newEquipment.size() + " to equipment database");
        equipmentDB.putAll(newEquipment);
        // TODO: This doesn't seem to work. Older ingestions seem to disappear.
        // instead re-ingest everything
        ingest(newEquipment.values());
        // load();
    }

    public EquipmentModel findByName(String name) {
        Log.infof("Finding by name: %s", name);
        Equipment equipment = equipmentDB.values().stream().filter(e -> e.name().equals(name)).findFirst().orElse(null);
        if (equipment == null) {
            return null;
        }
        Log.infof("Found: %s", equipment.name());
        return EquipmentModel.from(equipment);
    }

    public static record SearchResult(List<EquipmentModel> items, String summary) {
    }

    public List<EquipmentModel> ragSearch(String queryString) {
        Filter filter = new MetadataFilterBuilder("type").isNotEqualTo("Spell");
        try {
            EquipmentType type = metadataAgent.equipmentType(queryString);
            if (type != null && type != EquipmentType.All) {
                Log.infof("Filter Type: %s", type);
                Filter typeFilter = new MetadataFilterBuilder("type").isEqualTo(type.name());
                if (filter == null) {
                    filter = typeFilter;
                } else {
                    filter = filter.and(typeFilter);
                }
                try {
                    EquipmentSlot slot = metadataAgent.equipmentSlot(queryString);
                    if (slot != null && slot != EquipmentSlot.Unknown) {
                        Log.infof("Filter Slot: %s", slot);
                        Filter slotFilter = new MetadataFilterBuilder("slot").isEqualTo(slot.name());
                        filter =filter.and(slotFilter);
                    }
                } catch (Exception e) {
                    Log.warnf("Error getting equipment slot metadata from prompt query: %s", e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.warnf("Error getting equipment type metadata from prompt query: %s", e.getMessage());
        }

        List<EquipmentModel> models = embeddingSearchRequest(queryString, filter);
        return models;
    }

    public List<EquipmentModel> embeddingSearchRequest(String queryString, Filter filter) {
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
        List<Equipment> result = search.matches().stream().map(m -> {
            String id = m.embedded().metadata().getString("id");
            return equipmentDB.get(id);
        }).toList();

        return result.stream().map(EquipmentModel::from).toList();

    }

    /**
     * Utility method to find capital letters in a string
     * 
     * @param str the input string
     * @return a list of capital letters found in the string
     */
    public static List<Character> findCapitalLetters(String str) {
        List<Character> capitals = new ArrayList<>();
        if (str == null) {
            return capitals;
        }

        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                capitals.add(c);
            }
        }
        return capitals;
    }

    /**
     * Alternative method to find capital letters using regex
     * 
     * @param str the input string
     * @return a list of capital letters found in the string
     */
    public static List<Character> findCapitalLettersRegex(String str) {
        List<Character> capitals = new ArrayList<>();
        if (str == null) {
            return capitals;
        }

        // Using regex to find capital letters
        String capitalPattern = "[A-Z]";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(capitalPattern);
        java.util.regex.Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            capitals.add(matcher.group().charAt(0));
        }
        return capitals;
    }

    /**
     * Method to get positions of capital letters in a string
     * 
     * @param str the input string
     * @return a map of character to list of positions where it appears
     */
    public static Map<Character, List<Integer>> findCapitalLetterPositions(String str) {
        Map<Character, List<Integer>> positions = new HashMap<>();
        if (str == null) {
            return positions;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                positions.computeIfAbsent(c, k -> new ArrayList<>()).add(i);
            }
        }
        return positions;
    }

    /**
     * Adds spaces between capital letters in a string
     * 
     * @param str the input string
     * @return the string with spaces added between capital letters
     */
    public static String addSpacesBetweenCapitals(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                // Add space before capital letter (except at the beginning)
                result.append(' ');
            }
            result.append(c);
        }
        return result.toString();
    }

}
