package com.bgsoftware.superiorskyblock.module.trophies;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TrophiesModule extends BuiltinModule<TrophiesModule.Configuration> {

    private static TrophiesModule instance;

    private TrophyManager trophyManager;
    private BukkitRunnable potionTask;

    public TrophiesModule() {
        super("trophies");
        instance = this;
    }

    @Override
    protected void onEnable(SuperiorSkyblockPlugin plugin) {
        this.trophyManager = new TrophyManager(this);
        this.trophyManager.load();

        this.potionTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Island island = plugin.getGrid().getIslandAt(player.getLocation());
                    if (island == null)
                        continue;

                    for (PotionEffect effect : trophyManager.getEffectsForIsland(island))
                        player.addPotionEffect(effect);
                }
            }
        };
        this.potionTask.runTaskTimer(plugin, 20L, 160L);
    }

    @Override
    protected void onDisable(SuperiorSkyblockPlugin plugin) {
        if (this.potionTask != null) {
            this.potionTask.cancel();
            this.potionTask = null;
        }
    }

    @Override
    protected void loadData(SuperiorSkyblockPlugin plugin) {
    }

    @Override
    protected Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return new Listener[]{new com.bgsoftware.superiorskyblock.module.trophies.listeners.TrophyListener(plugin, this)};
    }

    @Override
    protected SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdTrophies(this)};
    }

    @Override
    protected SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdAdminTrophy(this)};
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration config) {
        return new Configuration(config);
    }

    @Override
    protected String[] getIgnoredSections() {
        // Never wipe custom trophy entries added by the server owner
        return new String[]{"trophies"};
    }

    public TrophyManager getTrophyManager() {
        return trophyManager;
    }

    public static TrophiesModule get() {
        return instance;
    }

    /**
     * Bonus multiplier for island crop growth / mob drops based on the
     * placed trophy collection. Returns 1.0 when the module is disabled
     * or not yet initialized.
     */
    public static double getBonusMultiplier(Island island, String bonusKey) {
        try {
            TrophiesModule module = instance;
            if (module == null || !module.isEnabled() || module.trophyManager == null)
                return 1.0;

            int distinctTrophies = module.trophyManager.getPlacedTrophyCount(island);
            TreeMap<Integer, Double> tiers = "crop-growth".equals(bonusKey) ?
                    module.getConfiguration().getCropGrowthTiers() : module.getConfiguration().getMobDropsTiers();

            double bonus = 0.0;
            for (Map.Entry<Integer, Double> entry : tiers.entrySet()) {
                if (distinctTrophies >= entry.getKey())
                    bonus = Math.max(bonus, entry.getValue());
            }

            return 1.0 + bonus;
        } catch (Exception error) {
            return 1.0;
        }
    }

    public static class TrophyInfo {
        private final String id;
        private final String name;
        private final String material;
        private final String texture;

        TrophyInfo(String id, String name, String material, String texture) {
            this.id = id;
            this.name = name;
            this.material = material;
            this.texture = texture;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getMaterial() {
            return material;
        }

        public String getTexture() {
            return texture;
        }
    }

    public static class Configuration implements IModuleConfiguration {

        private final boolean enabled;
        private final double dropChance;
        private final Map<String, TrophyInfo> trophies = new LinkedHashMap<>();
        private final TreeMap<Integer, List<PotionEffect>> effectTiers = new TreeMap<>();
        private final TreeMap<Integer, Double> cropGrowthTiers = new TreeMap<>();
        private final TreeMap<Integer, Double> mobDropsTiers = new TreeMap<>();

        Configuration(CommentedConfiguration config) {
            this.enabled = config.getBoolean("enabled", true);
            this.dropChance = config.getDouble("drop-chance", 35.0);

            org.bukkit.configuration.ConfigurationSection trophiesSection = config.getConfigurationSection("trophies");
            if (trophiesSection != null) {
                for (String id : trophiesSection.getKeys(false)) {
                    trophies.put(id.toLowerCase(), new TrophyInfo(
                            id.toLowerCase(),
                            trophiesSection.getString(id + ".name", id),
                            trophiesSection.getString(id + ".material", "PLAYER_HEAD"),
                            trophiesSection.getString(id + ".texture", "")));
                }
            }

            org.bukkit.configuration.ConfigurationSection effectsSection = config.getConfigurationSection("potions");
            if (effectsSection != null) {
                for (String key : effectsSection.getKeys(false)) {
                    try {
                        int threshold = Integer.parseInt(key);
                        List<PotionEffect> effects = new ArrayList<>();
                        for (String raw : effectsSection.getStringList(key)) {
                            PotionEffect effect = parseEffect(raw);
                            if (effect != null)
                                effects.add(effect);
                        }
                        if (!effects.isEmpty())
                            this.effectTiers.put(threshold, effects);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            org.bukkit.configuration.ConfigurationSection cropSection = config.getConfigurationSection("bonuses.crop-growth");
            if (cropSection != null) {
                for (String key : cropSection.getKeys(false)) {
                    try {
                        this.cropGrowthTiers.put(Integer.parseInt(key), cropSection.getDouble(key));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            org.bukkit.configuration.ConfigurationSection mobSection = config.getConfigurationSection("bonuses.mob-drops");
            if (mobSection != null) {
                for (String key : mobSection.getKeys(false)) {
                    try {
                        this.mobDropsTiers.put(Integer.parseInt(key), mobSection.getDouble(key));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        public double getDropChance() {
            return dropChance;
        }

        public Map<String, TrophyInfo> getTrophies() {
            return trophies;
        }

        public TreeMap<Integer, List<PotionEffect>> getEffectTiers() {
            return effectTiers;
        }

        public TreeMap<Integer, Double> getCropGrowthTiers() {
            return cropGrowthTiers;
        }

        public TreeMap<Integer, Double> getMobDropsTiers() {
            return mobDropsTiers;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        private static PotionEffect parseEffect(String raw) {
            // Format: EFFECT_NAME:level
            String[] parts = raw.split(":");
            PotionEffectType type = null;
            for (String alias : effectAliases(parts[0])) {
                try {
                    type = PotionEffectType.getByName(alias);
                } catch (Exception error) {
                    type = null;
                }
                if (type != null)
                    break;
            }
            if (type == null)
                return null;
            int amplifier = parts.length > 1 ? Integer.parseInt(parts[1]) - 1 : 0;
            // 20 seconds so the 8-second refresh cycle never lets it flicker
            return new PotionEffect(type, 20 * 20, Math.max(0, amplifier), true, false);
        }

        /**
         * Effect names changed between versions - resolve both spellings.
         */
        private static String[] effectAliases(String name) {
            switch (name.toUpperCase(java.util.Locale.ENGLISH)) {
                case "FAST_DIGGING":
                case "HASTE":
                    return new String[]{"FAST_DIGGING", "HASTE"};
                case "SLOW":
                case "SLOWNESS":
                    return new String[]{"SLOW", "SLOWNESS"};
                default:
                    return new String[]{name};
            }
        }

    }

}
