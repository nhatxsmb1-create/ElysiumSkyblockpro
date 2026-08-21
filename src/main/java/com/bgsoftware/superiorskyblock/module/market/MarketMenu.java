package com.bgsoftware.superiorskyblock.module.market;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.orestorage.OreStorageModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketMenu implements Listener {

    private final MarketModule module;
    private final SuperiorSkyblock plugin;
    private final Inventory inventory;

    public MarketMenu(MarketModule module, SuperiorSkyblock plugin) {
        this.module = module;
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 54, "\u00a78\u00a7lS\u00e0n Ch\u1ee9ng Kho\u00e1n T\u00e0i Nguy\u00ean");
        Bukkit.getPluginManager().registerEvents(this, (SuperiorSkyblockPlugin) plugin);
        update();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    private void update() {
        inventory.clear();
        int slot = 0;
        for (Map.Entry<String, MarketModule.MarketItemInfo> entry : module.getConfiguration().getItems().entrySet()) {
            if (slot >= 54) break;
            MarketModule.MarketItemInfo info = entry.getValue();
            Material mat = info.getMaterial();
            if (mat == null) continue;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("\u00a76\u00a7l" + MarketModule.getVietnameseName(mat));
                List<String> lore = new ArrayList<>();
                double currentPrice = info.getCurrentPrice();
                String status = currentPrice >= info.getBasePrice() ? "\u00a7a\u2b06 \u0110ang c\u00f3 gi\u00e1" : "\u00a7c\u2b07 R\u1edbt gi\u00e1";
                
                lore.add("\u00a77Tr\u1ea1ng th\u00e1i: " + status);
                lore.add("\u00a7eGi\u00e1 b\u00e1n hi\u1ec7n t\u1ea1i: \u00a7a" + String.format("$%.2f", currentPrice) + " \u00a77/ 1 c\u00e1i");
                lore.add("");
                lore.add("\u00a78Gi\u00e1 g\u1ed1c: $" + info.getBasePrice());
                lore.add("\u00a78Gi\u00e1 \u0111\u1ec9nh: $" + info.getMaxPrice());
                lore.add("\u00a78Gi\u00e1 \u0111\u00e1y: $" + info.getMinPrice());
                lore.add("");
                lore.add("\u00a7bS\u1ed1 l\u01b0\u1ee3ng server \u0111\u00e3 b\u00e1n h\u00f4m nay: \u00a7f" + info.getPoolSize());
                lore.add("");
                lore.add("\u00a7a[\u25b6] Click Chu\u1ed9t Tr\u00e1i \u0111\u1ec3 b\u00e1n t\u1ea5t c\u1ea3 trong T\u00fai");
                lore.add("\u00a7a[\u25b6] Click Chu\u1ed9t Ph\u1ea3i \u0111\u1ec3 b\u00e1n t\u1ea5t c\u1ea3 t\u1eeb /is kho");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot++, item);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;
        Material clickedMat = e.getCurrentItem().getType();
        MarketModule.MarketItemInfo info = null;

        for (MarketModule.MarketItemInfo i : module.getConfiguration().getItems().values()) {
            if (i.getMaterial() == clickedMat) {
                info = i;
                break;
            }
        }
        if (info == null) return;

        Player player = (Player) e.getWhoClicked();
        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        
        long amountToSell = 0;

        if (e.getClick() == ClickType.LEFT) {
            // Sell from inventory
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() == clickedMat) {
                    amountToSell += contents[i].getAmount();
                    player.getInventory().setItem(i, null);
                }
            }
        } else if (e.getClick() == ClickType.RIGHT) {
            // Sell from /is kho
            if (sp.getIsland() == null) {
                player.sendMessage("\u00a7cB\u1ea1n ch\u01b0a c\u00f3 \u0111\u1ea3o \u0111\u1ec3 d\u00f9ng kho!");
                return;
            }
            BigInteger amountInKho = BuiltinModules.ORE_STORAGE.getStorageManager().getAmount(sp.getIsland().getUniqueId(), clickedMat);
            if (amountInKho.compareTo(BigInteger.ZERO) > 0) {
                // To avoid economy overflow or extreme loops, limit sell to 100,000 per click
                amountToSell = amountInKho.longValue();
                if (amountToSell > 100000L) amountToSell = 100000L;
                BuiltinModules.ORE_STORAGE.getStorageManager().removeAmount(sp.getIsland().getUniqueId(), clickedMat, BigInteger.valueOf(amountToSell));
            }
        }

        if (amountToSell <= 0) {
            player.sendMessage("\u00a7cB\u1ea1n kh\u00f4ng c\u00f3 " + MarketModule.getVietnameseName(clickedMat) + " \u0111\u1ec3 b\u00e1n!");
            return;
        }

        // Calculate money using current price!
        // To be realistic, if they sell 10,000 items, the price drops *as they sell*.
        // But for simplicity and server performance, we'll use the current price for the whole batch,
        // then update the pool size. It acts as a bulk-sale bonus.
        double currentPrice = info.getCurrentPrice();
        double totalMoney = currentPrice * amountToSell;

        plugin.getProviders().getEconomyProvider().depositMoney(sp, totalMoney);
        info.addPoolSize((int) amountToSell);
        module.saveData();

        player.sendMessage("\u00a7a\u2714 \u0110\u00e3 b\u00e1n " + amountToSell + "x " + MarketModule.getVietnameseName(clickedMat) + " v\u1edbi gi\u00e1 " + String.format("$%.2f", totalMoney));
        try {
            player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP"), 1f, 1f);
        } catch (Exception ex) {
            try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ORB_PICKUP"), 1f, 1f); } catch (Exception ignored) {}
        }
        
        update();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }
}
