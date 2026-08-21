package com.bgsoftware.superiorskyblock.module.market;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class MarketTask extends BukkitRunnable {

    private final MarketModule module;

    public MarketTask(MarketModule module) {
        this.module = module;
    }

    @Override
    public void run() {
        for (Map.Entry<String, MarketModule.MarketItemInfo> entry : module.getConfiguration().getItems().entrySet()) {
            MarketModule.MarketItemInfo info = entry.getValue();
            // recoveryRate defines how much pool size decreases per hour
            info.addPoolSize(-info.getRecoveryRate());
        }
        module.saveData();
    }
}
