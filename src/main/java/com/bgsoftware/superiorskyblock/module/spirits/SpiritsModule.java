package com.bgsoftware.superiorskyblock.module.spirits;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.BuiltinModule;
import com.bgsoftware.superiorskyblock.api.modules.IModuleConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public SpiritManager getSpiritManager() {
        return spiritManager;
    }

    public static SpiritsModule get() {
        return instance;
    }

    public static class SpiritConfigInfo {
        private final String type;
        private final String name;
        private final String texture;
        private final String particle;
        private final int actionIntervalTicks;
        private final int actionRadius;

        public SpiritConfigInfo(String type, String name, String texture, String particle, int actionIntervalTicks, int actionRadius) {
            this.type = type;
            this.name = name;
            this.texture = texture;
            this.particle = particle;
            this.actionIntervalTicks = actionIntervalTicks;
            this.actionRadius = actionRadius;
        }

        public String getType() { return type; }
        public String getName() { return name; }
        public String getTexture() { return texture; }
        public String getParticle() { return particle; }
        public int getActionIntervalTicks() { return actionIntervalTicks; }
        public int getActionRadius() { return actionRadius; }
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
                    spirits.put(key.toLowerCase(), new SpiritConfigInfo(
                            key.toLowerCase(),
                            sec.getString(path + "name", "&bTinh Linh"),
                            sec.getString(path + "texture", ""),
                            sec.getString(path + "particle", "HAPPY_VILLAGER"),
                            sec.getInt(path + "interval-ticks", 40),
                            sec.getInt(path + "radius", 3)
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
