package com.bgsoftware.superiorskyblock.module.worldevents.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

/**
 * Listens for activities that raise island Instability:
 *  - Block mining   → +instabilityPerMine
 *  - Entity kills   → +instabilityPerKill  (only mobs, not players)
 */
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
        Island island = getIslandAt(event.getBlock().getLocation());
        if (island == null) return;
        if (!isMember(island, player)) return;

        int delta = module.getConfiguration().getInstabilityPerMine();
        int newVal = module.getInstabilityManager().addInstability(island.getUniqueId(), delta);
        notifyThreshold(player, island.getUniqueId(), newVal);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return; // ignore PvP deaths

        Player killer = entity.getKiller();
        if (killer == null) return;

        Island island = getIslandAt(entity.getLocation());
        if (island == null) return;
        if (!isMember(island, killer)) return;

        int delta = module.getConfiguration().getInstabilityPerKill();
        int newVal = module.getInstabilityManager().addInstability(island.getUniqueId(), delta);
        notifyThreshold(killer, island.getUniqueId(), newVal);
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private Island getIslandAt(Location loc) {
        return SuperiorSkyblockAPI.getGrid().getIslandAt(loc);
    }

    private boolean isMember(Island island, Player player) {
        SuperiorPlayer sp = SuperiorSkyblockAPI.getPlayer(player);
        return island.isMember(sp);
    }

    /** Warn player when instability crosses notable thresholds. */
    private void notifyThreshold(Player player, UUID islandId, int instability) {
        if (instability == 25) {
            player.sendActionBar("§e⚠ Island Instability: §625% §7— Mild events possible");
        } else if (instability == 50) {
            player.sendActionBar("§c⚠ Island Instability: §c50% §7— Strong events approaching!");
        } else if (instability == 75) {
            player.sendActionBar("§4⚠ Island Instability: §475% §7— Extreme events incoming!");
        } else if (instability >= 90) {
            player.sendActionBar("§4§l⚠ CRITICAL INSTABILITY §c— Space Rift / Volcano imminent!");
        }
    }
}
