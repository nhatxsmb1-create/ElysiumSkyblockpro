package com.bgsoftware.superiorskyblock.module.orestorage;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageManager {

    private final File dataFile;
    // Map of IslandUUID -> (Material -> Amount)
    private final Map<UUID, Map<Material, BigInteger>> storageMap = new HashMap<>();

    public StorageManager(File moduleFolder) {
        if (!moduleFolder.exists()) {
            moduleFolder.mkdirs();
        }
        this.dataFile = new File(moduleFolder, "data.yml");
    }

    public void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        
        for (String islandIdStr : cfg.getKeys(false)) {
            try {
                UUID islandId = UUID.fromString(islandIdStr);
                Map<Material, BigInteger> islandData = new HashMap<>();
                
                for (String matName : cfg.getConfigurationSection(islandIdStr).getKeys(false)) {
                    Material mat = Material.matchMaterial(matName);
                    if (mat != null) {
                        String amountStr = cfg.getString(islandIdStr + "." + matName);
                        try {
                            islandData.put(mat, new BigInteger(amountStr));
                        } catch (Exception ignored) {}
                    }
                }
                storageMap.put(islandId, islandData);
            } catch (IllegalArgumentException ignored) { }
        }
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Map<Material, BigInteger>> entry : storageMap.entrySet()) {
            String islandId = entry.getKey().toString();
            for (Map.Entry<Material, BigInteger> matEntry : entry.getValue().entrySet()) {
                cfg.set(islandId + "." + matEntry.getKey().name(), matEntry.getValue().toString());
            }
        }
        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BigInteger getAmount(UUID islandId, Material material) {
        return storageMap.getOrDefault(islandId, new HashMap<>()).getOrDefault(material, BigInteger.ZERO);
    }

    public void addAmount(UUID islandId, Material material, BigInteger amount) {
        storageMap.putIfAbsent(islandId, new HashMap<>());
        Map<Material, BigInteger> islandData = storageMap.get(islandId);
        islandData.put(material, islandData.getOrDefault(material, BigInteger.ZERO).add(amount));
    }

    public void removeAmount(UUID islandId, Material material, BigInteger amount) {
        storageMap.putIfAbsent(islandId, new HashMap<>());
        Map<Material, BigInteger> islandData = storageMap.get(islandId);
        BigInteger current = islandData.getOrDefault(material, BigInteger.ZERO);
        BigInteger newValue = current.subtract(amount);
        if (newValue.compareTo(BigInteger.ZERO) < 0) {
            newValue = BigInteger.ZERO;
        }
        islandData.put(material, newValue);
    }
}
