package com.bgsoftware.superiorskyblock.module.spirits;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import org.bukkit.Material;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpiritsModule extends BuiltinModule<SpiritsModule.Configuration> {

    private static SpiritsModule instance;
    private SpiritManager spiritManager;
    private SpiritTask spiritTask;

    public SpiritsModule() {
        super("spirits");
        instance = this;
    }

    @Override
    protected void onEnable(SuperiorSkyblockPlugin plugin) {
        this.spiritManager = new SpiritManager(plugin, this);
        this.spiritManager.load();
        this.spiritTask = new SpiritTask(plugin, this);
        this.spiritTask.runTaskTimer(plugin, 20L, 20L);
    }

    @Override
    protected void onDisable(SuperiorSkyblockPlugin plugin) {
        if (this.spiritTask != null) {
            this.spiritTask.cancel();
        }
        if (this.spiritManager != null) {
            this.spiritManager.save();
        }
    }

    @Override
    protected void loadData(SuperiorSkyblockPlugin plugin) {
    }

    @Override
    protected Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return new Listener[]{new SpiritsListener(plugin, this)};
    }

    @Override
    protected SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdSpirits(this)};
    }

    @Override
    protected SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdAdminSpirit(this)};
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration config) {
        return new Configuration(config);
    }

    @Override
    protected String[] getIgnoredSections() {
        return new String[]{"spirits"};
    }

    public SpiritTask getSpiritTask() {
        return spiritTask;
    }

    public SpiritManager getSpiritManager() {
        return spiritManager;
    }

    public static SpiritsModule get() {
        return instance;
    }

    public static class SpiritUpgradeInfo {
        private final int level;
        private final int intervalTicks;
        private final Map<Material, Integer> cost;

        public SpiritUpgradeInfo(int level, int intervalTicks, Map<Material, Integer> cost) {
            this.level = level;
            this.intervalTicks = intervalTicks;
            this.cost = cost;
        }

        public int getLevel() { return level; }
        public int getIntervalTicks() { return intervalTicks; }
        public Map<Material, Integer> getCost() { return cost; }
    }

    public static class SpiritConfigInfo {
        private final String type;
        private final String name;
        private final String texture;
        private final String particle;
        private final List<String> description;
        private final int actionIntervalTicks;
        private final int actionRadius;
        private final Map<Integer, SpiritUpgradeInfo> upgrades;

        public SpiritConfigInfo(String type, String name, String texture, String particle, int actionIntervalTicks, int actionRadius, List<String> description, Map<Integer, SpiritUpgradeInfo> upgrades) {
            this.type = type;
            this.name = name;
            this.texture = texture;
            this.particle = particle;
            this.description = description;
            this.actionIntervalTicks = actionIntervalTicks;
            this.actionRadius = actionRadius;
            this.upgrades = upgrades;
        }

        public String getType() { return type; }
        public String getName() { return name; }
        public String getTexture() { return texture; }
        public String getParticle() { return particle; }
        public List<String> getDescription() { return description; }
        public int getActionIntervalTicks() { return actionIntervalTicks; }
        public int getActionRadius() { return actionRadius; }
        public Map<Integer, SpiritUpgradeInfo> getUpgrades() { return upgrades; }
        
        public int getMaxLevel() {
            int max = 1;
            for (Integer lvl : upgrades.keySet()) {
                if (lvl > max) max = lvl;
            }
            return max;
        }
    }

    public static class Configuration implements IModuleConfiguration {

        private final boolean enabled;
        private final Map<String, SpiritConfigInfo> spirits = new LinkedHashMap<>();

        Configuration(CommentedConfiguration config) {
            this.enabled = config.getBoolean("enabled", true);

            org.bukkit.configuration.ConfigurationSection sec = config.getConfigurationSection("spirits");
            if (sec != null) {
                for (String key : sec.getKeys(false)) {
                    String path = key + ".";
                    
                    Map<Integer, SpiritUpgradeInfo> upgrades = new HashMap<>();
                    org.bukkit.configuration.ConfigurationSection upSec = config.getConfigurationSection("spirits." + key + ".upgrades");
                    if (upSec != null) {
                        for (String lvlKey : upSec.getKeys(false)) {
                            try {
                                int level = Integer.parseInt(lvlKey);
                                int interval = upSec.getInt(lvlKey + ".interval-ticks", 40);
                                Map<Material, Integer> cost = new HashMap<>();
                                org.bukkit.configuration.ConfigurationSection costSec = upSec.getConfigurationSection(lvlKey + ".cost");
                                if (costSec != null) {
                                    for (String matKey : costSec.getKeys(false)) {
                                        try {
                                            Material mat = Material.matchMaterial(matKey);
                                            if (mat != null) {
                                                cost.put(mat, costSec.getInt(matKey));
                                            }
                                        } catch (Exception ignored) {}
                                    }
                                }
                                upgrades.put(level, new SpiritUpgradeInfo(level, interval, cost));
                            } catch (Exception ignored) {}
                        }
                    }

                    spirits.put(key.toLowerCase(), new SpiritConfigInfo(
                            key.toLowerCase(),
                            sec.getString(path + "name", "&bTinh Linh"),
                            sec.getString(path + "texture", ""),
                            sec.getString(path + "particle", "HAPPY_VILLAGER"),
                            sec.getInt(path + "interval-ticks", 40),
                            sec.getInt(path + "radius", 3),
                            sec.getStringList(path + "description"),
                            upgrades
                    ));
                }
            }
        }

        public Map<String, SpiritConfigInfo> getSpirits() {
            return spirits;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
