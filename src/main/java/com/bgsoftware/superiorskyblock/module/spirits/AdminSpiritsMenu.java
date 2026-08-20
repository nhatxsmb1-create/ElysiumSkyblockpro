package com.bgsoftware.superiorskyblock.module.spirits;

import com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule.SpiritConfigInfo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminSpiritsMenu implements InventoryHolder {

    private final Inventory inventory;
    private final SpiritsModule module;

    public AdminSpiritsMenu(SpiritsModule module) {
        this.module = module;
        this.inventory = Bukkit.createInventory(this, 54, "\u00a7c\u00a7l[Admin] \u00a78T\u1ea5t C\u1ea3 Tinh Linh");
        refresh();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void refresh() {
        inventory.clear();

        int slot = 10;
        for (Map.Entry<String, SpiritConfigInfo> entry : module.getConfiguration().getSpirits().entrySet()) {
            ItemStack item = module.getSpiritManager().createSpiritItem(entry.getKey());
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add("\u00a7c\u25b6 Click \u0111\u1ec3 l\u1ea5y Tinh Linh n\u00e0y!");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot++, item);
            if (slot % 9 == 8) slot += 2;
        }

        ItemStack filler = new ItemStack(matchMaterial("RED_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"));
        if (filler.getType().name().equals("STAINED_GLASS_PANE")) filler.setDurability((short) 14);
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
