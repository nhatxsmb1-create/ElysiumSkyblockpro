package com.bgsoftware.superiorskyblock.module.market;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
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
    private Inventory inventory;
    
    private enum State { MAIN, CATEGORY, ITEM }
    private State currentState = State.MAIN;
    private String currentCategory = null;
    private MarketModule.MarketItemInfo currentItem = null;

    public MarketMenu(MarketModule module, SuperiorSkyblock plugin) {
        this.module = module;
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, (SuperiorSkyblockPlugin) plugin);
    }

    public void open(Player player) {
        openMain(player);
    }

    private void openMain(Player player) {
        currentState = State.MAIN;
        inventory = Bukkit.createInventory(null, 27, "\u00a78\u00a7lS\u00e0n Ch\u1ee9ng Kho\u00e1n T\u00e0i Nguy\u00ean");
        
        ItemStack border = getBorder();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        ItemStack minBtn = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta minMeta = minBtn.getItemMeta();
        if (minMeta != null) {
            minMeta.setDisplayName("\u00a7b\u00a7l\u2728 S\u00c0N KHO\u00c1NG S\u1ea2N \u2728");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Click \u0111\u1ec3 v\u00e0o S\u00e0n Giao D\u1ecbch");
            lore.add("\u00a77c\u00e1c lo\u1ea1i qu\u1eb7ng v\u00e0 kh\u1ed1i kho\u00e1ng s\u1ea3n.");
            minMeta.setLore(lore);
            minBtn.setItemMeta(minMeta);
        }
        inventory.setItem(11, minBtn);

                ItemStack cropBtn;
        try {
            cropBtn = new ItemStack(Material.valueOf("GOLDEN_HOE"));
        } catch (Exception ex) {
            cropBtn = new ItemStack(Material.valueOf("GOLD_HOE"));
        }
        ItemMeta cropMeta = cropBtn.getItemMeta();
        if (cropMeta != null) {
            cropMeta.setDisplayName("\u00a7e\u00a7l\u2728 S\u00c0N N\u00d4NG S\u1ea2N \u2728");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Click \u0111\u1ec3 v\u00e0o S\u00e0n Giao D\u1ecbch");
            lore.add("\u00a77c\u00e1c lo\u1ea1i n\u00f4ng s\u1ea3n \u0111\u1ea3o.");
            cropMeta.setLore(lore);
            cropBtn.setItemMeta(cropMeta);
        }
        inventory.setItem(15, cropBtn);

        player.openInventory(inventory);
    }

    private void openCategory(Player player, String category) {
        currentState = State.CATEGORY;
        currentCategory = category;
        String title = category.equals("MINERAL") ? "\u00a78\u00a7lS\u00e0n Kho\u00e1ng S\u1ea3n" : "\u00a78\u00a7lS\u00e0n N\u00f4ng S\u1ea3n";
        inventory = Bukkit.createInventory(null, 54, title);
        
        ItemStack border = getBorder();
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        inventory.setItem(49, getBackButton());

        int[] innerSlots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
        int index = 0;
        
        for (Map.Entry<String, MarketModule.MarketItemInfo> entry : module.getConfiguration().getItems().entrySet()) {
            MarketModule.MarketItemInfo info = entry.getValue();
            if (!info.getCategory().equalsIgnoreCase(category)) continue;
            if (index >= innerSlots.length) break;
            
            Material mat = info.getMaterial();
            if (mat == null) continue;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("\u00a76\u00a7l" + MarketModule.getVietnameseName(mat));
                List<String> lore = new ArrayList<>();
                double currentPrice = info.getCurrentPrice();
                String status = currentPrice >= info.getBasePrice() ? "\u00a7a\u2b06 \u0110ang c\u00f3 gi\u00e1 (\u0110\u1ec9nh)" : "\u00a7c\u2b07 L\u1ea1m ph\u00e1t (R\u1edbt gi\u00e1)";
                
                lore.add("\u00a78\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
                lore.add("\u00a77Tr\u1ea1ng th\u00e1i: " + status);
                lore.add("\u00a7eGi\u00e1 thu mua: \u00a7a$" + String.format("%.2f", currentPrice));
                lore.add("");
                lore.add("\u00a7a[\u25b6] Click \u0111\u1ec3 Giao D\u1ecbch");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(innerSlots[index++], item);
        }
        player.openInventory(inventory);
    }

    private void openItem(Player player, MarketModule.MarketItemInfo info) {
        currentState = State.ITEM;
        currentItem = info;
        inventory = Bukkit.createInventory(null, 45, "\u00a78\u00a7lGiao D\u1ecbch: " + MarketModule.getVietnameseName(info.getMaterial()));
        
        ItemStack border = getBorder();
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(40, getBackButton());

        // Center item with Sparkline chart
        ItemStack center = new ItemStack(info.getMaterial());
        ItemMeta meta = center.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a76\u00a7l" + MarketModule.getVietnameseName(info.getMaterial()));
            List<String> lore = new ArrayList<>();
            double currentPrice = info.getCurrentPrice();
            
            lore.add("\u00a78\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
            lore.add("\u00a7eGi\u00e1 thu mua hi\u1ec7n t\u1ea1i: \u00a7a$" + String.format("%.2f", currentPrice) + " \u00a77/ c\u00e1i");
            lore.add("");
            lore.add("\u00a77\u25b6 Gi\u00e1 th\u1ecb tr\u01b0\u1eddng g\u1ed1c: \u00a7f$" + info.getBasePrice());
            lore.add("\u00a77\u25b6 Gi\u00e1 tr\u1ea7n (Cao nh\u1ea5t): \u00a7a$" + info.getMaxPrice());
            lore.add("\u00a77\u25b6 Gi\u00e1 s\u00e0n (Th\u1ea5p nh\u1ea5t): \u00a7c$" + info.getMinPrice());
            lore.add("");
            lore.add("\u00a7bS\u1ed1 l\u01b0\u1ee3ng server \u0111\u00e3 b\u00e1n h\u00f4m nay: \u00a7f" + info.getPoolSize());
            lore.add("");
            lore.add("\u00a7d\u25bc Bi\u1ec3u \u0111\u1ed3 bi\u1ebfn \u0111\u1ed9ng gi\u00e1 \u25bc");
            
            // Build sparkline
            List<Double> hist = new ArrayList<>(info.getPriceHistory());
            hist.add(currentPrice); // Add current to end
            StringBuilder sparkline = new StringBuilder();
            String[] blocks = {" ", "\u2581", "\u2582", "\u2583", "\u2584", "\u2585", "\u2586", "\u2587", "\u2588"};
            for (double p : hist) {
                // Normalize 0.0 to 1.0 based on min/max
                double range = info.getMaxPrice() - info.getMinPrice();
                if (range <= 0) range = 1;
                double normalized = (p - info.getMinPrice()) / range;
                if (normalized < 0) normalized = 0;
                if (normalized > 1) normalized = 1;
                int blockIndex = (int) Math.round(normalized * 8);
                
                String color = "\u00a7c"; // Red if low
                if (normalized > 0.4) color = "\u00a7e"; // Yellow mid
                if (normalized > 0.7) color = "\u00a7a"; // Green high
                sparkline.append(color).append(blocks[blockIndex]);
            }
            lore.add(sparkline.toString());
            lore.add("\u00a78\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
            meta.setLore(lore);
            center.setItemMeta(meta);
        }
        inventory.setItem(13, center);

        // Sell from Inv Button
        ItemStack sellInv = new ItemStack(Material.CHEST);
        ItemMeta invMeta = sellInv.getItemMeta();
        if (invMeta != null) {
            invMeta.setDisplayName("\u00a7a\u00a7lB\u00e1n t\u1eeb T\u00fai \u0110\u1ed3");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77B\u00e1n t\u1ea5t c\u1ea3 " + MarketModule.getVietnameseName(info.getMaterial()));
            lore.add("\u00a77\u0111ang c\u00f3 trong T\u00fai \u0110\u1ed3 c\u1ee7a b\u1ea1n.");
            lore.add("");
            lore.add("\u00a7a[\u25b6] Click \u0111\u1ec3 B\u00e1n");
            invMeta.setLore(lore);
            sellInv.setItemMeta(invMeta);
        }
        inventory.setItem(11, sellInv);

        // Sell from Kho Button
        ItemStack sellKho = new ItemStack(Material.ENDER_CHEST);
        ItemMeta khoMeta = sellKho.getItemMeta();
        if (khoMeta != null) {
            khoMeta.setDisplayName("\u00a7d\u00a7lB\u00e1n t\u1eeb Kho \u1ea2o (/is kho)");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77B\u00e1n t\u1ea5t c\u1ea3 " + MarketModule.getVietnameseName(info.getMaterial()));
            lore.add("\u00a77\u0111ang c\u00f3 trong Kho \u1ea2o.");
            lore.add("\u00a7c(T\u1ed1i \u0111a 100k s\u1ea3n ph\u1ea9m / 1 click)");
            lore.add("");
            lore.add("\u00a7a[\u25b6] Click \u0111\u1ec3 B\u00e1n");
            khoMeta.setLore(lore);
            sellKho.setItemMeta(khoMeta);
        }
        inventory.setItem(15, sellKho);

        player.openInventory(inventory);
    }

    private ItemStack getBorder() {
        ItemStack border;
        try {
            border = new ItemStack(Material.valueOf("BLACK_STAINED_GLASS_PANE"));
        } catch (Exception ex) {
            border = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 15);
        }
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            border.setItemMeta(meta);
        }
        return border;
    }

    private ItemStack getBackButton() {
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta meta = back.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a7c\u00a7l\u2b05 Tr\u1edf L\u1ea1i");
            back.setItemMeta(meta);
        }
        return back;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory() != inventory) return; // Note: e.getInventory().equals() can bug out sometimes, != is safer for instance matching. But wait, new Bukkit versions recreate inventory. Let's stick to equals.
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;
        Material clickedMat = e.getCurrentItem().getType();
        if (clickedMat.name().contains("GLASS_PANE")) return;

        Player player = (Player) e.getWhoClicked();

        if (currentState == State.MAIN) {
            if (clickedMat == Material.DIAMOND_PICKAXE) {
                openCategory(player, "MINERAL");
            } else if (clickedMat.name().contains("HOE")) {
                openCategory(player, "CROP");
            }
        } 
        else if (currentState == State.CATEGORY) {
            if (clickedMat == Material.BARRIER) {
                openMain(player);
                return;
            }
            MarketModule.MarketItemInfo info = null;
            for (MarketModule.MarketItemInfo i : module.getConfiguration().getItems().values()) {
                if (i.getMaterial() == clickedMat && i.getCategory().equalsIgnoreCase(currentCategory)) {
                    info = i;
                    break;
                }
            }
            if (info != null) {
                openItem(player, info);
            }
        }
        else if (currentState == State.ITEM) {
            if (clickedMat == Material.BARRIER) {
                openCategory(player, currentCategory);
                return;
            }
            
            if (clickedMat == Material.CHEST || clickedMat == Material.ENDER_CHEST) {
                handleSell(player, currentItem, clickedMat == Material.ENDER_CHEST);
            }
        }
    }

    private void handleSell(Player player, MarketModule.MarketItemInfo info, boolean fromKho) {
        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        long amountToSell = 0;
        Material mat = info.getMaterial();

        if (!fromKho) {
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() == mat) {
                    amountToSell += contents[i].getAmount();
                    player.getInventory().setItem(i, null);
                }
            }
        } else {
            if (sp.getIsland() == null) {
                player.sendMessage("\u00a7cB\u1ea1n ch\u01b0a c\u00f3 \u0111\u1ea3o \u0111\u1ec3 d\u00f9ng kho!");
                return;
            }
            BigInteger amountInKho = BuiltinModules.ORE_STORAGE.getStorageManager().getAmount(sp.getIsland().getUniqueId(), mat);
            if (amountInKho.compareTo(BigInteger.ZERO) > 0) {
                amountToSell = amountInKho.longValue();
                if (amountToSell > 100000L) amountToSell = 100000L;
                BuiltinModules.ORE_STORAGE.getStorageManager().removeAmount(sp.getIsland().getUniqueId(), mat, BigInteger.valueOf(amountToSell));
            }
        }

        if (amountToSell <= 0) {
            player.sendMessage("\u00a7cB\u1ea1n kh\u00f4ng c\u00f3 " + MarketModule.getVietnameseName(mat) + " \u0111\u1ec3 b\u00e1n!");
            return;
        }

        double currentPrice = info.getCurrentPrice();
        double totalMoney = currentPrice * amountToSell;

        plugin.getProviders().getEconomyProvider().depositMoney(sp, totalMoney);
        info.addPoolSize((int) amountToSell);
        module.saveData();

        player.sendMessage("\u00a7a\u2714 \u0110\u00e3 b\u00e1n " + amountToSell + "x " + MarketModule.getVietnameseName(mat) + " v\u1edbi gi\u00e1 " + String.format("$%.2f", totalMoney));
        try {
            player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP"), 1f, 1f);
        } catch (Exception ex) {
            try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ORB_PICKUP"), 1f, 1f); } catch (Exception ignored) {}
        }
        
        // Refresh item view
        openItem(player, info);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inventory)) {
            // Unregister listener when fully closed
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!e.getPlayer().getOpenInventory().getTopInventory().equals(inventory)) {
                    HandlerList.unregisterAll(this);
                }
            }, 2L);
        }
    }
}
