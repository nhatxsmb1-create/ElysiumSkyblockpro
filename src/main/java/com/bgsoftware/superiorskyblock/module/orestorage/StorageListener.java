package com.bgsoftware.superiorskyblock.module.orestorage;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class StorageListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;
    private final OreStorageModule module;

    // Default trackable materials — covers what actually drops when mining ores
    // Use a regular HashSet because EnumSet doesn't support dynamic add from static blocks cleanly
    public static final Set<Material> TRACKABLE_MATERIALS = new java.util.HashSet<>(java.util.Arrays.asList(
            Material.COBBLESTONE, Material.STONE, Material.GRAVEL, Material.SAND,
            Material.COAL, Material.DIAMOND, Material.EMERALD,
            Material.LAPIS_LAZULI, Material.REDSTONE, Material.OBSIDIAN,
            Material.IRON_INGOT, Material.GOLD_INGOT,   // Pre-1.17 smelted drops
            Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK
    ));

    // Safely add 1.17+ raw ore materials
    static {
        String[] extras = { "RAW_IRON", "RAW_GOLD", "RAW_COPPER", "COPPER_INGOT",
                "AMETHYST_SHARD", "NETHER_QUARTZ" };
        for (String name : extras) {
            try { TRACKABLE_MATERIALS.add(Material.valueOf(name)); } catch (Exception ignored) {}
        }
    }

    public StorageListener(SuperiorSkyblockPlugin plugin, OreStorageModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        Island island = SuperiorSkyblockAPI.getGrid().getIslandAt(block.getLocation());
        if (island == null) return;
        
        SuperiorPlayer sp = SuperiorSkyblockAPI.getPlayer(player);
        if (!island.isMember(sp)) return;

        // Get drops considering tool
        Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand(), player);
        if (drops.isEmpty()) return;

        boolean addedToStorage = false;

        for (ItemStack drop : drops) {
            if (TRACKABLE_MATERIALS.contains(drop.getType())) {
                module.getStorageManager().addAmount(island.getUniqueId(), drop.getType(), BigInteger.valueOf(drop.getAmount()));
                addedToStorage = true;
            } else {
                // If it's not trackable, just drop it naturally
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }

        if (addedToStorage) {
            // Cancel natural drop
            event.setDropItems(false);
            // Optionally send action bar message
            plugin.getNMSPlayers().sendActionBar(player, "§a+ Đã thêm vào /kho");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof OreStorageMenu) {
            event.setCancelled(true);
            if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof OreStorageMenu) {
                ((OreStorageMenu) holder).handleClick((Player) event.getWhoClicked(), event.getSlot(), event.getClick(), event.getCurrentItem());
            } else if (event.getClickedInventory() != null && event.getClick().name().contains("SHIFT")) {
                // Prevent shift clicking items from player inventory into the menu if not handled
                event.setCancelled(true);
            }
        }
    }
}
