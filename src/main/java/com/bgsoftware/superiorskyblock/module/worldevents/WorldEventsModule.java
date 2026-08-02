package com.bgsoftware.superiorskyblock.module.worldevents;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import com.bgsoftware.superiorskyblock.module.worldevents.commands.CmdAdminSetInstability;
import com.bgsoftware.superiorskyblock.module.worldevents.commands.CmdAdminTriggerEvent;
import com.bgsoftware.superiorskyblock.module.worldevents.data.InstabilityManager;
import com.bgsoftware.superiorskyblock.module.worldevents.listener.WorldEventsListener;
import org.bukkit.event.Listener;

public class WorldEventsModule extends BuiltinModule<WorldEventsModule.Configuration> {

    private InstabilityManager instabilityManager;
    private WorldEventScheduler scheduler;

    public WorldEventsModule() {
        super("worldevents");
    }

    @Override
    protected void onEnable(SuperiorSkyblockPlugin plugin) {
        this.instabilityManager = new InstabilityManager(getModuleFolder());
        this.instabilityManager.load();
        this.scheduler = new WorldEventScheduler(plugin, this);
        this.scheduler.start();
    }

    @Override
    protected void onDisable(SuperiorSkyblockPlugin plugin) {
        if (this.scheduler != null) this.scheduler.stop();
        if (this.instabilityManager != null) this.instabilityManager.save();
    }

    @Override
    protected void loadData(SuperiorSkyblockPlugin plugin) {
        // nothing extra
    }

    @Override
    protected Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return new Listener[]{new WorldEventsListener(plugin, this)};
    }

    @Override
    protected SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return null;
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

    public InstabilityManager getInstabilityManager() {
        return instabilityManager;
    }

    public WorldEventScheduler getScheduler() {
        return scheduler;
    }

    // =========================================================
    // Configuration
    // =========================================================
    public static class Configuration implements IModuleConfiguration {

        private final boolean enabled;
        // How often (seconds) the scheduler checks each island for a possible event
        private final int checkIntervalSeconds;
        // Base chance (0-100) at 0% instability that any event fires on a check
        private final int baseEventChance;
        // Bonus chance added per instability point (e.g. 0.5 → at 100% instability total chance = base + 50)
        private final double instabilityChanceBonus;
        // How much instability each tracked action adds
        private final int instabilityPerKill;
        private final int instabilityPerMine;
        // How fast instability decays per check (passive decay)
        private final int instabilityDecayPerCheck;

        Configuration(CommentedConfiguration config) {
            this.enabled = config.getBoolean("enabled", true);
            this.checkIntervalSeconds = config.getInt("check-interval-seconds", 300);
            this.baseEventChance = config.getInt("base-event-chance", 5);
            this.instabilityChanceBonus = config.getDouble("instability-chance-bonus", 0.5);
            this.instabilityPerKill = config.getInt("instability-per-kill", 2);
            this.instabilityPerMine = config.getInt("instability-per-mine", 1);
            this.instabilityDecayPerCheck = config.getInt("instability-decay-per-check", 3);
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        public int getCheckIntervalSeconds() { return checkIntervalSeconds; }
        public int getBaseEventChance() { return baseEventChance; }
        public double getInstabilityChanceBonus() { return instabilityChanceBonus; }
        public int getInstabilityPerKill() { return instabilityPerKill; }
        public int getInstabilityPerMine() { return instabilityPerMine; }
        public int getInstabilityDecayPerCheck() { return instabilityDecayPerCheck; }
    }
}
