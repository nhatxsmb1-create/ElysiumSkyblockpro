package com.bgsoftware.superiorskyblock.module.spirits;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Bukkit;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import com.bgsoftware.superiorskyblock.module.spirits.SpiritManager.PlacedSpirit;


public class SpiritsListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        Island island = plugin.getGrid().getIslandAt(block.getLocation());
        if (island == null) return;
        
        PlacedSpirit spirit = module.getSpiritManager().getPlacedSpiritLocations(island).get(block.getLocation());
        if (spirit != null) {
            e.setCancelled(true);
            new SpiritUpgradeMenu(module, island, block.getLocation(), spirit).open(e.getPlayer());
        }
    }


    private final SuperiorSkyblockPlugin plugin;
    private final SpiritsModule module;

    public SpiritsListener(SuperiorSkyblockPlugin plugin, SpiritsModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack itemInHand = e.getItemInHand();
        String type = module.getSpiritManager().getSpiritType(itemInHand);
        if (type == null)
            return;

        Block block = e.getBlock();
        Island island = plugin.getGrid().getIslandAt(block.getLocation());
        if (island == null)
            return;

        module.getSpiritManager().addPlacedSpirit(island, type, 1, block.getLocation());

        e.getPlayer().sendMessage("\u00a7b\u2728 \u00a7e\u0110\u00e3 tri\u1ec7u h\u1ed3i Tinh Linh \u00a7f" +
                org.bukkit.ChatColor.translateAlternateColorCodes('&', module.getConfiguration().getSpirits().get(type).getName()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Island island = plugin.getGrid().getIslandAt(block.getLocation());
        if (island == null)
            return;

        String type = module.getSpiritManager().removePlacedSpirit(island, block.getLocation());
        if (type == null)
            return;

        e.setCancelled(true);
        // Safely clear block
        Bukkit.getScheduler().runTask(plugin, () -> block.setType(org.bukkit.Material.AIR));

        ItemStack item = module.getSpiritManager().createSpiritItem(type);
        if (item != null)
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), item);

        e.getPlayer().sendMessage("\u00a7b\u2728 \u00a7c\u0110\u00e3 thu h\u1ed3i Tinh Linh.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(block -> isSpiritBlock(block.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> isSpiritBlock(block.getLocation()));
    }



    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof AdminSpiritsMenu) {
            e.setCancelled(true);
            if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof AdminSpiritsMenu)) return;
            
            ItemStack clicked = e.getCurrentItem();
            String type = module.getSpiritManager().getSpiritType(clicked);
            if (type != null) {
                ItemStack item = module.getSpiritManager().createSpiritItem(type);
                if (item != null) {
                    Player player = (Player) e.getWhoClicked();
                    player.getInventory().addItem(item);
                    player.sendMessage("\u00a7b\u2728 \u00a7eB\u1ea1n \u0111\u00e3 l\u1ea5y 1 " + item.getItemMeta().getDisplayName());
                }
            }
        } else if (holder instanceof SpiritUpgradeMenu) {
            e.setCancelled(true);
            ((SpiritUpgradeMenu) holder).handleClick(e);
        } else if (holder instanceof PlayerSpiritsMenu) {
            e.setCancelled(true);
        }
    }

    private boolean isSpiritBlock(Location location) {
        Island island = plugin.getGrid().getIslandAt(location);
        return island != null && module.getSpiritManager().isPlacedSpiritAt(island, location);
    }
}
