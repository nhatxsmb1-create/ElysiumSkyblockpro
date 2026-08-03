package com.bgsoftware.superiorskyblock.module.worldevents.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

public class WorldEventsListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;
    private final WorldEventsModule module;

    public WorldEventsListener(SuperiorSkyblockPlugin plugin, WorldEventsModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Island island = SuperiorSkyblockAPI.getGrid().getIslandAt(event.getBlock().getLocation());
        if (island == null) return;
        if (!island.isMember(SuperiorSkyblockAPI.getPlayer(player))) return;

        int newVal = module.getInstabilityManager()
                .addInstability(island.getUniqueId(), module.getConfiguration().getInstabilityPerMine());
        notifyThreshold(player, newVal);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;

        Player killer = entity.getKiller();
        if (killer == null) return;

        Island island = SuperiorSkyblockAPI.getGrid().getIslandAt(entity.getLocation());
        if (island == null) return;
        if (!island.isMember(SuperiorSkyblockAPI.getPlayer(killer))) return;

        int newVal = module.getInstabilityManager()
                .addInstability(island.getUniqueId(), module.getConfiguration().getInstabilityPerKill());
        notifyThreshold(killer, newVal);
    }

    private void notifyThreshold(Player player, int instability) {
        String msg = null;
        if      (instability == 25)  msg = "§e⚠ Island Instability: §625% §7— Mild events possible";
        else if (instability == 50)  msg = "§c⚠ Island Instability: §c50% §7— Strong events approaching!";
        else if (instability == 75)  msg = "§4⚠ Island Instability: §475% §7— Extreme events incoming!";
        else if (instability >= 90)  msg = "§4§l⚠ CRITICAL INSTABILITY §c— Space Rift / Volcano imminent!";

        if (msg != null)
            plugin.getNMSPlayers().sendActionBar(player, msg);
    }
}
