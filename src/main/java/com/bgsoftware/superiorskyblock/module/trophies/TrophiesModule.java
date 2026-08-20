package com.bgsoftware.superiorskyblock.module.trophies;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.modules.BuiltinModule;
import com.bgsoftware.superiorskyblock.api.modules.IModuleConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class TrophiesModule extends BuiltinModule<TrophiesModule.Configuration> {

    private static TrophiesModule instance;

    private TrophyManager trophyManager;
    private BukkitRunnable potionTask;
    private BukkitRunnable particleTask;

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

        this.particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                Set<Island> islandsWithPlayers = new HashSet<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Island island = plugin.getGrid().getIslandAt(player.getLocation());
                    if (island != null) {
                        islandsWithPlayers.add(island);
                    }
                }
                
                for (Island island : islandsWithPlayers) {
                    Map<Location, String> trophies = trophyManager.getPlacedTrophyLocations(island);
                    for (Map.Entry<Location, String> entry : trophies.entrySet()) {
                        Location loc = entry.getKey().clone().add(0.5, 0.8, 0.5);
                        String id = entry.getValue();
                        TrophiesModule.TrophyInfo info = trophyManager.getTrophies().get(id);
                        if (info != null && info.getParticle() != null && !info.getParticle().isEmpty()) {
                            spawnParticle(loc, info.getParticle());
                        }
                    }
                }
            }
        };
        // Run particles twice a second
        this.particleTask.runTaskTimer(plugin, 20L, 10L);
    }

    @Override
    protected void onDisable(SuperiorSkyblockPlugin plugin) {
        if (this.potionTask != null) {
            this.potionTask.cancel();
            this.potionTask = null;
        }
        if (this.particleTask != null) {
            this.particleTask.cancel();
            this.particleTask = null;
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
        return new String[]{"trophies"};
    }

    public TrophyManager getTrophyManager() {
        return trophyManager;
    }

    public static TrophiesModule get() {
        return instance;
    }

    public static double getBonusMultiplier(Island island, String bonusKey) {
        try {
            TrophiesModule module = instance;
            if (module == null || !module.isEnabled() || module.trophyManager == null)
                return 1.0;

            double bonus = 0.0;
            for (String trophyId : module.trophyManager.getPlacedTrophyIds(island)) {
                TrophyInfo info = module.getConfiguration().getTrophies().get(trophyId);
                if (info != null && info.getBonuses().containsKey(bonusKey)) {
                    bonus += info.getBonuses().get(bonusKey);
                }
            }

            return 1.0 + bonus;
        } catch (Exception error) {
            return 1.0;
        }
    }

    public static void spawnParticle(Location loc, String particleName) {
        if (particleName.equalsIgnoreCase("NONE")) return;
        try {
            Object particleEnum = Class.forName("org.bukkit.Particle").getMethod("valueOf", String.class).invoke(null, particleName.toUpperCase());
            // Double extra parameter is speed
            loc.getWorld().getClass().getMethod("spawnParticle", Class.forName("org.bukkit.Particle"), Location.class, int.class, double.class, double.class, double.class, double.class)
                 .invoke(loc.getWorld(), particleEnum, loc, 3, 0.3, 0.3, 0.3, 0.02);
        } catch (Exception e) {
            try {
                Object effectEnum = Class.forName("org.bukkit.Effect").getMethod("valueOf", String.class).invoke(null, particleName.toUpperCase());
                loc.getWorld().getClass().getMethod("playEffect", Location.class, Class.forName("org.bukkit.Effect"), int.class)
                     .invoke(loc.getWorld(), loc, effectEnum, 0);
            } catch (Exception ignored) {}
        }
    }

    public static class TrophyInfo {
        private final String id;
        private final String name;
        private final String material;
        private final String texture;
        private final String particle;
        private final List<PotionEffect> potions = new ArrayList<>();
        private final Map<String, Double> bonuses = new HashMap<>();

        TrophyInfo(String id, String name, String material, String texture, String particle, List<PotionEffect> potions, Map<String, Double> bonuses) {
            this.id = id;
            this.name = name;
            this.material = material;
            this.texture = texture;
            this.particle = particle;
            if (potions != null) this.potions.addAll(potions);
            if (bonuses != null) this.bonuses.putAll(bonuses);
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getMaterial() { return material; }
        public String getTexture() { return texture; }
        public String getParticle() { return particle; }
        public List<PotionEffect> getPotions() { return potions; }
        public Map<String, Double> getBonuses() { return bonuses; }
    }

    public static class Configuration implements IModuleConfiguration {

        private final boolean enabled;
        private final double dropChance;
        private final Map<String, TrophyInfo> trophies = new LinkedHashMap<>();

        Configuration(CommentedConfiguration config) {
            this.enabled = config.getBoolean("enabled", true);
            this.dropChance = config.getDouble("drop-chance", 35.0);

            org.bukkit.configuration.ConfigurationSection trophiesSection = config.getConfigurationSection("trophies");
            if (trophiesSection != null) {
                for (String id : trophiesSection.getKeys(false)) {
                    String path = id + ".";
                    
                    List<PotionEffect> effects = new ArrayList<>();
                    if (trophiesSection.isList(path + "buffs.potions")) {
                        for (String raw : trophiesSection.getStringList(path + "buffs.potions")) {
                            PotionEffect effect = parseEffect(raw);
                            if (effect != null)
                                effects.add(effect);
                        }
                    }
                    
                    Map<String, Double> bonuses = new HashMap<>();
                    org.bukkit.configuration.ConfigurationSection bonusSection = trophiesSection.getConfigurationSection(path + "buffs.bonuses");
                    if (bonusSection != null) {
                        for (String key : bonusSection.getKeys(false)) {
                            bonuses.put(key, bonusSection.getDouble(key));
                        }
                    }

                    trophies.put(id.toLowerCase(), new TrophyInfo(
                            id.toLowerCase(),
                            trophiesSection.getString(path + "name", id),
                            trophiesSection.getString(path + "material", "PLAYER_HEAD"),
                            trophiesSection.getString(path + "texture", ""),
                            trophiesSection.getString(path + "particle", "PORTAL"),
                            effects,
                            bonuses));
                }
            }
        }

        public double getDropChance() { return dropChance; }
        public Map<String, TrophyInfo> getTrophies() { return trophies; }
        @Override public boolean isEnabled() { return enabled; }

        private static PotionEffect parseEffect(String raw) {
            String[] parts = raw.split(":");
            PotionEffectType type = null;
            for (String alias : effectAliases(parts[0])) {
                try {
                    type = PotionEffectType.getByName(alias);
                } catch (Exception error) { type = null; }
                if (type != null) break;
            }
            if (type == null) return null;
            int amplifier = parts.length > 1 ? Integer.parseInt(parts[1]) - 1 : 0;
            return new PotionEffect(type, 20 * 20, Math.max(0, amplifier), true, false);
        }

        private static String[] effectAliases(String name) {
            switch (name.toUpperCase(java.util.Locale.ENGLISH)) {
                case "FAST_DIGGING":
                case "HASTE": return new String[]{"FAST_DIGGING", "HASTE"};
                case "SLOW":
                case "SLOWNESS": return new String[]{"SLOW", "SLOWNESS"};
                case "JUMP":
                case "LEAPING": return new String[]{"JUMP", "LEAPING"};
                case "REGENERATION":
                case "REGEN": return new String[]{"REGENERATION", "REGEN"};
                case "DAMAGE_RESISTANCE":
                case "RESISTANCE": return new String[]{"DAMAGE_RESISTANCE", "RESISTANCE"};
                case "INCREASE_DAMAGE":
                case "STRENGTH": return new String[]{"INCREASE_DAMAGE", "STRENGTH"};
                default: return new String[]{name};
            }
        }
    }
}
