package com.bgsoftware.superiorskyblock.module.trophies;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule.TrophyInfo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrophiesCollectionMenu implements InventoryHolder {

    private final Inventory inventory;
    private final TrophiesModule module;
    private final Island island;
    private final Player player;

    public TrophiesCollectionMenu(TrophiesModule module, Island island, Player player) {
        this.module = module;
        this.island = island;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, "\u00a78\ud83c\udfc6 Trophy \u00a78\u25b6 B\u1ed9 S\u01b0u T\u1eadp");
        refresh();
    }

    public void open() {
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

            int ownedCount = countOwnedTrophies(player, info.getId());

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add("");
                lore.add("\u00a77\u25b6 \u0110ang s\u1edf h\u1eefu: \u00a7e" + ownedCount);
                
                if (!info.getPotions().isEmpty()) {
                    lore.add("\u00a7d\u25b6 Buff nh\u1eadn \u0111\u01b0\u1ee3c:");
                    for (PotionEffect effect : info.getPotions()) {
                        String typeName = effect.getType().getName();
                        int level = effect.getAmplifier() + 1;
                        lore.add("  \u00a77- \u00a7f" + typeName + " " + level);
                    }
                }
                
                if (!info.getBonuses().isEmpty()) {
                    lore.add("\u00a7b\u25b6 Buff th\u01b0\u1edfng th\u00eam:");
                    for (Map.Entry<String, Double> bonus : info.getBonuses().entrySet()) {
                        lore.add("  \u00a77- \u00a7f" + bonus.getKey() + ": \u00a7a+" + (bonus.getValue() * 100) + "%");
                    }
                }
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot++, item);
            if (slot % 9 == 8) 
                slot += 2;
        }

        // Back button
        ItemStack backBtn = new ItemStack(matchMaterial("ARROW", "ARROW"));
        ItemMeta backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("\u00a7c\u00a7lQuay L\u1ea1i");
            backBtn.setItemMeta(backMeta);
        }
        inventory.setItem(49, backBtn);

        ItemStack filler = new ItemStack(matchMaterial("BLUE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"));
        if (filler.getType().name().equals("STAINED_GLASS_PANE"))
            filler.setDurability((short) 11);
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

    private int countOwnedTrophies(Player player, String trophyId) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                if (trophyId.equals(module.getTrophyManager().getTrophyId(item))) {
                    count += item.getAmount();
                }
            }
        }
        return count;
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
