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

        int val = module.getInstabilityManager()
                .addInstability(island.getUniqueId(), module.getConfiguration().getInstabilityPerMine());
        notify(plugin, player, island.getUniqueId(), val);
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

        int val = module.getInstabilityManager()
                .addInstability(island.getUniqueId(), module.getConfiguration().getInstabilityPerKill());
        notify(plugin, killer, island.getUniqueId(), val);
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
