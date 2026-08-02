package com.bgsoftware.superiorskyblock.module.worldevents.data;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores and persists instability values (0–100) per island UUID.
 */
public class InstabilityManager {

    private final File dataFile;
    private final Map<UUID, Integer> instabilityMap = new HashMap<>();

    public InstabilityManager(File moduleFolder) {
        this.dataFile = new File(moduleFolder, "instability.yml");
    }

    public void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : cfg.getKeys(false)) {
            try {
                instabilityMap.put(UUID.fromString(key), cfg.getInt(key, 0));
            } catch (IllegalArgumentException ignored) { }
        }
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Integer> entry : instabilityMap.entrySet()) {
            cfg.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Returns instability 0–100 for the given island. */
    public int getInstability(UUID islandId) {
        return instabilityMap.getOrDefault(islandId, 0);
    }

    /** Adds delta (positive or negative), clamped to 0–100. Returns new value. */
    public int addInstability(UUID islandId, int delta) {
        int current = instabilityMap.getOrDefault(islandId, 0);
        int updated = Math.max(0, Math.min(100, current + delta));
        instabilityMap.put(islandId, updated);
        return updated;
    }

    /** Hard-sets instability, clamped to 0–100. */
    public int setInstability(UUID islandId, int value) {
        int clamped = Math.max(0, Math.min(100, value));
        instabilityMap.put(islandId, clamped);
        return clamped;
    }
}
