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
                        String typeName = translatePotion(effect.getType().getName());
                        int level = effect.getAmplifier() + 1;
                        lore.add("  \u00a77- \u00a7f" + typeName + " " + toRoman(level));
                    }
                }
                
                if (!info.getBonuses().isEmpty()) {
                    lore.add("\u00a7b\u25b6 Buff th\u01b0\u1edfng th\u00eam:");
                    for (Map.Entry<String, Double> bonus : info.getBonuses().entrySet()) {
                        lore.add("  \u00a77- \u00a7f" + translateBonus(bonus.getKey()) + ": \u00a7a+" + (bonus.getValue() * 100) + "%");
                    }
                }
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot++, item);
            if (slot % 9 == 8) 
                slot += 2;
        }

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

    private String translatePotion(String name) {
        switch (name.toUpperCase()) {
            case "FAST_DIGGING": return "\u0110\u00e0o Nhanh";
            case "SPEED": return "T\u1ed1c \u0110\u1ed9";
            case "FIRE_RESISTANCE": return "Kh\u00e1ng L\u1eeda";
            case "DAMAGE_RESISTANCE": return "Kh\u00e1ng C\u1ef1";
            case "INCREASE_DAMAGE": return "S\u1ee9c M\u1ea1nh";
            case "REGENERATION": return "H\u1ed3i M\u00e1u";
            case "NIGHT_VISION": return "Nh\u00ecn Trong \u0110\u00eam";
            case "JUMP": return "Nh\u1ea3y Cao";
            case "SLOW_FALLING": return "R\u01a1i Ch\u1eadm";
            case "WATER_BREATHING": return "Th\u1edf D\u01b0\u1edbi N\u01b0\u1edbc";
            case "INVISIBILITY": return "T\u00e0ng H\u00ecnh";
            case "BLINDNESS": return "M\u00f9 L\u00f2a";
            case "SLOW": return "Ch\u1eadm Ch\u1ea1p";
            case "GLOWING": return "Ph\u00e1t S\u00e1ng";
            case "HEAL": return "H\u1ed3i Má\u00e1u T\u1ee9c Th\u1eddi";
            case "HARM": return "S\u00e1t Th\u01b0\u01a1ng";
            case "POISON": return "Tr\u00fang \u0110\u1ed9c";
            case "WITHER": return "H\u00e9o \u00dana";
            case "HEALTH_BOOST": return "T\u0103ng M\u00e1u T\u1ed1i \u0110a";
            case "ABSORPTION": return "H\u1ea5p Th\u1ee5";
            case "SATURATION": return "No B\u1ee5ng";
            case "LEVITATION": return "Bay L\u01a1 L\u1eedng";
            case "CONDUIT_POWER": return "S\u1ee9c M\u1ea1nh \u0110\u1ea1i D\u01b0\u01a1ng";
            case "DOLPHINS_GRACE": return "S\u1ee9c M\u1ea1nh C\u00e1 Heo";
            case "BAD_OMEN": return "Đi\u1ec1m X\u1ea5u";
            case "HERO_OF_THE_VILLAGE": return "Anh H\u00f9ng L\u00e0ng";
            default: return name;
        }
    }

    private String translateBonus(String name) {
        switch (name.toLowerCase()) {
            case "crop-growth": return "T\u1ed1c \u0111\u1ed9 m\u1ecdc c\u00e2y";
            case "mob-drops": return "T\u1ec9 l\u1ec7 r\u1edbt \u0111\u1ed3";
            case "spawner-rates": return "T\u1ed1c \u0111\u1ed9 Spawner";
            default: return name;
        }
    }

    private String toRoman(int num) {
        switch (num) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
            default: return String.valueOf(num);
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
