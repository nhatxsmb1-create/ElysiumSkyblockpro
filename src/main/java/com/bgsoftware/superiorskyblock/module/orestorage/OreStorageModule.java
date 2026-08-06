package com.bgsoftware.superiorskyblock.module.orestorage;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import org.bukkit.event.Listener;

public class OreStorageModule extends BuiltinModule<OreStorageModule.Configuration> {

    private StorageManager storageManager;

    public OreStorageModule() {
        super("orestorage");
    }

    @Override
    protected void onEnable(SuperiorSkyblockPlugin plugin) {
        this.storageManager = new StorageManager(getModuleFolder());
        this.storageManager.load();
    }

    @Override
    protected void onDisable(SuperiorSkyblockPlugin plugin) {
        if (this.storageManager != null) {
            this.storageManager.save();
        }
    }

    @Override
    protected void loadData(SuperiorSkyblockPlugin plugin) {
    }

    @Override
    protected Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return new Listener[]{new StorageListener(plugin, this)};
    }

    @Override
    protected SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdKho(this)};
    }

    @Override
    protected SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[0];
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration config) {
        return new Configuration(config);
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public static class Configuration implements IModuleConfiguration {
        private final boolean enabled;

        Configuration(CommentedConfiguration config) {
            this.enabled = config.getBoolean("enabled", true);
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
