package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class IslandWorldEvent {

    protected final Island island;
    protected final Location center;
    protected SuperiorSkyblockPlugin plugin;

    protected IslandWorldEvent(Island island, Location center) {
        this.island = island;
        this.center = center.clone();
    }

    public abstract void start(SuperiorSkyblockPlugin plugin, Runnable onFinish);

    protected List<Player> getOnlinePlayers() {
        List<Player> list = new ArrayList<>();
        for (SuperiorPlayer sp : island.getIslandMembers(true)) {
            if (sp.isOnline() && sp.asPlayer() != null)
                list.add(sp.asPlayer());
        }
        return list;
    }

    protected void broadcast(String msg) {
        for (Player p : getOnlinePlayers())
            p.sendMessage("§d[World Event] §r" + msg);
    }

    /** NMS-safe action bar (works in 1.8.8+) */
    protected void broadcastActionBar(String msg) {
        for (Player p : getOnlinePlayers())
            plugin.getNMSPlayers().sendActionBar(p, msg);
    }

    /** NMS-safe title */
    protected void broadcastTitle(String title, String sub) {
        for (Player p : getOnlinePlayers())
            plugin.getNMSPlayers().sendTitle(p, title, sub, 10, 60, 20);
    }

    /** Safe sound — silently ignores names that don't exist in this version */
    protected void sound(Location loc, float vol, float pitch, String... names) {
        for (String name : names) {
            try {
                loc.getWorld().playSound(loc, Sound.valueOf(name), vol, pitch);
                return;
            } catch (Exception ignored) {}
        }
    }

    /** Safe particle via Effect enum — available in 1.8.8+ */
    protected void fx(Location loc, int count, String... effectNames) {
        for (String name : effectNames) {
            try {
                Effect eff = Effect.valueOf(name);
                for (int i = 0; i < count; i++)
                    loc.getWorld().playEffect(loc, eff, 0);
                return;
            } catch (Exception ignored) {}
        }
    }
}
