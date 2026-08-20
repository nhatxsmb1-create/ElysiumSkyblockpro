package com.bgsoftware.superiorskyblock.module.trophies.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule;
import com.bgsoftware.superiorskyblock.module.trophies.TrophiesMainMenu;
import com.bgsoftware.superiorskyblock.module.trophies.TrophiesPlacedMenu;
import com.bgsoftware.superiorskyblock.module.trophies.TrophiesCollectionMenu;
import com.bgsoftware.superiorskyblock.module.trophies.AdminTrophiesMenu;
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
import org.bukkit.event.inventory.InventoryClickEvent;
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
        e.getPlayer().sendMessage("\u00a76\ud83c\udfc6 \u00a7eĐã trưng bày trophy \u00a76" +
                module.getTrophyManager().getTrophies().get(trophyId).getName() +
                " \u00a77(" + count + "/" + module.getTrophyManager().getTrophies().size() + " loại)");
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

        e.setCancelled(true);
        block.setType(org.bukkit.Material.AIR);
        ItemStack trophyItem = module.getTrophyManager().createTrophyItem(trophyId);
        if (trophyItem != null)
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), trophyItem);

        Player player = e.getPlayer();
        player.sendMessage("\u00a76\ud83c\udfc6 \u00a7cĐã tháo trophy khỏi Trophy Hall. Buff của đảo đã được tính lại.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(block -> isTrophyBlock(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> isTrophyBlock(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof TrophiesMainMenu) {
            e.setCancelled(true);
            TrophiesMainMenu menu = (TrophiesMainMenu) e.getInventory().getHolder();
            if (e.getRawSlot() == 11) {
                new TrophiesPlacedMenu(menu.getModule(), menu.getIsland(), menu.getPlayer()).open();
            } else if (e.getRawSlot() == 15) {
                new TrophiesCollectionMenu(menu.getModule(), menu.getIsland(), menu.getPlayer()).open();
            }
        } else if (e.getInventory().getHolder() instanceof TrophiesPlacedMenu) {
            e.setCancelled(true);
            TrophiesPlacedMenu menu = (TrophiesPlacedMenu) e.getInventory().getHolder();
            if (e.getRawSlot() == 49) {
                new TrophiesMainMenu(menu.getModule(), menu.getIsland(), menu.getPlayer()).open();
            }
        } else if (e.getInventory().getHolder() instanceof TrophiesCollectionMenu) {
            e.setCancelled(true);
            TrophiesCollectionMenu menu = (TrophiesCollectionMenu) e.getInventory().getHolder();
            if (e.getRawSlot() == 49) {
                new TrophiesMainMenu(menu.getModule(), menu.getIsland(), menu.getPlayer()).open();
            }
        } else if (e.getInventory().getHolder() instanceof AdminTrophiesMenu) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            String trophyId = module.getTrophyManager().getTrophyId(clicked);
            if (trophyId != null) {
                ItemStack trophyItem = module.getTrophyManager().createTrophyItem(trophyId);
                if (trophyItem != null) {
                    Player player = (Player) e.getWhoClicked();
                    player.getInventory().addItem(trophyItem);
                    player.sendMessage("\u00a76\ud83c\udfc6 \u00a7eBạn đã lấy 1 " + trophyItem.getItemMeta().getDisplayName());
                }
            }
        }
    }

    private boolean isTrophyBlock(Location location) {
        Island island = plugin.getGrid().getIslandAt(location);
        return island != null && module.getTrophyManager().isPlacedTrophyAt(island, location);
    }

}
