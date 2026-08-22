package com.bgsoftware.superiorskyblock.module.market;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DealMenu implements Listener {

    private final MarketModule module;
    private final SuperiorSkyblock plugin;
    private Inventory inventory;
    private Player viewer;

    public DealMenu(MarketModule module, SuperiorSkyblock plugin) {
        this.module = module;
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, (SuperiorSkyblockPlugin) plugin);
    }

    public void open(Player player) {
        this.viewer = player;
        inventory = Bukkit.createInventory(null, 45, "\u00a78\u00a7lTh\u01b0\u01a1ng V\u1ee5 B\u1ea1c T\u1ef7");
        refresh();
        player.openInventory(inventory);
    }

    private void refresh() {
        DealManager deal = module.getDealManager();
        Material mat = deal.getTargetMaterial();
        long current = deal.getCurrentAmount();
        long target = deal.getTargetAmount();
        boolean active = deal.isActive();
        
        ItemStack border = getBorder();
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i > 35 || i % 9 == 0 || i % 9 == 8) inventory.setItem(i, border);
        }
        
        ItemStack center = new ItemStack(mat);
        ItemMeta meta = center.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a76\u00a7lTh\u01b0\u01a1ng V\u1ee5: \u00a7a" + MarketModule.getVietnameseName(mat));
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77T\u1eadp \u0111o\u00e0n \u0111ang c\u1ea7n thu mua g\u1ea5p v\u1eadt ph\u1ea9m n\u00e0y!");
            lore.add("");
            lore.add("\u00a7eTi\u1ebfn \u0111\u1ed9: \u00a7f" + current + " \u00a78/ \u00a7f" + target);
            
            int bars = 20;
            int filled = target > 0 ? (int) ((current * bars) / target) : 0;
            if (filled > bars) filled = bars;
            StringBuilder pb = new StringBuilder("\u00a78[");
            for (int i = 0; i < bars; i++) {
                if (i < filled) pb.append("\u00a7a\u2588");
                else pb.append("\u00a77\u2591");
            }
            pb.append("\u00a78]");
            lore.add(pb.toString());
            lore.add("");
            
            if (active) {
                lore.add("\u00a7c\u27A4 Đang C\u1ea7n H\u00e0ng!");
            } else {
                lore.add("\u00a7a\u2714 Th\u01b0\u01a1ng V\u1ee5 \u0110\u00e3 Ho\u00e0n Th\u00e0nh!");
            }
            meta.setLore(lore);
            center.setItemMeta(meta);
        }
        inventory.setItem(13, center);
        
        if (active) {
            inventory.setItem(29, getContributeBtn(mat, false));
            inventory.setItem(33, getContributeBtn(mat, true));
        } else {
            inventory.setItem(29, getBorder());
            inventory.setItem(33, getBorder());
        }
        
        List<Map.Entry<UUID, Long>> top = deal.getTopContributors();
        inventory.setItem(22, getTopHead(top, 0, "\u00a7e\u00a7lTOP 1"));
        inventory.setItem(21, getTopHead(top, 1, "\u00a7f\u00a7lTOP 2"));
        inventory.setItem(23, getTopHead(top, 2, "\u00a76\u00a7lTOP 3"));
        
        long myCont = deal.getPlayerContribution(viewer.getUniqueId());
        ItemStack infoBtn = new ItemStack(Material.PAPER);
        ItemMeta iMeta = infoBtn.getItemMeta();
        if (iMeta != null) {
            iMeta.setDisplayName("\u00a7b\u00a7lTh\u00f4ng Tin C\u1ee7a B\u1ea1n");
            List<String> l = new ArrayList<>();
            l.add("\u00a77B\u1ea1n \u0111\u00e3 \u0111\u00f3ng g\u00f3p: \u00a7f" + myCont + " \u00a77v\u1eadt ph\u1ea9m");
            iMeta.setLore(l);
            infoBtn.setItemMeta(iMeta);
        }
        inventory.setItem(40, infoBtn);
    }

    private ItemStack getBorder() {
        ItemStack item;
        try { item = new ItemStack(Material.valueOf("YELLOW_STAINED_GLASS_PANE")); }
        catch (Exception e) { item = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 4); }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); item.setItemMeta(meta); }
        return item;
    }

    private ItemStack getContributeBtn(Material mat, boolean fromKho) {
        Material icon = fromKho ? Material.ENDER_CHEST : Material.CHEST;
        String fromStr = fromKho ? "Kho \u0110\u1ea3o" : "T\u00fai \u0110\u1ed3";
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("\u00a7a\u00a7lN\u1ed9p T\u1ea5t C\u1ea3 t\u1eeb " + fromStr);
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Click \u0111\u1ec3 n\u1ed9p to\u00e0n b\u1ed9 " + MarketModule.getVietnameseName(mat));
            lore.add("\u00a77t\u1eeb " + fromStr + " v\u00e0o Th\u01b0\u01a1ng V\u1ee5.");
            lore.add("");
            int chunkSize = module.getConfiguration().getConfig().getInt("deal-settings.chunk-size", 100);
            lore.add("\u00a7e\u272a C\u1ee9 n\u1ed9p " + chunkSize + " c\u00e1i l\u00e0 \u0111\u01b0\u1ee3c Th\u01b0\u1edfng!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack getTopHead(List<Map.Entry<UUID, Long>> top, int index, String title) {
        ItemStack head;
        try { head = new ItemStack(Material.valueOf("PLAYER_HEAD")); }
        catch (Exception e) { head = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3); }
        ItemMeta meta = head.getItemMeta();
        if (meta != null) {
            if (index < top.size()) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(top.get(index).getKey());
                meta.setDisplayName(title + "\u00a78 - \u00a7a" + p.getName());
                ((SkullMeta) meta).setOwner(p.getName());
                List<String> lore = new ArrayList<>();
                lore.add("\u00a77\u0110\u00e3 n\u1ed9p: \u00a7f" + top.get(index).getValue());
                meta.setLore(lore);
            } else {
                meta.setDisplayName(title + "\u00a78 - \u00a77Tr\u1ed1ng");
            }
            head.setItemMeta(meta);
        }
        return head;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null) return;
        
        Player player = (Player) e.getWhoClicked();
        int slot = e.getSlot();
        DealManager deal = module.getDealManager();
        if (!deal.isActive()) return;
        
        Material mat = deal.getTargetMaterial();
        
        if (slot == 29) {
            long amountToSell = 0;
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null && contents[i].getType() == mat) {
                    amountToSell += contents[i].getAmount();
                    player.getInventory().setItem(i, null);
                }
            }
            if (amountToSell > 0) {
                deal.addContribution(player, amountToSell);
                player.sendMessage("\u00a7a\u2714 B\u1ea1n v\u1eeba n\u1ed9p " + amountToSell + "x " + MarketModule.getVietnameseName(mat));
                try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP"), 1f, 1f); } 
                catch (Exception ex) { try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ORB_PICKUP"), 1f, 1f); } catch (Exception ignored) {} }
                refresh();
            } else {
                player.sendMessage("\u00a7cB\u1ea1n kh\u00f4ng c\u00f3 " + MarketModule.getVietnameseName(mat) + " trong T\u00fai \u0111\u1ed3!");
            }
        }
        else if (slot == 33) {
            SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
            if (sp.getIsland() == null) { player.sendMessage("\u00a7cB\u1ea1n ch\u01b0a c\u00f3 \u0111\u1ea3o!"); return; }
            BigInteger amountInKho = BuiltinModules.ORE_STORAGE.getStorageManager().getAmount(sp.getIsland().getUniqueId(), mat);
            if (amountInKho.compareTo(BigInteger.ZERO) > 0) {
                long amountToSell = amountInKho.longValue();
                if (amountToSell > 100000L) amountToSell = 100000L;
                BuiltinModules.ORE_STORAGE.getStorageManager().removeAmount(sp.getIsland().getUniqueId(), mat, BigInteger.valueOf(amountToSell));
                deal.addContribution(player, amountToSell);
                player.sendMessage("\u00a7a\u2714 B\u1ea1n v\u1eeba n\u1ed9p " + amountToSell + "x " + MarketModule.getVietnameseName(mat) + " t\u1eeb Kho \u0110\u1ea3o!");
                try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP"), 1f, 1f); } 
                catch (Exception ex) { try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ORB_PICKUP"), 1f, 1f); } catch (Exception ignored) {} }
                refresh();
            } else {
                player.sendMessage("\u00a7cKho \u0110\u1ea3o c\u1ee7a b\u1ea1n kh\u00f4ng c\u00f2n " + MarketModule.getVietnameseName(mat) + "!");
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(inventory)) {
            Bukkit.getScheduler().runTaskLater((SuperiorSkyblockPlugin) plugin, () -> {
                if (!e.getPlayer().getOpenInventory().getTopInventory().equals(inventory)) {
                    HandlerList.unregisterAll(this);
                }
            }, 2L);
        }
    }
}
