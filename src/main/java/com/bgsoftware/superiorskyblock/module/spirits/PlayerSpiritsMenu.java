package com.bgsoftware.superiorskyblock.module.spirits;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule.SpiritConfigInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerSpiritsMenu implements InventoryHolder {

    private final Inventory inventory;
    private final SpiritsModule module;
    private final Island island;

    public PlayerSpiritsMenu(SpiritsModule module, Island island) {
        this.module = module;
        this.island = island;
        this.inventory = Bukkit.createInventory(this, 54, "\u00a78\u2728 Qu\u1ea3n L\u00fd Tinh Linh");
        refresh();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void refresh() {
        inventory.clear();

        Map<Location, String> placed = module.getSpiritManager().getPlacedSpiritLocations(island);
        
        int slot = 10;
        for (Map.Entry<Location, String> entry : placed.entrySet()) {
            Location loc = entry.getKey();
            String type = entry.getValue();
            SpiritConfigInfo info = module.getConfiguration().getSpirits().get(type);
            if (info == null) continue;

            ItemStack item = module.getSpiritManager().createSpiritItem(type);
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add("\u00a7a\u25b6 T\u1ecda \u0111\u1ed9:");
                lore.add("  \u00a77X: \u00a7f" + loc.getBlockX());
                lore.add("  \u00a77Y: \u00a7f" + loc.getBlockY());
                lore.add("  \u00a77Z: \u00a7f" + loc.getBlockZ());
                lore.add("");
                lore.add("\u00a7e\u25b6 \u0110ang ho\u1ea1t \u0111\u1ed9ng (" + (info.getActionIntervalTicks() / 20.0) + "s/l\u1ea7n)");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot++, item);
            if (slot % 9 == 8) slot += 2;
            if (slot >= 44) break;
        }

        ItemStack filler = new ItemStack(matchMaterial("LIGHT_BLUE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"));
        if (filler.getType().name().equals("STAINED_GLASS_PANE")) filler.setDurability((short) 3);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName("\u00a7f");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                    inventory.setItem(i, filler);
                }
            }
        }
    }

    private static Material matchMaterial(String... names) {
        for (String name : names) {
            try {
                Material material = Material.matchMaterial(name);
                if (material != null)
                    return material;
            } catch (Exception ignored) {
            }
        }
        return Material.STONE;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
