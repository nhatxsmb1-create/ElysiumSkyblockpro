package com.bgsoftware.superiorskyblock.module.trophies.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

public class TrophyListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;
    private final TrophiesModule module;

    public TrophyListener(SuperiorSkyblockPlugin plugin, TrophiesModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack itemInHand = e.getItemInHand();
        String trophyId = module.getTrophyManager().getTrophyId(itemInHand);
        if (trophyId == null)
            return;

        Block block = e.getBlock();
        Island island = plugin.getGrid().getIslandAt(block.getLocation());
        if (island == null)
            return;

        module.getTrophyManager().addPlacedTrophy(island, trophyId, block.getLocation());

        int count = module.getTrophyManager().getPlacedTrophyCount(island);
        e.getPlayer().sendMessage("§6🏆 §eĐã trưng bày trophy §6" +
                module.getTrophyManager().getTrophies().get(trophyId).getName() +
                " §7(" + count + "/" + module.getTrophyManager().getTrophies().size() + " loại)");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Island island = plugin.getGrid().getIslandAt(block.getLocation());
        if (island == null)
            return;

        String trophyId = module.getTrophyManager().removePlacedTrophy(island, block.getLocation());
        if (trophyId == null)
            return;

        // Drop the actual trophy item instead of the plain block
        e.setDropItems(false);
        ItemStack trophyItem = module.getTrophyManager().createTrophyItem(trophyId);
        if (trophyItem != null)
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), trophyItem);

        Player player = e.getPlayer();
        player.sendMessage("§6🏆 §cĐã tháo trophy khỏi Trophy Hall. Buff của đảo đã được tính lại.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(block -> isTrophyBlock(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> isTrophyBlock(block.getLocation()));
    }

    private boolean isTrophyBlock(Location location) {
        Island island = plugin.getGrid().getIslandAt(location);
        return island != null && module.getTrophyManager().isPlacedTrophyAt(island, location);
    }

}
