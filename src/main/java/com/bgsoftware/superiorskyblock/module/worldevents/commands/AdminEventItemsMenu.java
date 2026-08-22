package com.bgsoftware.superiorskyblock.module.worldevents.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

public class AdminEventItemsMenu implements Listener {

    private final WorldEventsModule module;
    private final SuperiorSkyblock plugin;
    private Inventory inventory;

    public AdminEventItemsMenu(WorldEventsModule module, SuperiorSkyblock plugin) {
        this.module = module;
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, (SuperiorSkyblockPlugin) plugin);
    }

    public void open(Player player) {
        inventory = Bukkit.createInventory(null, 54, "\u00a78\u00a7lKho V\u1eadt Ph\u1ea9m S\u1ef1 Ki\u1ec7n");
        populateItems();
        player.openInventory(inventory);
    }

    private void populateItems() {
        int slot = 0;
        
        // Volcano
        inventory.setItem(slot++, createItem(Material.MAGMA_CREAM, "\u00a7c\u00a7lTinh Th\u1ec3 Dung Nham", "\u00a7d\u00a7lS\u1eed Thi", "S\u1ef1 ki\u1ec7n N\u00fai L\u1eeda", "Tinh th\u1ec3 n\u00f3ng ch\u1ea3y r\u1edbt ra t\u1eeb l\u00f5i c\u1ee7a Golem L\u1eeda. C\u1ea7m tr\u00ean tay v\u1eabn c\u00f2n c\u1ea3m th\u1ea5y s\u1ee9c n\u00f3ng kinh ng\u01b0\u1eddi."));
        inventory.setItem(slot++, createItem(Material.BLAZE_ROD, "\u00a76\u00a7lL\u00f5i Nham Th\u1ea1ch", "\u00a7e\u00a7lHi\u1ebfm", "S\u1ef1 ki\u1ec7n N\u00fai L\u1eeda", "Thanh nhi\u1ec7t l\u01b0\u1ee3ng cung c\u1ea5p s\u1ee9c m\u1ea1nh cho Golem L\u1eeda."));
        inventory.setItem(slot++, createItem(Material.NETHER_STAR, "\u00a74\u00a7lB\u1ea3o Ng\u1ecdc \u0110\u1ecba Ng\u1ee5c", "\u00a76\u00a7lHuy\u1ec1n Tho\u1ea1i", "S\u1ef1 ki\u1ec7n N\u00fai L\u1eeda", "Vi\u00ean ng\u1ecdc ch\u1ee9a \u0111\u1ef1ng to\u00e0n b\u1ed9 c\u01a1n th\u1ecbnh n\u1ed9 c\u1ee7a n\u00fai l\u1eeda. V\u00f4 c\u00f9ng hi\u1ebfm c\u00f3."));
        
        // Invasion
        inventory.setItem(slot++, createItem(Material.IRON_INGOT, "\u00a7c\u00a7lC\u00fap B\u1ea3o V\u1ec7 \u0110\u1ea3o", "\u00a7e\u00a7lHi\u1ebfm", "S\u1ef1 ki\u1ec7n X\u00e2m L\u01b0\u1ee3c", "K\u1ef7 ni\u1ec7m ch\u01b0\u01a1ng vinh danh ng\u01b0\u1eddi anh h\u00f9ng \u0111\u00e3 d\u0169ng c\u1ea3m b\u1ea3o v\u1ec7 h\u00f2n \u0111\u1ea3o kh\u1ecfi b\u1ea7y y\u00eau qu\u00e1i."));
        inventory.setItem(slot++, createItem(Material.GOLD_NUGGET, "\u00a76\u00a7lXu Chi\u1ebfn L\u1ee3i Ph\u1ea9m", "\u00a7a\u00a7lTh\u01b0\u1eddng", "S\u1ef1 ki\u1ec7n X\u00e2m L\u01b0\u1ee3c", "\u0110\u1ed3ng xu c\u1ed5 \u0111\u01b0\u1ee3c qu\u00e2n x\u00e2m l\u01b0\u1ee3c mang theo."));
        inventory.setItem(slot++, createItem(Material.DIAMOND, "\u00a7b\u00a7lKim C\u01b0\u01a1ng Ch\u1ec9 Huy", "\u00a76\u00a7lHuy\u1ec1n Tho\u1ea1i", "S\u1ef1 ki\u1ec7n X\u00e2m L\u01b0\u1ee3c", "Vi\u00ean kim c\u01b0\u01a1ng c\u01b0\u1edbp \u0111\u01b0\u1ee3c t\u1eeb t\u00ean th\u1ee7 l\u0129nh qu\u00e2n x\u00e2m l\u01b0\u1ee3c."));
        
        // SpaceRift
        inventory.setItem(slot++, createItem(Material.ENDER_PEARL, "\u00a75\u00a7lTh\u00e1nh V\u1eadt H\u01b0 V\u00f4", "\u00a7d\u00a7lS\u1eed Thi", "V\u1ebft N\u1ee9t Kh\u00f4ng Gian", "V\u1eadt th\u1ec3 k\u1ef3 l\u1ea1 t\u1ecfa ra n\u0103ng l\u01b0\u1ee3ng t\u1ed1i t\u1eeb chi\u1ec1u kh\u00f4ng gian kh\u00e1c."));
        inventory.setItem(slot++, createItem(Material.EMERALD, "\u00a7d\u00a7lM\u1ea3nh C\u1ed5ng Kh\u00f4ng Gian", "\u00a7e\u00a7lHi\u1ebfm", "V\u1ebft N\u1ee9t Kh\u00f4ng Gian", "M\u1ed9t m\u1ea3nh v\u1ee1 r\u1edbt l\u1ea1i sau khi v\u1ebft n\u1ee9t kh\u00f4ng gian kh\u00e9p l\u1ea1i."));
        inventory.setItem(slot++, createItem(Material.OBSIDIAN, "\u00a75\u00a7lTinh Ch\u1ea5t H\u01b0 V\u00f4", "\u00a76\u00a7lHuy\u1ec1n Tho\u1ea1i", "V\u1ebft N\u1ee9t Kh\u00f4ng Gian", "C\u00f4 \u0111\u1eb7c c\u1ee7a b\u00f3ng t\u1ed1i h\u01b0 v\u00f4. C\u1ef1c k\u1ef3 gi\u00e1 tr\u1ecb."));
        
        // Meteor
        inventory.setItem(slot++, createItem(Material.DIAMOND_ORE, "\u00a76\u00a7lQu\u1eb7ng Thi\u00ean Th\u1ea1ch", "\u00a7e\u00a7lHi\u1ebfm", "M\u01b0a Thi\u00ean Th\u1ea1ch", "Kh\u1ed1i DIAMOND_ORE mang n\u0103ng l\u01b0\u1ee3ng v\u0169 tr\u1ee5 v\u1eeba \u0111\u1eadp xu\u1ed1ng \u0111\u1ea3o c\u1ee7a b\u1ea1n."));
        
        // Tornado
        inventory.setItem(slot++, createItem(Material.NETHER_STAR, "\u00a7b\u00a7lL\u00f5i B\u00e3o", "\u00a7d\u00a7lS\u1eed Thi", "V\u00f2i R\u1ed3ng", "L\u00f5i n\u0103ng l\u01b0\u1ee3ng ng\u01b0ng t\u1ee5 c\u1ee7a c\u01a1n b\u00e3o d\u1eef d\u1ed9i."));
        inventory.setItem(slot++, createItem(Material.GOLD_INGOT, "\u00a7e\u00a7lM\u1ea3nh S\u00e9t", "\u00a7e\u00a7lHi\u1ebfm", "V\u00f2i R\u1ed3ng", "M\u1ea3nh n\u0103ng l\u01b0\u1ee3ng s\u1ea5m s\u00e9t r\u1edbt ra t\u1eeb H\u1ed3n B\u00e3o."));
        
        // Celestial
        inventory.setItem(slot++, createItem(Material.GHAST_TEAR, "\u00a7d\u00a7lM\u1ea3nh Tinh T\u00fa", "\u00a7d\u00a7lS\u1eed Thi", "Th\u00fa Thi\u00ean Th\u1ec3", "M\u1ed9t kh\u1ed1i l\u1ea5p l\u00e1nh mang n\u0103ng l\u01b0\u1ee3ng t\u1eeb nh\u1eefng v\u00ec sao."));
        inventory.setItem(slot++, createItem(Material.GLOWSTONE_DUST, "\u00a7e\u00a7lB\u1ee5i Thi\u00ean Th\u1ec3", "\u00a7a\u00a7lTh\u01b0\u1eddng", "Th\u00fa Thi\u00ean Th\u1ec3", "T\u00e0n d\u01b0 b\u1ee5i s\u00e1ng l\u1ea5p l\u00e1nh s\u00f3t l\u1ea1i c\u1ee7a linh th\u00fa."));
        inventory.setItem(slot++, createItem(Material.NETHER_STAR, "\u00a7b\u00a7lL\u00f5i Thi\u00ean H\u00e0", "\u00a76\u00a7lHuy\u1ec1n Tho\u1ea1i", "Th\u00fa Thi\u00ean Th\u1ec3", "V\u1eadt ph\u1ea9m t\u1ed1i cao ch\u1ee9a \u0111\u1ef1ng n\u0103ng l\u01b0\u1ee3ng thi\u00ean h\u00e0."));
        inventory.setItem(slot++, createItem(Material.GLOWSTONE_DUST, "\u00a7e\u2726 B\u1ee5i Sao", "\u00a7a\u00a7lTh\u01b0\u1eddng", "Th\u00fa Thi\u00ean Th\u1ec3", "Nh\u1eefng h\u1ea1t b\u1ee5i ph\u00e1t s\u00e1ng r\u01a1i xu\u1ed1ng t\u1eeb tr\u1eadn chi\u1ebfn tinh t\u00fa."));
        
        // AncientTree
        inventory.setItem(slot++, createItem(Material.VINE, "\u00a7a\u00a7lTinh Ch\u1ea5t Thi\u00ean Nhi\u00ean", "\u00a7e\u00a7lHi\u1ebfm", "Th\u1ee5 Th\u1ea7n C\u1ed5 \u0110\u1ea1i", "Nh\u1ef1a c\u00e2y tinh khi\u1ebft h\u1ea5p th\u1ee5 s\u1ee9c s\u1ed1ng ng\u00e0n n\u0103m c\u1ee7a h\u00f2n \u0111\u1ea3o."));
        inventory.setItem(slot++, createItem(Material.SAPLING, "\u00a72\u00a7lH\u1ea1t Gi\u1ed1ng C\u1ed5 R\u1eebng", "\u00a7d\u00a7lS\u1eed Thi", "Th\u1ee5 Th\u1ea7n C\u1ed5 \u0110\u1ea1i", "M\u1ea7m s\u1ed1ng r\u1ef1c r\u1ee1 mang trong m\u00ecnh linh h\u1ed3n c\u1ee7a Dryad."));
        inventory.setItem(slot++, createItem(Material.EMERALD, "\u00a7a\u00a7lB\u1ee5i R\u1eebng Xanh", "\u00a7a\u00a7lTh\u01b0\u1eddng", "Th\u1ee5 Th\u1ea7n C\u1ed5 \u0110\u1ea1i", "M\u1ea3nh v\u1ee5n sinh th\u00e1i l\u1ea5p l\u00e1nh r\u1edbt ra t\u1eeb qu\u00e1i c\u00e2y."));
        inventory.setItem(slot++, createItem(Material.NETHER_STAR, "\u00a72\u00a7lPh\u01b0\u1edbc L\u00e0nh Dryad", "\u00a76\u00a7lHuy\u1ec1n Tho\u1ea1i", "Th\u1ee5 Th\u1ea7n C\u1ed5 \u0110\u1ea1i", "Linh kh\u00ed th\u1ea7n th\u00e1nh ban ph\u01b0\u1edbc cho b\u1ea5t k\u1ef3 sinh v\u1eadt n\u00e0o s\u1edf h\u1eefu n\u00f3."));
        
    }

    private ItemStack createItem(Material mat, String name, String rarity, String eventName, String description) {
        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("\u00a78\u00a7m                                ");
            lore.add("\u00a77Ngu\u1ed3n g\u1ed1c: \u00a76" + eventName);
            lore.add("\u00a77Ph\u1ea9m ch\u1ea5t: " + rarity);
            lore.add("");
            String[] words = description.split(" ");
            StringBuilder line = new StringBuilder("\u00a7f");
            for (String word : words) {
                if (line.length() + word.length() > 30) {
                    lore.add(line.toString());
                    line = new StringBuilder("\u00a7f");
                }
                line.append(word).append(" ");
            }
            if (line.length() > 2) lore.add(line.toString());
            lore.add("");
            lore.add("\u00a7a\u2714 \u00a77D\u00f9ng \u0111\u1ec3 Trao \u0111\u1ed5i");
            lore.add("\u00a77t\u1ea1i khu v\u1ef1c: \u00a7e/warp trade");
            lore.add("\u00a78\u00a7m                                ");
            meta.setLore(lore);
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            try { meta.addItemFlags(ItemFlag.HIDE_ENCHANTS); } catch (Exception ignored) {}
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);
        if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
            e.getWhoClicked().getInventory().addItem(e.getCurrentItem().clone());
            e.getWhoClicked().sendMessage("\u00a7a\u2714 \u0110\u00e3 l\u1ea5y " + e.getCurrentItem().getItemMeta().getDisplayName());
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
