package org.baldurs.forge.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;

@ApplicationScoped
public class InMemoryEmbeddingStoreProducer {
    protected InMemoryEmbeddingStore<TextSegment> store;
    @Inject
    @ConfigProperty(name = "baldurs.forge.data.path", defaultValue = "/home/bburke/projects/baldurs-forge-data")
    private String rootPath;

    private String cachePath;

    private boolean cached;

    @Inject
    LibraryService library;

    @PostConstruct
    public void init() {
        cachePath = rootPath + "/cache";
    }

    @Startup(EquipmentDB.PRIORITY + 1)
    public void start() {
        cached = load();
        Log.info("Embedding store loaded: " + cached);
    }

    public boolean isCached() {
        return cached;
    }

    private boolean load() {
        store = new InMemoryEmbeddingStore<>();
        Path cacheDir = Path.of(cachePath);

        if (!Files.exists(cacheDir)) {
            return false;
        }
        Path cacheFile = cacheDir.resolve("embedding-store.json");
        if (!Files.exists(cacheFile)) {
            return false;
        }
        try {
            long lastCache = library.lastCacheUpdate();
            long lastFile = Files.getLastModifiedTime(cacheFile).toMillis();
            Log.debugv("Last cache update: {0}, last file: {1}", lastCache, lastFile);
            if (lastCache > lastFile) {
                Files.delete(cacheFile);
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        store = InMemoryEmbeddingStore.fromFile(cacheFile);
        return true;
    }

    public void save() {
        Path cacheDir = Path.of(cachePath);

        if (!Files.exists(cacheDir)) {
            try {
                Files.createDirectories(cacheDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Path cacheFile = cacheDir.resolve("embedding-store.json");
        store.serializeToFile(cacheFile);
    }

    public InMemoryEmbeddingStore<TextSegment> getStore() {
        return store;
    }
}
