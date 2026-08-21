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
        
        // Setup border with glass panes
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            border.setItemMeta(borderMeta);
        }
        
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }
        
        // Setup Info book at slot 4
        ItemStack infoBook = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoBook.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§b§l✨ HƯỚNG DẪN SÀN CHỨNG KHOÁN ✨");
            List<String> infoLore = new ArrayList<>();
            infoLore.add("§7Đây là Sàn Giao Dịch Tài Nguyên mở của Server.");
            infoLore.add("§7Giá của vật phẩm §akhông cố định§7, mà sẽ");
            infoLore.add("§athay đổi liên tục §7dựa vào hành động của người chơi!");
            infoLore.add("");
            infoLore.add("§c⬇ Nếu nhiều người đổ xô bán§7, giá sẽ rớt thê thảm.");
            infoLore.add("§a⬆ Nếu không ai bán§7, giá sẽ dần hồi phục lên đỉnh!");
            infoLore.add("");
            infoLore.add("§e➤ §eHãy trở thành con sói già phố Wall Bán đỉnh Mua đáy!");
            infoMeta.setLore(infoLore);
            infoBook.setItemMeta(infoMeta);
        }
        inventory.setItem(4, infoBook);
        
        // Setup Close button at slot 49
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeBtn.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§l✖ Đóng Giao Diện");
            closeBtn.setItemMeta(closeMeta);
        }
        inventory.setItem(49, closeBtn);

        int[] innerSlots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
        int index = 0;
        
        for (Map.Entry<String, MarketModule.MarketItemInfo> entry : module.getConfiguration().getItems().entrySet()) {
            if (index >= innerSlots.length) break;
            MarketModule.MarketItemInfo info = entry.getValue();
            Material mat = info.getMaterial();
            if (mat == null) continue;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§6§l" + MarketModule.getVietnameseName(mat));
                List<String> lore = new ArrayList<>();
                double currentPrice = info.getCurrentPrice();
                String status = currentPrice >= info.getBasePrice() ? "§a⬆ Đang có giá (Đỉnh)" : "§c⬇ Lạm phát (Rớt giá)";
                
                lore.add("§8▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄");
                lore.add("");
                lore.add("§7Trạng thái: " + status);
                lore.add("§eGiá thu mua hiện tại: §a$" + String.format("%.2f", currentPrice) + " §7/ cái");
                lore.add("");
                lore.add("§7▶ Giá thị trường gốc: §f$" + info.getBasePrice());
                lore.add("§7▶ Giá trần (Cao nhất): §a$" + info.getMaxPrice());
                lore.add("§7▶ Giá sàn (Thấp nhất): §c$" + info.getMinPrice());
                lore.add("");
                lore.add("§bSố lượng server đã bán hôm nay: §f" + info.getPoolSize());
                lore.add("");
                lore.add("§8▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄");
                lore.add("");
                lore.add("§a[▶] Click Chuột Trái để bán tất cả trong §6Túi");
                lore.add("§a[▶] Click Chuột Phải để bán tất cả từ §e/is kho");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(innerSlots[index++], item);
        }
    }
@EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;
        Material clickedMat = e.getCurrentItem().getType();
        
        if (clickedMat == Material.BLACK_STAINED_GLASS_PANE || clickedMat == Material.BOOK) {
            return;
        }
        if (clickedMat == Material.BARRIER) {
            e.getWhoClicked().closeInventory();
            return;
        }
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
