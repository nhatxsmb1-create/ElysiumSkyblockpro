package com.bgsoftware.superiorskyblock.module.trophies;

import com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule.TrophyInfo;
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

public class AdminTrophiesMenu implements InventoryHolder {

    private final Inventory inventory;
    private final TrophiesModule module;

    public AdminTrophiesMenu(TrophiesModule module) {
        this.module = module;
        this.inventory = Bukkit.createInventory(this, 54, "\u00a7c\u00a7lAdmin \u00a78\u25b6 \u00a78\ud83c\udfc6 Trophy");
        refresh();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void refresh() {
        inventory.clear();

        int totalTypes = module.getTrophyManager().getTrophies().size();
        int rowStart = 20; 
        int slot = rowStart;
        if (totalTypes <= 7) {
            slot = 22 - (totalTypes / 2); 
        }

        for (Map.Entry<String, TrophyInfo> entry : module.getTrophyManager().getTrophies().entrySet()) {
            TrophyInfo info = entry.getValue();
            ItemStack item = module.getTrophyManager().createTrophyItem(info.getId());
            if (item == null)
                continue;

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add("\u00a7e\u25b6 Nhấn để lấy 1 chiếc!");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot++, item);
            if (slot % 9 == 8) 
                slot += 2;
        }

        ItemStack info = new ItemStack(matchMaterial("COMMAND_BLOCK", "COMMAND"));
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("\u00a7c\u00a7lAdmin Panel");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Lấy Trophy để test tính năng.");
            lore.add("\u00a77Người chơi bình thường không");
            lore.add("\u00a77thể mở được menu này.");
            infoMeta.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        inventory.setItem(49, info);

        ItemStack filler = new ItemStack(matchMaterial("RED_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"));
        if (filler.getType().name().equals("STAINED_GLASS_PANE"))
            filler.setDurability((short) 14);
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
        return Material.BOOK;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
