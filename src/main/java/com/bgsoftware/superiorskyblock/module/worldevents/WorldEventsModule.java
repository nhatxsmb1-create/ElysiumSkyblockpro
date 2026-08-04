package com.bgsoftware.superiorskyblock.module.worldevents;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import com.bgsoftware.superiorskyblock.module.worldevents.commands.CmdAdminSetInstability;
import com.bgsoftware.superiorskyblock.module.worldevents.commands.CmdAdminTriggerEvent;
import com.bgsoftware.superiorskyblock.module.worldevents.commands.CmdInstability;
import com.bgsoftware.superiorskyblock.module.worldevents.data.InstabilityManager;
import com.bgsoftware.superiorskyblock.module.worldevents.listener.WorldEventsListener;
import org.bukkit.event.Listener;

public class WorldEventsModule extends BuiltinModule<WorldEventsModule.Configuration> {

    private InstabilityManager instabilityManager;
    private WorldEventScheduler scheduler;
    private WorldEventLogger logger;

    public WorldEventsModule() { super("worldevents"); }

    @Override
    protected void onEnable(SuperiorSkyblockPlugin plugin) {
        this.instabilityManager = new InstabilityManager(getModuleFolder());
        this.instabilityManager.load();
        this.logger = new WorldEventLogger(getModuleFolder());
        this.scheduler = new WorldEventScheduler(plugin, this);
        this.scheduler.start();
    }

    @Override
    protected void onDisable(SuperiorSkyblockPlugin plugin) {
        if (this.scheduler != null) this.scheduler.stop();
        if (this.instabilityManager != null) this.instabilityManager.save();
    }

    @Override
    protected void loadData(SuperiorSkyblockPlugin plugin) {}

    @Override
    protected Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return new Listener[]{ new WorldEventsListener(plugin, this) };
    }

    @Override
    protected SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{ new CmdInstability(this) };
    }

    @Override
    protected SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{
                new CmdAdminTriggerEvent(this),
                new CmdAdminSetInstability(this)
        };
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration config) {
        return new Configuration(config);
    }

    public InstabilityManager getInstabilityManager() { return instabilityManager; }
    public WorldEventScheduler getScheduler()         { return scheduler; }
    public WorldEventLogger getLogger()               { return logger; }

    // =========================================================
    public static class Configuration implements IModuleConfiguration {

        private final boolean enabled;
        private final int  checkIntervalSeconds;
        private final int  baseEventChance;
        private final double instabilityChanceBonus;
        private final int  instabilityPerKill;
        private final int  instabilityPerMine;
        private final int  instabilityDecayPerCheck;
        // new
        private final int    islandCooldownSeconds;
        private final double bossHPPerLevel;
        private final int    bonusLootThreshold;
        private final double bonusLootChance;
        private final int    countdownSeconds;
        private final boolean announceRareEvents;

        Configuration(CommentedConfiguration config) {
            enabled                  = config.getBoolean("enabled", true);
            checkIntervalSeconds     = config.getInt("check-interval-seconds", 300);
            baseEventChance          = config.getInt("base-event-chance", 5);
            instabilityChanceBonus   = config.getDouble("instability-chance-bonus", 0.5);
            instabilityPerKill       = config.getInt("instability-per-kill", 2);
            instabilityPerMine       = config.getInt("instability-per-mine", 1);
            instabilityDecayPerCheck = config.getInt("instability-decay-per-check", 3);
            islandCooldownSeconds    = config.getInt("island-cooldown-seconds", 600);
            bossHPPerLevel           = config.getDouble("boss-hp-per-level", 0.001); // +0.1% per level point
            bonusLootThreshold       = config.getInt("bonus-loot-threshold", 70);
            bonusLootChance          = config.getDouble("bonus-loot-chance", 0.5);
            countdownSeconds         = config.getInt("countdown-seconds", 5);
            announceRareEvents       = config.getBoolean("announce-rare-events", true);
        }

        @Override public boolean isEnabled()               { return enabled; }
        public int  getCheckIntervalSeconds()              { return checkIntervalSeconds; }
        public int  getBaseEventChance()                   { return baseEventChance; }
        public double getInstabilityChanceBonus()          { return instabilityChanceBonus; }
        public int  getInstabilityPerKill()                { return instabilityPerKill; }
        public int  getInstabilityPerMine()                { return instabilityPerMine; }
        public int  getInstabilityDecayPerCheck()          { return instabilityDecayPerCheck; }
        public int  getIslandCooldownSeconds()             { return islandCooldownSeconds; }
        public double getBossHPPerLevel()                  { return bossHPPerLevel; }
        public int  getBonusLootThreshold()                { return bonusLootThreshold; }
        public double getBonusLootChance()                 { return bonusLootChance; }
        public int  getCountdownSeconds()                  { return countdownSeconds; }
        public boolean isAnnounceRareEvents()              { return announceRareEvents; }
    }
}
