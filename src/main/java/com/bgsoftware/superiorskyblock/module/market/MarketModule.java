package com.bgsoftware.superiorskyblock.module.market;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MarketModule extends BuiltinModule<MarketModule.Configuration> {

    private static MarketModule instance;
    private File dataFile;
    private YamlConfiguration dataConfig;
    private MarketTask marketTask;

    public MarketModule() {
        super("market");
        instance = this;
    }

    public static MarketModule get() {
        return instance;
    }

    @Override
    protected void onEnable(SuperiorSkyblockPlugin plugin) {
        File marketFolder = new File(plugin.getDataFolder(), "modules/market");
        if (!marketFolder.exists()) {
            marketFolder.mkdirs();
        }
        dataFile = new File(marketFolder, "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ignored) {}
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load pool sizes from dataConfig into Configuration
        for (Map.Entry<String, MarketItemInfo> entry : getConfiguration().getItems().entrySet()) {
            int poolSize = dataConfig.getInt("pool." + entry.getKey(), 0);
            entry.getValue().setPoolSize(poolSize);
        }

        marketTask = new MarketTask(this);
        // Run every hour (72000 ticks)
        marketTask.runTaskTimerAsynchronously(plugin, 72000L, 72000L);
    }

    @Override
    protected void onDisable(SuperiorSkyblockPlugin plugin) {
        if (marketTask != null) {
            marketTask.cancel();
        }
        saveData();
    }

    public void saveData() {
        if (dataConfig != null && dataFile != null) {
            for (Map.Entry<String, MarketItemInfo> entry : getConfiguration().getItems().entrySet()) {
                dataConfig.set("pool." + entry.getKey(), entry.getValue().getPoolSize());
            }
            try {
                dataConfig.save(dataFile);
            } catch (IOException ignored) {}
        }
    }

    @Override
    protected void loadData(SuperiorSkyblockPlugin plugin) {
    }

@Override
    protected Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return new Listener[0];
    }

    @Override
    protected SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdMarket(this)};
    }

    @Override
    protected SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdAdminMarket(this)};
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration configuration) {
        return new Configuration(configuration);
    }

    public static class Configuration implements IModuleConfiguration {
        @Override
        public boolean isEnabled() { return true; }
        private final Map<String, MarketItemInfo> items = new LinkedHashMap<>();

        public Configuration(CommentedConfiguration config) {
            org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection("items");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    double basePrice = section.getDouble(key + ".base-price");
                    double minPrice = section.getDouble(key + ".min-price");
                    double maxPrice = section.getDouble(key + ".max-price");
                    double dropRate = section.getDouble(key + ".drop-rate");
                    int recoveryRate = section.getInt(key + ".recovery-rate");
                    items.put(key, new MarketItemInfo(key, basePrice, minPrice, maxPrice, dropRate, recoveryRate));
                }
            }
        }

        public Map<String, MarketItemInfo> getItems() {
            return items;
        }

        @Override
        public void removeInvalidBlocks() {}
    }

    public static class MarketItemInfo {
        private final String materialName;
        private final double basePrice;
        private final double minPrice;
        private final double maxPrice;
        private final double dropRate;
        private final int recoveryRate;
        private int poolSize = 0;

        public MarketItemInfo(String materialName, double basePrice, double minPrice, double maxPrice, double dropRate, int recoveryRate) {
            this.materialName = materialName;
            this.basePrice = basePrice;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.dropRate = dropRate;
            this.recoveryRate = recoveryRate;
        }

        public Material getMaterial() {
            return Material.matchMaterial(materialName);
        }

        public double getBasePrice() { return basePrice; }
        public double getMinPrice() { return minPrice; }
        public double getMaxPrice() { return maxPrice; }
        public int getRecoveryRate() { return recoveryRate; }

        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }
        public void addPoolSize(int amount) { this.poolSize = this.poolSize + amount; }

        public double getCurrentPrice() {
            // If poolSize > 0 (many sold), price drops. If poolSize < 0 (can we have negative pool?), let's allow negative pool for price increase!
            // Wait, setPoolSize uses Math.max(0). Let's allow negative pool so price can go up!
            // Actually, if we want price to go up to maxPrice, poolSize should be allowed to go negative.
            // Let's remove Math.max(0) from setPoolSize.
            
            double current = basePrice - (poolSize * dropRate);
            if (current < minPrice) return minPrice;
            if (current > maxPrice) return maxPrice;
            return current;
        }
    }

    public static String getVietnameseName(Material mat) {
        switch (mat.name()) {
            case "DIAMOND_BLOCK": return "Kh\u1ed1i Kim C\u01b0\u01a1ng";
            case "IRON_BLOCK": return "Kh\u1ed1i S\u1eaft";
            case "GOLD_BLOCK": return "Kh\u1ed1i V\u00e0ng";
            case "EMERALD_BLOCK": return "Kh\u1ed1i Ng\u1ecdc L\u1ee5c B\u1ea3o";
            case "COAL_BLOCK": return "Kh\u1ed1i Than";
            case "REDSTONE_BLOCK": return "Kh\u1ed1i \u0110\u00e1 \u0110\u1ecf";
            case "LAPIS_BLOCK": return "Kh\u1ed1i L\u01b0u Ly";
            case "NETHERITE_BLOCK": return "Kh\u1ed1i Netherite";
            case "HAY_BLOCK": return "Kh\u1ed1i R\u01a1m";
            case "MELON": return "D\u01b0a H\u1ea5u";
            case "PUMPKIN": return "B\u00ed Ng\u00f4";
            case "WHEAT": return "L\u00faa M\u00ec";
            case "CARROT": return "C\u00e0 R\u1ed1t";
            case "POTATO": return "Khoai T\u00e2y";
            case "SUGAR_CANE": return "M\u00eda";
            case "COBBLESTONE": return "\u0110\u00e1 Cu\u1ed9i";
            case "STONE": return "\u0110\u00e1";
            case "IRON_INGOT": return "Ph\u00f4i S\u1eaft";
            case "GOLD_INGOT": return "Ph\u00f4i V\u00e0ng";
            case "DIAMOND": return "Kim C\u01b0\u01a1ng";
            case "EMERALD": return "Ng\u1ecdc L\u1ee5c B\u1ea3o";
            case "COAL": return "Than";
            default:
                String[] parts = mat.name().toLowerCase().split("_");
                StringBuilder sb = new StringBuilder();
                for (String p : parts) {
                    if (p.length() > 0) {
                        sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
                    }
                }
                return sb.toString().trim();
        }
    }
}
