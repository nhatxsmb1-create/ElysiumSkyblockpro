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
    
    private enum State { MAIN, MARKET_CATEGORY, MARKET_ITEM, SHOP_MAIN, SHOP_BUY }
    private State currentState = State.MAIN;
    private String currentCategory = null;
    private MarketModule.MarketItemInfo currentMarketItem = null;
    private MarketModule.Configuration.ShopItemInfo currentShopItem = null;

    public MarketMenu(MarketModule module, SuperiorSkyblock plugin) {
        this.module = module;
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, (SuperiorSkyblockPlugin) plugin);
    }

    public void open(Player player) {
        openMain(player);
    }

    public void openMarket(Player player) {
        openMarketCategory(player, "MINERAL");
    }

    public void openBuyShop(Player player) {
        openShopMain(player, "BUILDING");
    }

    private void openMain(Player player) {
        currentState = State.MAIN;
        inventory = Bukkit.createInventory(null, 27, "\u00a78\u00a7lTrung T\u00e2m Giao Th\u01b0\u01a1ng");
        
        ItemStack border = getBorder();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        ItemStack shopBtn = new ItemStack(Material.CHEST);
        ItemMeta shopMeta = shopBtn.getItemMeta();
        if (shopMeta != null) {
            shopMeta.setDisplayName("\u00a7a\u00a7l\uD83D\uDED2 C\u1eecA H\u00c0NG V\u1eacT PH\u1ea8M (MUA)");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Click \u0111\u1ec3 v\u00e0o C\u1eeda h\u00e0ng mua s\u1eafm");
            lore.add("\u00a77c\u00e1c v\u1eadt ph\u1ea9m x\u00e2y d\u1ef1ng, trang tr\u00ed.");
            shopMeta.setLore(lore);
            shopBtn.setItemMeta(shopMeta);
        }
        inventory.setItem(11, shopBtn);

        ItemStack marketBtn = new ItemStack(Material.ENDER_CHEST);
        ItemMeta marketMeta = marketBtn.getItemMeta();
        if (marketMeta != null) {
            marketMeta.setDisplayName("\u00a76\u00a7l\uD83D\uDCC8 S\u00c0N CH\u1ee8NG KHO\u00c1N (B\u00c1N)");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Click \u0111\u1ec3 v\u00e0o S\u00e0n giao d\u1ecbch");
            lore.add("\u00a77qu\u1eb7ng v\u00e0 n\u00f4ng s\u1ea3n theo th\u1eddi gian th\u1ef1c.");
            marketMeta.setLore(lore);
            marketBtn.setItemMeta(marketMeta);
        }
        inventory.setItem(15, marketBtn);

        player.openInventory(inventory);
    }

    private void openMarketCategory(Player player, String category) {
        currentState = State.MARKET_CATEGORY;
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
        
        // Tab switchers
        inventory.setItem(45, getTabButton(false)); // Switch to Shop
        inventory.setItem(53, getTabButton(true)); // Switch to other market category
        
        if (category.equals("MINERAL")) {
            inventory.getItem(53).getItemMeta().setDisplayName("\u00a7e\u27a4 Chuy\u1ec3n sang S\u00e0n N\u00f4ng S\u1ea3n");
        } else {
            inventory.getItem(53).getItemMeta().setDisplayName("\u00a7b\u27a4 Chuy\u1ec3n sang S\u00e0n Kho\u00e1ng S\u1ea3n");
        }

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
                lore.add("\u00a7a[\u25b6] Click \u0111\u1ec3 B\u00c1N");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(innerSlots[index++], item);
        }
        player.openInventory(inventory);
    }

    private void openShopMain(Player player, String category) {
        currentState = State.SHOP_MAIN;
        currentCategory = category;
        inventory = Bukkit.createInventory(null, 54, "\u00a78\u00a7lC\u1eeda H\u00e0ng: " + category);
        
        ItemStack border = getBorder();
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Category tabs at top
        inventory.setItem(2, getShopCatBtn("BUILDING", Material.COBBLESTONE, "Kh\u1ed1i X\u00e2y D\u1ef1ng", category));
        inventory.setItem(3, getShopCatBtn("DECORATION", Material.SEA_LANTERN, "\u0110\u1ed3 Trang Tr\u00ed", category));
        inventory.setItem(5, getShopCatBtn("TOOLS", Material.DIAMOND_PICKAXE, "C\u00f4ng C\u1ee5", category));
        inventory.setItem(6, getShopCatBtn("RARES", Material.NETHER_STAR, "\u0110\u1ed3 Hi\u1ebfm", category));

        inventory.setItem(49, getBackButton());
        inventory.setItem(53, getTabButton(true)); // Switch to Market
        
        int[] innerSlots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
        int index = 0;
        
        for (Map.Entry<String, MarketModule.Configuration.ShopItemInfo> entry : module.getConfiguration().getShopItems().entrySet()) {
            MarketModule.Configuration.ShopItemInfo info = entry.getValue();
            if (!info.getCategory().equalsIgnoreCase(category)) continue;
            if (index >= innerSlots.length) break;
            
            Material mat = info.getMaterial();
            if (mat == null) continue;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("\u00a7a\u00a7l" + MarketModule.getVietnameseName(mat));
                List<String> lore = new ArrayList<>();
                lore.add("\u00a78\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
                lore.add("\u00a7eGi\u00e1 mua: \u00a7c$" + String.format("%.2f", info.getBuyPrice()));
                lore.add("");
                lore.add("\u00a7a[\u25b6] Click \u0111\u1ec3 MUA");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(innerSlots[index++], item);
        }
        player.openInventory(inventory);
    }

    private ItemStack getShopCatBtn(String id, Material mat, String name, String current) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (id.equals(current)) {
                meta.setDisplayName("\u00a7a\u00a7l\u2728 " + name + " \u2728");
            } else {
                meta.setDisplayName("\u00a77" + name);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack getTabButton(boolean toMarket) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (toMarket) {
                meta.setDisplayName("\u00a76\u27a4 Chuy\u1ec3n sang Ch\u1ee9ng Kho\u00e1n");
            } else {
                meta.setDisplayName("\u00a7a\u27a4 Chuy\u1ec3n sang C\u1eeda H\u00e0ng");
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private void openMarketItem(Player player, MarketModule.MarketItemInfo info) {
        currentState = State.MARKET_ITEM;
        currentMarketItem = info;
        inventory = Bukkit.createInventory(null, 45, "\u00a78\u00a7lB\u00e1n: " + MarketModule.getVietnameseName(info.getMaterial()));
        
        ItemStack border = getBorder();
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(40, getBackButton());

        ItemStack center = new ItemStack(info.getMaterial());
        ItemMeta meta = center.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a76\u00a7l" + MarketModule.getVietnameseName(info.getMaterial()));
            List<String> lore = new ArrayList<>();
            double currentPrice = info.getCurrentPrice();
            lore.add("");
            lore.add("\u00a7eGi\u00e1 thu mua hi\u1ec7n t\u1ea1i: \u00a7a$" + String.format("%.2f", currentPrice) + " \u00a77/ c\u00e1i");
            lore.add("");
            lore.add("\u00a7bS\u1ed1 l\u01b0\u1ee3ng server \u0111\u00e3 b\u00e1n h\u00f4m nay: \u00a7f" + info.getPoolSize());
            lore.add("");
            lore.add("\u00a7d\u25bc Bi\u1ec3u \u0111\u1ed3 bi\u1ebfn \u0111\u1ed9ng gi\u00e1 \u25bc");
            
            List<Double> hist = new ArrayList<>(info.getPriceHistory());
            hist.add(currentPrice);
            while (hist.size() < 15) {
                hist.add(0, info.getBasePrice());
            }
            
            int CHART_HEIGHT = 5;
            int[] heights = new int[15];
            double min = info.getMinPrice();
            double max = info.getMaxPrice();
            double range = max - min;
            if (range <= 0) range = 1;
            
            for (int i = 0; i < 15; i++) {
                double p = hist.get(i);
                double normalized = (p - min) / range;
                int h = (int) Math.round(normalized * CHART_HEIGHT);
                if (h > CHART_HEIGHT) h = CHART_HEIGHT;
                if (h < 1) h = 1;
                heights[i] = h;
            }
            
            for (int row = CHART_HEIGHT; row >= 1; row--) {
                StringBuilder line = new StringBuilder("    ");
                for (int col = 0; col < 15; col++) {
                    if (heights[col] >= row) {
                        String color = "\u00a7c";
                        if (heights[col] >= 3) color = "\u00a7e";
                        if (heights[col] == 5) color = "\u00a7a";
                        line.append(color).append("\u2588");
                    } else {
                        line.append("\u00a78\u2591");
                    }
                }
                lore.add(line.toString());
            }
            lore.add("\u00a78\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
            meta.setLore(lore);
            center.setItemMeta(meta);
        }
        inventory.setItem(13, center);

        inventory.setItem(10, getSellBtn(info, 1, false));
        inventory.setItem(11, getSellBtn(info, 64, false));
        inventory.setItem(12, getSellBtn(info, -1, false)); // -1 means All

        inventory.setItem(14, getSellBtn(info, 64, true));
        inventory.setItem(15, getSellBtn(info, -1, true));

        player.openInventory(inventory);
    }

    private void openShopBuy(Player player, MarketModule.Configuration.ShopItemInfo info) {
        currentState = State.SHOP_BUY;
        currentShopItem = info;
        inventory = Bukkit.createInventory(null, 45, "\u00a78\u00a7lMua: " + MarketModule.getVietnameseName(info.getMaterial()));
        
        ItemStack border = getBorder();
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, border);
        }

        inventory.setItem(40, getBackButton());

        inventory.setItem(10, getBuyBtn(info, 1));
        inventory.setItem(12, getBuyBtn(info, 64));
        inventory.setItem(14, getBuyBtn(info, 128));
        inventory.setItem(16, getBuyBtn(info, 192));

        player.openInventory(inventory);
    }

    private ItemStack getBuyBtn(MarketModule.Configuration.ShopItemInfo info, int amount) {
        ItemStack item = new ItemStack(info.getMaterial(), amount > 64 ? 64 : amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a7a\u00a7lMua x" + amount);
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Gi\u00e1 ph\u1ea3i tr\u1ea3: \u00a7c$" + String.format("%.2f", info.getBuyPrice() * amount));
            lore.add("");
            lore.add("\u00a7a[\u25b6] Click \u0111\u1ec3 thanh to\u00e1n");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
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
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;
        Material clickedMat = e.getCurrentItem().getType();
        if (clickedMat.name().contains("GLASS_PANE")) return;

        Player player = (Player) e.getWhoClicked();

        if (currentState == State.MAIN) {
            if (clickedMat == Material.CHEST) openShopMain(player, "BUILDING");
            else if (clickedMat == Material.ENDER_CHEST) openMarketCategory(player, "MINERAL");
        } 
        else if (currentState == State.MARKET_CATEGORY) {
            if (clickedMat == Material.BARRIER) { openMain(player); return; }
            if (clickedMat == Material.PAPER) {
                if (e.getSlot() == 45) openShopMain(player, "BUILDING");
                else openMarketCategory(player, currentCategory.equals("MINERAL") ? "CROP" : "MINERAL");
                return;
            }
            MarketModule.MarketItemInfo info = null;
            for (MarketModule.MarketItemInfo i : module.getConfiguration().getItems().values()) {
                if (i.getMaterial() == clickedMat && i.getCategory().equalsIgnoreCase(currentCategory)) { info = i; break; }
            }
            if (info != null) openMarketItem(player, info);
        }
        else if (currentState == State.MARKET_ITEM) {
            if (clickedMat == Material.BARRIER) { openMarketCategory(player, currentCategory); return; }
            if (clickedMat == Material.CHEST || clickedMat == Material.ENDER_CHEST) {
                int amount = -1;
                if (e.getSlot() == 10) amount = 1;
                if (e.getSlot() == 11 || e.getSlot() == 14) amount = 64;
                handleSell(player, currentMarketItem, clickedMat == Material.ENDER_CHEST, amount);
            }
        }
        else if (currentState == State.SHOP_MAIN) {
            if (clickedMat == Material.BARRIER) { openMain(player); return; }
            if (clickedMat == Material.PAPER && e.getSlot() == 53) { openMarketCategory(player, "MINERAL"); return; }
            if (e.getSlot() == 2) { openShopMain(player, "BUILDING"); return; }
            if (e.getSlot() == 3) { openShopMain(player, "DECORATION"); return; }
            if (e.getSlot() == 5) { openShopMain(player, "TOOLS"); return; }
            if (e.getSlot() == 6) { openShopMain(player, "RARES"); return; }
            
            MarketModule.Configuration.ShopItemInfo info = null;
            for (MarketModule.Configuration.ShopItemInfo i : module.getConfiguration().getShopItems().values()) {
                if (i.getMaterial() == clickedMat && i.getCategory().equalsIgnoreCase(currentCategory)) { info = i; break; }
            }
            if (info != null) openShopBuy(player, info);
        }
        else if (currentState == State.SHOP_BUY) {
            if (clickedMat == Material.BARRIER) { openShopMain(player, currentCategory); return; }
            if (clickedMat == currentShopItem.getMaterial()) {
                int amount = 1;
                if (e.getSlot() == 12) amount = 64;
                if (e.getSlot() == 14) amount = 128;
                if (e.getSlot() == 16) amount = 192;
                handleBuy(player, currentShopItem, amount);
            }
        }
    }

    private void handleBuy(Player player, MarketModule.Configuration.ShopItemInfo info, int amount) {
        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        double price = info.getBuyPrice() * amount;
        
        if (plugin.getProviders().getEconomyProvider().getBalance(sp).doubleValue() < price) {
            player.sendMessage("\u00a7cB\u1ea1n kh\u00f4ng \u0111\u1ee7 $" + String.format("%.2f", price) + " \u0111\u1ec3 mua!");
            return;
        }

        // Check inventory space
        int freeSpace = 0;
        Material mat = info.getMaterial();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                freeSpace += mat.getMaxStackSize();
            } else if (item.getType() == mat) {
                freeSpace += (mat.getMaxStackSize() - item.getAmount());
            }
        }

        if (freeSpace < amount) {
            player.sendMessage("\u00a7cT\u00fai \u0111\u1ed3 c\u1ee7a b\u1ea1n kh\u00f4ng c\u00f2n \u0111\u1ee7 ch\u1ed7 tr\u1ed1ng!");
            return;
        }

                plugin.getProviders().getEconomyProvider().withdrawMoney(sp, price);
        int remaining = amount;
        while (remaining > 0) {
            int current = Math.min(64, remaining);
            player.getInventory().addItem(new ItemStack(mat, current));
            remaining -= current;
        }
        player.sendMessage("\u00a7a\u2714 Mua th\u00e0nh c\u00f4ng " + amount + "x " + MarketModule.getVietnameseName(mat) + " v\u1edbi gi\u00e1 $" + String.format("%.2f", price));
        
        try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP"), 1f, 1f); } 
        catch (Exception ex) { try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ORB_PICKUP"), 1f, 1f); } catch (Exception ignored) {} }
    }

    private ItemStack getSellBtn(MarketModule.MarketItemInfo info, int amount, boolean fromKho) {
        Material icon = fromKho ? Material.ENDER_CHEST : Material.CHEST;
        String amountStr = amount == -1 ? "Tất Cả" : "x" + amount;
        String fromStr = fromKho ? "Kho Ảo" : "Túi Đồ";
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String color = fromKho ? "§d§l" : "§a§l";
            meta.setDisplayName(color + "Bán " + amountStr + " (" + fromStr + ")");
            List<String> lore = new ArrayList<>();
            lore.add("§7Bán " + amountStr + " " + MarketModule.getVietnameseName(info.getMaterial()));
            lore.add("§7từ " + fromStr + " của bạn vào Sàn.");
            lore.add("");
            lore.add("§a[▶] Click để Bán");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void handleSell(Player player, MarketModule.MarketItemInfo info, boolean fromKho, int exactAmount) {
        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        long amountToSell = 0;
        Material mat = info.getMaterial();

        if (!fromKho) {
            if (exactAmount == -1) {
                ItemStack[] contents = player.getInventory().getContents();
                for (int i = 0; i < contents.length; i++) {
                    if (contents[i] != null && contents[i].getType() == mat) {
                        amountToSell += contents[i].getAmount();
                        player.getInventory().setItem(i, null);
                    }
                }
            } else {
                int count = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == mat) count += item.getAmount();
                }
                if (count < exactAmount) {
                    player.sendMessage("§cBạn không có đủ " + exactAmount + "x " + MarketModule.getVietnameseName(mat) + "!");
                    return;
                }
                int remaining = exactAmount;
                ItemStack[] contents = player.getInventory().getContents();
                for (int i = 0; i < contents.length; i++) {
                    if (contents[i] != null && contents[i].getType() == mat) {
                        if (contents[i].getAmount() <= remaining) {
                            remaining -= contents[i].getAmount();
                            player.getInventory().setItem(i, null);
                        } else {
                            contents[i].setAmount(contents[i].getAmount() - remaining);
                            remaining = 0;
                        }
                        if (remaining <= 0) break;
                    }
                }
                amountToSell = exactAmount;
            }
        } else {
            if (sp.getIsland() == null) { player.sendMessage("§cBạn chưa có đảo!"); return; }
            BigInteger amountInKho = BuiltinModules.ORE_STORAGE.getStorageManager().getAmount(sp.getIsland().getUniqueId(), mat);
            if (amountInKho.compareTo(BigInteger.ZERO) > 0) {
                if (exactAmount == -1) {
                    amountToSell = amountInKho.longValue();
                    if (amountToSell > 100000L) amountToSell = 100000L;
                } else {
                    if (amountInKho.compareTo(BigInteger.valueOf(exactAmount)) < 0) {
                        player.sendMessage("§cKho Ảo của bạn không đủ " + exactAmount + "x " + MarketModule.getVietnameseName(mat) + "!");
                        return;
                    }
                    amountToSell = exactAmount;
                }
                BuiltinModules.ORE_STORAGE.getStorageManager().removeAmount(sp.getIsland().getUniqueId(), mat, BigInteger.valueOf(amountToSell));
            } else {
                player.sendMessage("§cKho Ảo của bạn không còn " + MarketModule.getVietnameseName(mat) + "!");
                return;
            }
        }

        if (amountToSell <= 0) { player.sendMessage("§cBạn không có " + MarketModule.getVietnameseName(mat) + " để bán!"); return; }

        double currentPrice = info.getCurrentPrice();
        double totalMoney = currentPrice * amountToSell;

        plugin.getProviders().getEconomyProvider().depositMoney(sp, totalMoney);
        info.addPoolSize((int) amountToSell);
        module.saveData();

        player.sendMessage("§a✔ Đã bán " + amountToSell + "x " + MarketModule.getVietnameseName(mat) + " với giá $" + String.format("%.2f", totalMoney));

        if (amountToSell >= 10000 || totalMoney >= 100000) {
            Bukkit.getServer().broadcastMessage("");
            Bukkit.getServer().broadcastMessage("§b▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄");
            Bukkit.getServer().broadcastMessage("§c§l⚠ CẢNH BÁO CÁ MẬP XẢ HÀNG ⚠");
            Bukkit.getServer().broadcastMessage("§eĐại gia §a" + player.getName() + " §evừa xả §f" + amountToSell + "x " + MarketModule.getVietnameseName(mat) + " §evào thị trường!");
            Bukkit.getServer().broadcastMessage("§7➤ Giá " + MarketModule.getVietnameseName(mat) + " đang rớt! Anh em cẩn thận!");
            Bukkit.getServer().broadcastMessage("§b▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄");
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                try { p.playSound(p.getLocation(), org.bukkit.Sound.valueOf("ENTITY_ENDER_DRAGON_GROWL"), 0.5f, 1.5f); } 
                catch (Exception ex) { try { p.playSound(p.getLocation(), org.bukkit.Sound.valueOf("ENDERDRAGON_GROWL"), 0.5f, 1.5f); } catch (Exception ignored) {} }
            }
            try {
                Class<?> particleClass = Class.forName("org.bukkit.Particle");
                Object totem = Enum.valueOf((Class<Enum>) particleClass, "TOTEM");
                player.getWorld().getClass().getMethod("spawnParticle", particleClass, org.bukkit.Location.class, int.class, double.class, double.class, double.class, double.class)
                    .invoke(player.getWorld(), totem, player.getLocation().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.1);
            } catch (Exception ignored) {}
        } else {
            try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 0.5f, 2f); } 
            catch (Exception ex) { try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("LEVEL_UP"), 0.5f, 2f); } catch (Exception ignored) {} }
        }
        openMarketItem(player, info);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inventory)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!e.getPlayer().getOpenInventory().getTopInventory().equals(inventory)) {
                    HandlerList.unregisterAll(this);
                }
            }, 2L);
        }
    }
}
