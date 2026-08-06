package com.bgsoftware.superiorskyblock.module.orestorage;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class StorageListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;
    private final OreStorageModule module;

    // Materials auto-collected into the ore storage kho
    // All resolved safely so old API jars don't fail to compile
    public static final Set<Material> TRACKABLE_MATERIALS = new HashSet<>();

    static {
        // Safe multi-version material resolution
        String[] matNames = {
            "COBBLESTONE", "STONE", "GRAVEL", "SAND",
            "COAL", "DIAMOND", "EMERALD",
            "LAPIS_LAZULI", "REDSTONE", "OBSIDIAN",
            "IRON_INGOT", "GOLD_INGOT",
            "IRON_BLOCK", "GOLD_BLOCK", "DIAMOND_BLOCK", "EMERALD_BLOCK",
            // 1.17+
            "RAW_IRON", "RAW_GOLD", "RAW_COPPER", "COPPER_INGOT",
            "AMETHYST_SHARD", "NETHER_QUARTZ"
        };
        for (String name : matNames) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) TRACKABLE_MATERIALS.add(mat);
        }
    }

    public StorageListener(SuperiorSkyblockPlugin plugin, OreStorageModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    /**
     * Intercept item drops (from block breaks) on islands.
     * Using ItemSpawnEvent is fully compatible with all Bukkit API versions.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        ItemStack stack = item.getItemStack();

        if (!TRACKABLE_MATERIALS.contains(stack.getType())) return;

        Island island = SuperiorSkyblockAPI.getGrid().getIslandAt(item.getLocation());
        if (island == null) return;

        // Vacuum into island storage
        module.getStorageManager().addAmount(island.getUniqueId(), stack.getType(),
                BigInteger.valueOf(stack.getAmount()));
        event.setCancelled(true); // Prevent item from appearing on ground
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof OreStorageMenu)) return;

        event.setCancelled(true);

        // Only handle clicks on the storage menu itself, not player's own inventory area
        if (event.getClickedInventory() == null) return;
        if (!(event.getClickedInventory().getHolder() instanceof OreStorageMenu)) return;

        ((OreStorageMenu) holder).handleClick(
                (Player) event.getWhoClicked(),
                event.getSlot(),
                event.getClick(),
                event.getCurrentItem()
        );
    }
}
