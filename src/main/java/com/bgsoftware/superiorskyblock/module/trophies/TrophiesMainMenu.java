package com.bgsoftware.superiorskyblock.module.trophies;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TrophiesMainMenu implements InventoryHolder {

    private final Inventory inventory;
    private final TrophiesModule module;
    private final Island island;
    private final Player player;

    public TrophiesMainMenu(TrophiesModule module, Island island, Player player) {
        this.module = module;
        this.island = island;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, "\u00a78\ud83c\udfc6 Trophy Hall \u00a78\u25b6 Menu");
        refresh();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void refresh() {
        inventory.clear();

        // Placed Trophies Button (Slot 11)
        ItemStack placedBtn = new ItemStack(matchMaterial("GOLD_BLOCK", "GOLD_BLOCK"));
        ItemMeta placedMeta = placedBtn.getItemMeta();
        if (placedMeta != null) {
            placedMeta.setDisplayName("\u00a76\u00a7lTrophy Trên Đảo");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Xem các Trophy đang được");
            lore.add("\u00a77trưng bày trên đảo của bạn.");
            lore.add("");
            lore.add("\u00a7e\u25b6 Nhấn để mở!");
            placedMeta.setLore(lore);
            placedBtn.setItemMeta(placedMeta);
        }
        inventory.setItem(11, placedBtn);

        // Collection Button (Slot 15)
        ItemStack collectionBtn = new ItemStack(matchMaterial("BOOK", "BOOK"));
        ItemMeta collectionMeta = collectionBtn.getItemMeta();
        if (collectionMeta != null) {
            collectionMeta.setDisplayName("\u00a7b\u00a7lBộ Sưu Tập Trophy");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Xem toàn bộ danh sách");
            lore.add("\u00a77Trophy có trong máy chủ.");
            lore.add("");
            lore.add("\u00a7e\u25b6 Nhấn để mở!");
            collectionMeta.setLore(lore);
            collectionBtn.setItemMeta(collectionMeta);
        }
        inventory.setItem(15, collectionBtn);

        // Fill glass
        ItemStack filler = new ItemStack(matchMaterial("BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"));
        if (filler.getType().name().equals("STAINED_GLASS_PANE"))
            filler.setDurability((short) 15);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName("\u00a7f");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
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

    public TrophiesModule getModule() {
        return module;
    }

    public Island getIsland() {
        return island;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
