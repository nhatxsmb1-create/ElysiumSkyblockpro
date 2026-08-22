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
    
    private enum State { HUB, SHOP_CATS, SHOP_ITEMS, SHOP_BUY, MARKET_CATS, MARKET_ITEMS, MARKET_SELL }
    private State currentState = State.HUB;
    private String currentCategory = null;
    private int currentPage = 0;
    
    private MarketModule.MarketItemInfo currentMarketItem = null;
    private MarketModule.Configuration.ShopItemInfo currentShopItem = null;

    public MarketMenu(MarketModule module, SuperiorSkyblock plugin) {
        this.module = module;
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, (SuperiorSkyblockPlugin) plugin);
    }

    public void open(Player player) { openHub(player); }
    public void openMarket(Player player) { openMarketCats(player); }
    public void openBuyShop(Player player) { openShopCats(player); }

    // ================= HUB =================
    private void openHub(Player player) {
        currentState = State.HUB;
        inventory = Bukkit.createInventory(null, 27, "\u00a78\u00a7lTrung T\u00e2m Giao Th\u01b0\u01a1ng");
        fillBorders(inventory, 27);

        ItemStack shopBtn = new ItemStack(Material.CHEST);
        ItemMeta shopMeta = shopBtn.getItemMeta();
        if (shopMeta != null) {
            shopMeta.setDisplayName("\u00a7a\u00a7l\u27A4 C\u1eecA H\u00c0NG V\u1eacT PH\u1ea8M (MUA)");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Click \u0111\u1ec3 mua s\u1eafm c\u00e1c v\u1eadt ph\u1ea9m");
            lore.add("\u00a77x\u00e2y d\u1ef1ng, trang tr\u00ed, c\u00f4ng c\u1ee5...");
            shopMeta.setLore(lore);
            shopBtn.setItemMeta(shopMeta);
        }
        inventory.setItem(11, shopBtn);

        ItemStack marketBtn = new ItemStack(Material.ENDER_CHEST);
        ItemMeta marketMeta = marketBtn.getItemMeta();
        if (marketMeta != null) {
            marketMeta.setDisplayName("\u00a76\u00a7l\u27A4 S\u00c0N CH\u1ee8NG KHO\u00c1N (B\u00c1N)");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Click \u0111\u1ec3 \u0111\u1ea7u t\u01b0 v\u00e0 b\u00e1n qu\u1eb7ng,");
            lore.add("\u00a77n\u00f4ng s\u1ea3n theo th\u1eddi gian th\u1ef1c.");
            marketMeta.setLore(lore);
            marketBtn.setItemMeta(marketMeta);
        }
        inventory.setItem(15, marketBtn);

        ItemStack thuongVuBtn = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta thuongVuMeta = thuongVuBtn.getItemMeta();
        if (thuongVuMeta != null) {
            thuongVuMeta.setDisplayName("\u00a7e\u00a7l\u272a TH\u01af\u01a0NG V\u1ee4 B\u1ea0C T\u1ef6 \u272a");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Click \u0111\u1ec3 xem H\u1ee3p \u0111\u1ed3ng");
            lore.add("\u00a77thu mua To\u00e0n M\u00e1y Ch\u1ee7 h\u00f4m nay.");
            thuongVuMeta.setLore(lore);
            thuongVuBtn.setItemMeta(thuongVuMeta);
        }
        inventory.setItem(13, thuongVuBtn);

        player.openInventory(inventory);
    }

    // ================= SHOP CATEGORIES =================
    private void openShopCats(Player player) {
        currentState = State.SHOP_CATS;
        inventory = Bukkit.createInventory(null, 45, "\u00a78\u00a7lC\u1eeda H\u00e0ng: Danh M\u1ee5c");
        fillBorders(inventory, 45);

        Material bMat = Material.BRICK;
        try { bMat = Material.valueOf("BRICKS"); } catch(Exception e) {}
        
        inventory.setItem(19, getCatBtn("BUILDING", bMat, "Kh\u1ed1i X\u00e2y D\u1ef1ng"));
        inventory.setItem(21, getCatBtn("DECORATION", Material.PAINTING, "\u0110\u1ed3 Trang Tr\u00ed"));
        inventory.setItem(23, getCatBtn("TOOLS", Material.DIAMOND_PICKAXE, "C\u00f4ng C\u1ee5"));
        
        Material rMat = Material.NETHER_STAR;
        inventory.setItem(25, getCatBtn("RARES", rMat, "\u0110\u1ed3 Hi\u1ebfm"));

        setupNavigationBar(inventory, 45, false, false, false);
        player.openInventory(inventory);
    }

    // ================= SHOP ITEMS =================
    private void openShopItems(Player player, String category, int page) {
        currentState = State.SHOP_ITEMS;
        currentCategory = category;
        currentPage = page;
        inventory = Bukkit.createInventory(null, 54, "\u00a78\u00a7lC\u1eeda H\u00e0ng: " + category);
        
        List<MarketModule.Configuration.ShopItemInfo> items = new ArrayList<>();
        for (MarketModule.Configuration.ShopItemInfo info : module.getConfiguration().getShopItems().values()) {
            if (info.getCategory().equalsIgnoreCase(category)) items.add(info);
        }

        int totalPages = (int) Math.ceil(items.size() / 45.0);
        if (totalPages == 0) totalPages = 1;
        
        for (int i = 0; i < 45; i++) {
            int idx = page * 45 + i;
            if (idx < items.size()) {
                MarketModule.Configuration.ShopItemInfo info = items.get(idx);
                ItemStack item = new ItemStack(info.getMaterial());
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("\u00a7a\u00a7l" + MarketModule.getVietnameseName(info.getMaterial()));
                    List<String> lore = new ArrayList<>();
                    lore.add("\u00a78\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
                    lore.add("\u00a7eGi\u00e1 mua: \u00a7c$" + String.format("%.2f", info.getBuyPrice()));
                    lore.add("");
                    lore.add("\u00a7a[\u25b6] Click \u0111\u1ec3 Xem t\u00f9y ch\u1ecdn Mua");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(i, item);
            }
        }

        setupNavigationBar(inventory, 54, page > 0, page < totalPages - 1, false);
        player.openInventory(inventory);
    }

    // ================= SHOP BUY CONFIRM =================
    private void openShopBuy(Player player, MarketModule.Configuration.ShopItemInfo info) {
        currentState = State.SHOP_BUY;
        currentShopItem = info;
        inventory = Bukkit.createInventory(null, 45, "\u00a78\u00a7lMua: " + MarketModule.getVietnameseName(info.getMaterial()));
        fillBorders(inventory, 45);

        inventory.setItem(13, getDisplayItem(info.getMaterial(), "\u00a7a\u00a7l" + MarketModule.getVietnameseName(info.getMaterial()), "\u00a7eGi\u00e1 g\u1ed1c: \u00a7c$" + info.getBuyPrice()));
        
        inventory.setItem(28, getBuyBtn(info, 1));
        inventory.setItem(30, getBuyBtn(info, 64));
        inventory.setItem(32, getBuyBtn(info, 128));
        inventory.setItem(34, getBuyBtn(info, 192));

        setupNavigationBar(inventory, 45, false, false, false);
        player.openInventory(inventory);
    }

    // ================= MARKET CATEGORIES =================
    private void openMarketCats(Player player) {
        currentState = State.MARKET_CATS;
        inventory = Bukkit.createInventory(null, 45, "\u00a78\u00a7lCh\u1ee9ng Kho\u00e1n: Danh M\u1ee5c");
        fillBorders(inventory, 45);

        inventory.setItem(20, getCatBtn("MINERAL", Material.DIAMOND_ORE, "S\u00e0n Kho\u00e1ng S\u1ea3n"));
        
        Material cMat = Material.WHEAT;
        inventory.setItem(24, getCatBtn("CROP", cMat, "S\u00e0n N\u00f4ng S\u1ea3n"));

        setupNavigationBar(inventory, 45, false, false, true);
        player.openInventory(inventory);
    }

    // ================= MARKET ITEMS =================
    private void openMarketItems(Player player, String category, int page) {
        currentState = State.MARKET_ITEMS;
        currentCategory = category;
        currentPage = page;
        String title = category.equals("MINERAL") ? "\u00a78\u00a7lS\u00e0n Kho\u00e1ng S\u1ea3n" : "\u00a78\u00a7lS\u00e0n N\u00f4ng S\u1ea3n";
        inventory = Bukkit.createInventory(null, 54, title);
        
        List<MarketModule.MarketItemInfo> items = new ArrayList<>();
        for (MarketModule.MarketItemInfo info : module.getConfiguration().getItems().values()) {
            if (info.getCategory().equalsIgnoreCase(category)) items.add(info);
        }

        int totalPages = (int) Math.ceil(items.size() / 45.0);
        if (totalPages == 0) totalPages = 1;
        
        for (int i = 0; i < 45; i++) {
            int idx = page * 45 + i;
            if (idx < items.size()) {
                MarketModule.MarketItemInfo info = items.get(idx);
                ItemStack item = new ItemStack(info.getMaterial());
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("\u00a76\u00a7l" + MarketModule.getVietnameseName(info.getMaterial()));
                    List<String> lore = new ArrayList<>();
                    double currentPrice = info.getCurrentPrice();
                    String status = currentPrice >= info.getBasePrice() ? "\u00a7a\u2b06 \u0110ang c\u00f3 gi\u00e1 (\u0110\u1ec9nh)" : "\u00a7c\u2b07 L\u1ea1m ph\u00e1t (R\u1edbt gi\u00e1)";
                    
                    lore.add("\u00a78\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
                    lore.add("\u00a77Tr\u1ea1ng th\u00e1i: " + status);
                    lore.add("\u00a7eGi\u00e1 thu mua: \u00a7a$" + String.format("%.2f", currentPrice));
                    lore.add("");
                    lore.add("\u00a77\u2191 Gi\u00e1 Đ\u1ec9nh: \u00a7a$" + String.format("%.2f", info.getMaxPrice()));
                    lore.add("\u00a77\u2193 Gi\u00e1 Đ\u00e1y: \u00a7c$" + String.format("%.2f", info.getMinPrice()));
                    lore.add("");
                    lore.add("\u00a7a[\u25b6] Click \u0111\u1ec3 Xem bi\u1ec3u \u0111\u1ed3 & B\u00c1N");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(i, item);
            }
        }

        setupNavigationBar(inventory, 54, page > 0, page < totalPages - 1, true);
        player.openInventory(inventory);
    }

    // ================= MARKET SELL CONFIRM =================
    private void openMarketSell(Player player, MarketModule.MarketItemInfo info) {
        currentState = State.MARKET_SELL;
        currentMarketItem = info;
        inventory = Bukkit.createInventory(null, 54, "\u00a78\u00a7lB\u00e1n: " + MarketModule.getVietnameseName(info.getMaterial()));
        fillBorders(inventory, 54);

        ItemStack center = new ItemStack(info.getMaterial());
        ItemMeta meta = center.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a76\u00a7l" + MarketModule.getVietnameseName(info.getMaterial()));
            List<String> lore = new ArrayList<>();
            double currentPrice = info.getCurrentPrice();
            lore.add("");
            lore.add("\u00a7eGi\u00e1 thu mua hi\u1ec7n t\u1ea1i: \u00a7a$" + String.format("%.2f", currentPrice) + " \u00a77/ c\u00e1i");
            lore.add("\u00a7bS\u1ed1 l\u01b0\u1ee3ng server \u0111\u00e3 b\u00e1n: \u00a7f" + info.getPoolSize());
            lore.add("");
            lore.add("\u00a77\u2191 Gi\u00e1 Đ\u1ec9nh (Cao nh\u1ea5t): \u00a7a$" + String.format("%.2f", info.getMaxPrice()));
            lore.add("\u00a77\u2193 Gi\u00e1 Đ\u00e1y (Th\u1ea5p nh\u1ea5t): \u00a7c$" + String.format("%.2f", info.getMinPrice()));
            lore.add("");
            lore.add("\u00a7d\u25bc Bi\u1ec3u \u0111\u1ed3 bi\u1ebfn \u0111\u1ed9ng gi\u00e1 \u25bc");
            
            List<Double> hist = new ArrayList<>(info.getPriceHistory());
            hist.add(currentPrice);
            while (hist.size() < 15) { hist.add(0, info.getBasePrice()); }
            
            double min = info.getMinPrice();
            double max = info.getMaxPrice();
            double base = info.getBasePrice();
            
            StringBuilder sparkline = new StringBuilder("  ");
            char[] blocks = new char[]{'_', '\u2581', '\u2582', '\u2583', '\u2584', '\u2585', '\u2586', '\u2588'};
            
            for (int i = 0; i < 15; i++) {
                double p = hist.get(i);
                double normalized = 0.5;
                if (p >= base) {
                    double range = max - base;
                    if (range <= 0) range = 1;
                    normalized = 0.5 + 0.5 * (p - base) / range;
                } else {
                    double range = base - min;
                    if (range <= 0) range = 1;
                    normalized = 0.5 * (p - min) / range;
                }
                if (normalized < 0) normalized = 0;
                if (normalized > 1) normalized = 1;
                
                String color = "\u00a7c";
                if (p == base) color = "\u00a7e";
                else if (p > base) color = "\u00a7a";
                
                int idx = (int) Math.round(normalized * 7);
                if (idx < 0) idx = 0;
                if (idx > 7) idx = 7;
                
                sparkline.append(color).append(blocks[idx]);
            }
            lore.add(sparkline.toString());
            
            lore.add("\u00a78\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
            meta.setLore(lore);
            center.setItemMeta(meta);
        }
        inventory.setItem(13, center);

        inventory.setItem(28, getSellBtn(info, 1, false));
        inventory.setItem(29, getSellBtn(info, 64, false));
        inventory.setItem(30, getSellBtn(info, -1, false));
        
        inventory.setItem(32, getSellBtn(info, 64, true));
        inventory.setItem(33, getSellBtn(info, -1, true));

        setupNavigationBar(inventory, 54, false, false, true);
        player.openInventory(inventory);
    }

    // ================= HELPERS =================
    private void fillBorders(Inventory inv, int size) {
        ItemStack border = getBorder(false);
        ItemStack accent = getBorder(true);
        for (int i = 0; i < size; i++) {
            if (i < 9 || i > size - 10 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, (i < 9 || i > size - 10) ? accent : border);
            }
        }
    }

    private void setupNavigationBar(Inventory inv, int size, boolean hasPrev, boolean hasNext, boolean isMarket) {
        int row = size / 9 - 1;
        int base = row * 9;
        
        ItemStack border = getBorder(true);
        for (int i = 0; i < 9; i++) {
            inv.setItem(base + i, border);
        }
        
        // Pagination (Arrows)
        if (hasPrev) inv.setItem(base + 2, getNavBtn("\u00a7e\u25c0 Trang Tr\u01b0\u1edbc", Material.ARROW));
        if (hasNext) inv.setItem(base + 6, getNavBtn("\u00a7eTrang T\u1edbi \u25b6", Material.ARROW));
        
        // Back / Close
        inv.setItem(base + 4, getNavBtn("\u00a7c\u00a7l\u2716 TR\u1ede L\u1ea0I", Material.BARRIER));
        
        // Tab Switcher (Paper at the corner)
        if (isMarket) {
            inv.setItem(base + 8, getNavBtn("\u00a7a\u00a7l\u21c4 Chuy\u1ec3n sang C\u1eecA H\u00c0NG", Material.PAPER));
        } else {
            inv.setItem(base + 8, getNavBtn("\u00a76\u00a7l\u21c4 Chuy\u1ec3n sang CH\u1ee8NG KHO\u00c1N", Material.PAPER));
        }
    }

    private ItemStack getBorder(boolean accent) {
        ItemStack border;
        try { border = new ItemStack(Material.valueOf(accent ? "CYAN_STAINED_GLASS_PANE" : "GRAY_STAINED_GLASS_PANE")); } 
        catch (Exception ex) { border = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) (accent ? 9 : 7)); }
        ItemMeta meta = border.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); border.setItemMeta(meta); }
        return border;
    }

    private ItemStack getNavBtn(String name, Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }

    private ItemStack getDisplayItem(Material mat, String name, String loreLine) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(loreLine);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack getCatBtn(String id, Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a7a\u00a7l" + name);
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Click \u0111\u1ec3 xem danh s\u00e1ch.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
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

    private ItemStack getSellBtn(MarketModule.MarketItemInfo info, int amount, boolean fromKho) {
        Material icon = fromKho ? Material.ENDER_CHEST : Material.CHEST;
        String amountStr = amount == -1 ? "T\u1ea5t C\u1ea3" : "x" + amount;
        String fromStr = fromKho ? "Kho \u0110\u1ea3o (/is kho)" : "T\u00fai \u0110\u1ed3";
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String color = fromKho ? "\u00a7d\u00a7l" : "\u00a7a\u00a7l";
            meta.setDisplayName(color + "B\u00e1n " + amountStr + " (" + fromStr + ")");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77B\u00e1n " + amountStr + " " + MarketModule.getVietnameseName(info.getMaterial()));
            lore.add("\u00a77t\u1eeb " + fromStr + " c\u1ee7a b\u1ea1n v\u00e0o S\u00e0n.");
            lore.add("");
            lore.add("\u00a7a[\u25b6] Click \u0111\u1ec3 B\u00e1n");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    // ================= EVENT HANDLER =================
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;
        Material clickedMat = e.getCurrentItem().getType();
        if (clickedMat.name().contains("GLASS_PANE")) return;

        Player player = (Player) e.getWhoClicked();
        int slot = e.getSlot();
        int size = inventory.getSize();
        int row = size / 9 - 1;
        int base = row * 9;

        // Global Navigation Bar Clicks
        if (slot == base + 8 && clickedMat == Material.PAPER) {
            if (currentState == State.SHOP_CATS || currentState == State.SHOP_ITEMS || currentState == State.SHOP_BUY) {
                openMarketCats(player);
            } else {
                openShopCats(player);
            }
            return;
        }
        
        if (slot == base + 4 && clickedMat == Material.BARRIER) {
            if (currentState == State.SHOP_CATS || currentState == State.MARKET_CATS) openHub(player);
            else if (currentState == State.SHOP_ITEMS) openShopCats(player);
            else if (currentState == State.SHOP_BUY) openShopItems(player, currentCategory, currentPage);
            else if (currentState == State.MARKET_ITEMS) openMarketCats(player);
            else if (currentState == State.MARKET_SELL) openMarketItems(player, currentCategory, currentPage);
            return;
        }
        
        if (slot == base + 2 && clickedMat == Material.ARROW) {
            if (currentState == State.SHOP_ITEMS) openShopItems(player, currentCategory, currentPage - 1);
            if (currentState == State.MARKET_ITEMS) openMarketItems(player, currentCategory, currentPage - 1);
            return;
        }
        
        if (slot == base + 6 && clickedMat == Material.ARROW) {
            if (currentState == State.SHOP_ITEMS) openShopItems(player, currentCategory, currentPage + 1);
            if (currentState == State.MARKET_ITEMS) openMarketItems(player, currentCategory, currentPage + 1);
            return;
        }

        // State Specific Clicks
        if (currentState == State.HUB) {
            if (slot == 11) openShopCats(player);
            else if (slot == 15) openMarketCats(player);
            else if (slot == 13) { new DealMenu(module, plugin).open(player); }
        }
        else if (currentState == State.SHOP_CATS) {
            if (slot == 19) openShopItems(player, "BUILDING", 0);
            if (slot == 21) openShopItems(player, "DECORATION", 0);
            if (slot == 23) openShopItems(player, "TOOLS", 0);
            if (slot == 25) openShopItems(player, "RARES", 0);
        }
        else if (currentState == State.SHOP_ITEMS) {
            if (slot < 45) {
                MarketModule.Configuration.ShopItemInfo info = null;
                for (MarketModule.Configuration.ShopItemInfo i : module.getConfiguration().getShopItems().values()) {
                    if (i.getMaterial() == clickedMat && i.getCategory().equalsIgnoreCase(currentCategory)) { info = i; break; }
                }
                if (info != null) openShopBuy(player, info);
            }
        }
        else if (currentState == State.SHOP_BUY) {
            if (slot == 28) handleBuy(player, currentShopItem, 1);
            if (slot == 30) handleBuy(player, currentShopItem, 64);
            if (slot == 32) handleBuy(player, currentShopItem, 128);
            if (slot == 34) handleBuy(player, currentShopItem, 192);
        }
        else if (currentState == State.MARKET_CATS) {
            if (slot == 20) openMarketItems(player, "MINERAL", 0);
            if (slot == 24) openMarketItems(player, "CROP", 0);
        }
        else if (currentState == State.MARKET_ITEMS) {
            if (slot < 45) {
                MarketModule.MarketItemInfo info = null;
                for (MarketModule.MarketItemInfo i : module.getConfiguration().getItems().values()) {
                    if (i.getMaterial() == clickedMat && i.getCategory().equalsIgnoreCase(currentCategory)) { info = i; break; }
                }
                if (info != null) openMarketSell(player, info);
            }
        }
        else if (currentState == State.MARKET_SELL) {
            if (slot == 28) handleSell(player, currentMarketItem, false, 1);
            if (slot == 29) handleSell(player, currentMarketItem, false, 64);
            if (slot == 30) handleSell(player, currentMarketItem, false, -1);
            if (slot == 32) handleSell(player, currentMarketItem, true, 64);
            if (slot == 33) handleSell(player, currentMarketItem, true, -1);
        }
    }

    private void handleBuy(Player player, MarketModule.Configuration.ShopItemInfo info, int amount) {
        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        double price = info.getBuyPrice() * amount;
        
        if (plugin.getProviders().getEconomyProvider().getBalance(sp).doubleValue() < price) {
            player.sendMessage("\u00a7cB\u1ea1n kh\u00f4ng \u0111\u1ee7 $" + String.format("%.2f", price) + " \u0111\u1ec3 mua!");
            return;
        }

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
                    player.sendMessage("\u00a7cB\u1ea1n kh\u00f4ng c\u00f3 \u0111\u1ee7 " + exactAmount + "x " + MarketModule.getVietnameseName(mat) + "!");
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
            if (sp.getIsland() == null) { player.sendMessage("\u00a7cB\u1ea1n ch\u01b0a c\u00f3 \u0111\u1ea3o!"); return; }
            BigInteger amountInKho = BuiltinModules.ORE_STORAGE.getStorageManager().getAmount(sp.getIsland().getUniqueId(), mat);
            if (amountInKho.compareTo(BigInteger.ZERO) > 0) {
                if (exactAmount == -1) {
                    amountToSell = amountInKho.longValue();
                    if (amountToSell > 100000L) amountToSell = 100000L;
                } else {
                    if (amountInKho.compareTo(BigInteger.valueOf(exactAmount)) < 0) {
                        player.sendMessage("\u00a7cKho \u0110\u1ea3o c\u1ee7a b\u1ea1n kh\u00f4ng \u0111\u1ee7 " + exactAmount + "x " + MarketModule.getVietnameseName(mat) + "!");
                        return;
                    }
                    amountToSell = exactAmount;
                }
                BuiltinModules.ORE_STORAGE.getStorageManager().removeAmount(sp.getIsland().getUniqueId(), mat, BigInteger.valueOf(amountToSell));
            } else {
                player.sendMessage("\u00a7cKho \u0110\u1ea3o c\u1ee7a b\u1ea1n kh\u00f4ng c\u00f2n " + MarketModule.getVietnameseName(mat) + "!");
                return;
            }
        }

        if (amountToSell <= 0) { player.sendMessage("\u00a7cB\u1ea1n kh\u00f4ng c\u00f3 " + MarketModule.getVietnameseName(mat) + " \u0111\u1ec3 b\u00e1n!"); return; }

        double currentPrice = info.getCurrentPrice();
        double totalMoney = currentPrice * amountToSell;

        plugin.getProviders().getEconomyProvider().depositMoney(sp, totalMoney);
        info.addPoolSize((int) amountToSell);
        module.saveData();

        player.sendMessage("\u00a7a\u2714 \u0110\u00e3 b\u00e1n " + amountToSell + "x " + MarketModule.getVietnameseName(mat) + " v\u1edbi gi\u00e1 $" + String.format("%.2f", totalMoney));

        if (amountToSell >= 10000 || totalMoney >= 100000) {
            Bukkit.getServer().broadcastMessage("");
            Bukkit.getServer().broadcastMessage("\u00a7b\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
            Bukkit.getServer().broadcastMessage("\u00a7c\u00a7l\u26a0 C\u1ea2NH B\u00c1O C\u00c1 M\u1eacP X\u1ea2 H\u00c0NG \u26a0");
            Bukkit.getServer().broadcastMessage("\u00a7e\u0110\u1ea1i gia \u00a7a" + player.getName() + " \u00a7ev\u1eeba x\u1ea3 \u00a7f" + amountToSell + "x " + MarketModule.getVietnameseName(mat) + " \u00a7ev\u00e0o th\u1ecb tr\u01b0\u1eddng!");
            Bukkit.getServer().broadcastMessage("\u00a77\u27a4 Gi\u00e1 " + MarketModule.getVietnameseName(mat) + " \u0111ang r\u1edbt! Anh em c\u1ea9n th\u1eadn!");
            Bukkit.getServer().broadcastMessage("\u00a7b\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
            
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
        openMarketSell(player, info);
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
