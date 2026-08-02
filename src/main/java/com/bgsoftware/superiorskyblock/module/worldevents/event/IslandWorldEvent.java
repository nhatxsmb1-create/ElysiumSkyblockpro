package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class IslandWorldEvent {

    protected final Island island;
    protected final Location center;

    protected IslandWorldEvent(Island island, Location center) {
        this.island = island;
        this.center = center.clone();
    }

    /**
     * Start the event.
     * @param plugin    the plugin instance for scheduling
     * @param onFinish  callback invoked when event ends (success or timeout)
     */
    public abstract void start(SuperiorSkyblockPlugin plugin, Runnable onFinish);

    /** Collect online players from island members. */
    protected List<Player> getOnlinePlayers() {
        List<Player> list = new ArrayList<>();
        for (SuperiorPlayer sp : island.getIslandMembers(true)) {
            if (sp.isOnline() && sp.asPlayer() != null) {
                list.add(sp.asPlayer());
            }
        }
        return list;
    }

    /** Broadcast to all online island members. */
    protected void broadcast(String msg) {
        String prefix = "§d[World Event] §r";
        for (Player p : getOnlinePlayers()) {
            p.sendMessage(prefix + msg);
        }
    }

    protected void broadcastActionBar(String msg) {
        for (Player p : getOnlinePlayers()) {
            p.sendActionBar(msg);
        }
    }
}
