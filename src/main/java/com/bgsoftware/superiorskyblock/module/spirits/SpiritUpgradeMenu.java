package com.bgsoftware.superiorskyblock.module.spirits;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.spirits.SpiritManager.PlacedSpirit;
import com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule.SpiritConfigInfo;
import com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule.SpiritUpgradeInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpiritUpgradeMenu implements InventoryHolder {

    private final Inventory inventory;
    private final SpiritsModule module;
    private final Island island;
    private final Location location;
    private final PlacedSpirit spirit;

    public SpiritUpgradeMenu(SpiritsModule module, Island island, Location location, PlacedSpirit spirit) {
        this.module = module;
        this.island = island;
        this.location = location;
        this.spirit = spirit;
        this.inventory = Bukkit.createInventory(this, 27, "\u00a78\u2728 Th\u1ee9c T\u1ec9nh Tinh Linh");
        refresh();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void refresh() {
        inventory.clear();

        SpiritConfigInfo info = module.getConfiguration().getSpirits().get(spirit.getType());
        if (info == null) return;

        int currentLevel = spirit.getLevel();
        int maxLevel = info.getMaxLevel();

        ItemStack item = module.getSpiritManager().createSpiritItem(spirit.getType());
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add(0, "");
                lore.add(0, "\u00a7e\u2605 C\u1ea5p \u0111\u1ed9 hi\u1ec7n t\u1ea1i: \u00a76" + currentLevel);
                
                int currentTicks = info.getActionIntervalTicks();
                if (info.getUpgrades().containsKey(currentLevel)) {
                    currentTicks = info.getUpgrades().get(currentLevel).getIntervalTicks();
                }
                lore.add(1, "\u00a7a\u23f1 T\u1ed1c \u0111\u1ed9: \u00a7f" + (currentTicks / 20.0) + "s/l\u1ea7n");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(11, item);
        }

        ItemStack upgradeBtn = new ItemStack(Material.matchMaterial("NETHER_STAR"));
        ItemMeta upMeta = upgradeBtn.getItemMeta();
        if (currentLevel >= maxLevel) {
            upMeta.setDisplayName("\u00a7c\u2716 \u0110\u00e3 \u0111\u1ea1t c\u1ea5p t\u1ed1i \u0111a");
        } else {
            upMeta.setDisplayName("\u00a7a\u2714 Nh\u1ea5n \u0111\u1ec3 Th\u1ee9c T\u1ec9nh l\u00ean C\u1ea5p " + (currentLevel + 1));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("\u00a77Nguy\u00ean li\u1ec7u y\u00eau c\u1ea7u:");
            
            SpiritUpgradeInfo nextUp = info.getUpgrades().get(currentLevel + 1);
            if (nextUp != null) {
                for (Map.Entry<Material, Integer> cost : nextUp.getCost().entrySet()) {
                    boolean hasEnough = hasEnoughItem(cost.getKey(), cost.getValue());
                    String color = hasEnough ? "\u00a7a" : "\u00a7c";
                    lore.add(color + "- " + cost.getValue() + "x " + cost.getKey().name());
                }
                lore.add("");
                lore.add("\u00a7e\u25b6 T\u1ed1c \u0111\u1ed9 m\u1edbi: \u00a7f" + (nextUp.getIntervalTicks() / 20.0) + "s/l\u1ea7n");
            }
            upMeta.setLore(lore);
        }
        upgradeBtn.setItemMeta(upMeta);
        inventory.setItem(15, upgradeBtn);
    }

    private boolean hasEnoughItem(Material mat, int amount) {
        BigInteger stored = BuiltinModules.ORE_STORAGE.getStorageManager().getAmount(island.getUniqueId(), mat);
        if (stored != null && stored.compareTo(BigInteger.valueOf(amount)) >= 0) {
            return true;
        }
        return false;
    }

    public void handleClick(InventoryClickEvent e) {
        if (e.getRawSlot() == 15) {
            Player player = (Player) e.getWhoClicked();
            SpiritConfigInfo info = module.getConfiguration().getSpirits().get(spirit.getType());
            if (info == null) return;
            
            int currentLevel = spirit.getLevel();
            int maxLevel = info.getMaxLevel();
            
            if (currentLevel >= maxLevel) {
                player.sendMessage("\u00a7cTinh linh n\u00e0y \u0111\u00e3 \u0111\u1ea1t c\u1ea5p t\u1ed1i \u0111a!");
                return;
            }
            
            SpiritUpgradeInfo nextUp = info.getUpgrades().get(currentLevel + 1);
            if (nextUp == null) return;
            
            for (Map.Entry<Material, Integer> cost : nextUp.getCost().entrySet()) {
                if (!hasEnoughItem(cost.getKey(), cost.getValue())) {
                    player.sendMessage("\u00a7c\u0110\u1ea3o c\u1ee7a b\u1ea1n kh\u00f4ng \u0111\u1ee7 nguy\u00ean li\u1ec7u trong kho (/is kho)!");
                    return;
                }
            }
            
            // Deduct
            for (Map.Entry<Material, Integer> cost : nextUp.getCost().entrySet()) {
                BuiltinModules.ORE_STORAGE.getStorageManager().removeAmount(island.getUniqueId(), cost.getKey(), BigInteger.valueOf(cost.getValue()));
            }
            
            // Upgrade
            module.getSpiritManager().removePlacedSpirit(island, location);
            module.getSpiritManager().addPlacedSpirit(island, spirit.getType(), currentLevel + 1, location);
            
            player.sendMessage("\u00a7a\u2728 Ch\u00fac m\u1eebng! Tinh linh \u0111\u00e3 \u0111\u01b0\u1ee3c Th\u1ee9c T\u1ec9nh l\u00ean c\u1ea5p " + (currentLevel + 1) + "!");
            player.closeInventory();
            
            // Spawn firework or sound
            try {
                try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 1f, 1f); } catch (Exception ex) { try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("LEVEL_UP"), 1f, 1f); } catch (Exception ex2) {} }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
