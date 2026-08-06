package com.bgsoftware.superiorskyblock.module.worldevents.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

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
        handleAction(player, event.getBlock().getLocation(), module.getConfiguration().getInstabilityPerMine());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;
        Player killer = entity.getKiller();
        if (killer != null) {
            handleAction(killer, entity.getLocation(), module.getConfiguration().getInstabilityPerKill());
        }
    }

    private void handleAction(Player player, org.bukkit.Location loc, int amount) {
        Island island = SuperiorSkyblockAPI.getGrid().getIslandAt(loc);
        if (island == null) return;
        if (!island.isMember(SuperiorSkyblockAPI.getPlayer(player))) return;

        UUID id = island.getUniqueId();
        if (module.getScheduler().isActive(id) || module.getScheduler().isOnCooldown(id)) return;

        int val = module.getInstabilityManager().addInstability(id, amount);
        
        if (val >= 100) {
            module.getInstabilityManager().setInstability(id, 0); // Reset after trigger
            module.getScheduler().triggerRandomEvent(island, 100);
        } else {
            notify(plugin, player, id, val);
        }
    }

    // ── Prevent Falling Blocks (Meteors, Stars, Spores, Geysers) from solidifying/damaging islands ──
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.FALLING_BLOCK) {
            if (entity.hasMetadata("worldevent_meteor")
                    || entity.hasMetadata("worldevent_star")
                    || entity.hasMetadata("worldevent_geyser")
                    || entity.hasMetadata("worldevent_spore")) {
                event.setCancelled(true);
                entity.remove();
            }
        }
    }

    // ── Prevent Volcano Fireballs or Celestial/Meteor explosions from breaking blocks ──
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity != null) {
            if (entity.hasMetadata("worldevent_fireball")
                    || entity.hasMetadata("worldevent_meteor")
                    || entity.hasMetadata("worldevent_star")
                    || entity.getType() == EntityType.FIREBALL) {
                event.blockList().clear();
            }
        }
    }

    private void notify(SuperiorSkyblockPlugin plugin, Player player, UUID islandId, int instability) {
        String msg = null;
        if      (instability == 25) msg = "§e⚠ Độ bất ổn đảo: §625% §7— Sự kiện nhẹ có thể xảy ra";
        else if (instability == 50) msg = "§c⚠ Độ bất ổn đảo: §c50% §7— Sự kiện mạnh đang đến gần!";
        else if (instability == 75) msg = "§4⚠ Độ bất ổn đảo: §475% §7— Sự kiện cực mạnh sắp xuất hiện!";
        else if (instability >= 90) msg = "§4§l⚠ BẤT ỔN CỰC ĐỘ §c— Cổng Không Gian / Núi Lửa sắp đến!";
        if (msg != null) plugin.getNMSPlayers().sendActionBar(player, msg);
    }
}
