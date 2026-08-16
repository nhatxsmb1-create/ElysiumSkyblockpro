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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TrophiesMenu implements InventoryHolder {

    private final Inventory inventory;
    private final TrophiesModule module;
    private final Island island;

    public TrophiesMenu(TrophiesModule module, Island island) {
        this.module = module;
        this.island = island;
        this.inventory = Bukkit.createInventory(this, 27, "§8🏆 Trophy Hall");
        refresh();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void refresh() {
        inventory.clear();

        Set<String> placed = module.getTrophyManager().getPlacedTrophyIds(island);

        int slot = 10;
        for (Map.Entry<String, TrophyInfo> entry : module.getTrophyManager().getTrophies().entrySet()) {
            TrophyInfo info = entry.getValue();
            boolean has = placed.contains(info.getId());

            ItemStack item = module.getTrophyManager().createTrophyItem(info.getId());
            if (item == null)
                continue;

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add(has ? "§a✔ Đang trưng bày" : "§c✘ Chưa có");
                lore.add("§7Đánh bại Mini Boss của event §e" + info.getName());
                lore.add("§7để nhận trophy này.");
                lore.add("");
                lore.add("§6Buff hiện tại của đảo:");
                lore.add("§7- " + describeEffects());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            if (!has) {
                // Grey out missing trophies (legacy 1.8 material: stained glass pane, data 7)
                item.setType(Material.STAINED_GLASS_PANE);
                item.setDurability((short) 7);
                ItemMeta grayMeta = item.getItemMeta();
                if (grayMeta != null) {
                    grayMeta.setDisplayName("§7" + info.getName());
                    grayMeta.setLore(meta != null ? meta.getLore() : null);
                    item.setItemMeta(grayMeta);
                }
            }

            inventory.setItem(slot++, item);
            if (slot == 17)
                slot = 19;
        }

        // Info item
        int placedCount = module.getTrophyManager().getPlacedTrophyCount(island);
        int total = module.getTrophyManager().getTrophies().size();
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§e§lBộ Sưu Tập: §f" + placedCount + "/" + total);
            List<String> lore = new ArrayList<>();
            lore.add("§7Đặt trophy lên đảo để kích hoạt buff.");
            lore.add("§7Tháo xuống sẽ mất buff ngay.");
            infoMeta.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        inventory.setItem(4, info);

        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName("§f");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null)
                inventory.setItem(i, filler);
        }
    }

    private String describeEffects() {
        int count = module.getTrophyManager().getPlacedTrophyCount(island);
        StringBuilder builder = new StringBuilder();
        module.getConfiguration().getEffectTiers().forEach((threshold, effects) -> {
            if (builder.length() > 0)
                builder.append("§7, ");
            builder.append(count >= threshold ? "§a" : "§8").append(threshold).append(" loại: ");
            List<String> names = new ArrayList<>();
            effects.forEach(effect -> names.add(effect.getType().getName() + " " + (effect.getAmplifier() + 1)));
            builder.append(String.join(", ", names));
        });
        return builder.length() == 0 ? "§7Không có" : builder.toString();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

}
