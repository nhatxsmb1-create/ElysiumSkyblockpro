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
import java.util.Set;

public class TrophiesMenu implements InventoryHolder {

    private final Inventory inventory;
    private final TrophiesModule module;
    private final Island island;

    public TrophiesMenu(TrophiesModule module, Island island, Player player) {
        this.module = module;
        this.island = island;
        this.inventory = Bukkit.createInventory(this, 54, "§8🏆 Trophy Hall");
        refresh(player);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void refresh(Player player) {
        inventory.clear();

        Set<String> placed = module.getTrophyManager().getPlacedTrophyIds(island);
        int totalTypes = module.getTrophyManager().getTrophies().size();
        
        // Calculate starting slot to center trophies
        int rowStart = 20; // Middle row, starting at slot 20
        int slot = rowStart;
        if (totalTypes <= 7) {
            slot = 22 - (totalTypes / 2); // Center items in row 3
        }

        for (Map.Entry<String, TrophyInfo> entry : module.getTrophyManager().getTrophies().entrySet()) {
            TrophyInfo info = entry.getValue();
            boolean has = placed.contains(info.getId());
            int placedCount = module.getTrophyManager().getPlacedTrophyCount(island, info.getId());
            int ownedCount = countOwnedTrophies(player, info.getId());

            ItemStack item = module.getTrophyManager().createTrophyItem(info.getId());
            if (item == null)
                continue;

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add(has ? "§a✔ Đang trưng bày" : "§c✘ Chưa trưng bày");
                lore.add("§7Đánh bại Mini Boss của event §e" + info.getName());
                lore.add("§7để nhận trophy này.");
                lore.add("");
                lore.add("§6Buff nhận được khi đặt:");
                
                boolean hasBuff = false;
                if (!info.getPotions().isEmpty()) {
                    for (PotionEffect effect : info.getPotions()) {
                        lore.add("§7- §b" + effect.getType().getName() + " " + (effect.getAmplifier() + 1));
                        hasBuff = true;
                    }
                }
                if (info.getBonuses().containsKey("crop-growth")) {
                    lore.add("§7- §a+" + (int)(info.getBonuses().get("crop-growth") * 100) + "% Tốc độ trồng trọt");
                    hasBuff = true;
                }
                if (info.getBonuses().containsKey("mob-drops")) {
                    lore.add("§7- §c+" + (int)(info.getBonuses().get("mob-drops") * 100) + "% Rớt đồ quái vật");
                    hasBuff = true;
                }
                if (!hasBuff) {
                    lore.add("§7- Không có");
                }
                
                lore.add("");
                lore.add("§fThống kê:");
                lore.add("§7- Đang sở hữu (trong túi): §e" + ownedCount);
                lore.add("§7- Đang trưng bày trên đảo: §a" + placedCount);
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            if (!has) {
                // Grey out missing trophies
                Material grayPane = matchMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
                item.setType(grayPane);
                if (grayPane.name().equals("STAINED_GLASS_PANE"))
                    item.setDurability((short) 7);
                ItemMeta grayMeta = item.getItemMeta();
                if (grayMeta != null) {
                    grayMeta.setDisplayName("§7" + info.getName());
                    grayMeta.setLore(meta != null ? meta.getLore() : null);
                    item.setItemMeta(grayMeta);
                }
            }

            inventory.setItem(slot++, item);
            if (slot % 9 == 8) // move to next row if reaching edge
                slot += 2;
        }

        // Info item at the bottom center
        int totalPlaced = module.getTrophyManager().getPlacedTrophyCount(island);
        ItemStack info = new ItemStack(matchMaterial("NETHER_STAR"));
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§e§lTrophy Hall");
            List<String> lore = new ArrayList<>();
            lore.add("§7Bạn đã trưng bày tổng cộng §e" + totalPlaced + " §7trophies.");
            lore.add("");
            lore.add("§7Đặt trophy lên đảo để kích hoạt buff.");
            lore.add("§7Mỗi loại trophy có một buff riêng biệt.");
            lore.add("§7Tháo xuống sẽ mất buff của trophy đó.");
            infoMeta.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        inventory.setItem(49, info);

        // Decorate borders
        ItemStack filler = new ItemStack(matchMaterial("BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"));
        if (filler.getType().name().equals("STAINED_GLASS_PANE"))
            filler.setDurability((short) 15);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName("§f");
            filler.setItemMeta(fillerMeta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                // Fill borders
                if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                    inventory.setItem(i, filler);
                }
            }
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

    /**
     * Resolves a material across server versions - modern name first, legacy fallback second.
     */
    private static Material matchMaterial(String... names) {
        for (String name : names) {
            try {
                Material material = Material.matchMaterial(name);
                if (material != null)
                    return material;
            } catch (Exception ignored) {
            }
        }
        return Material.BOOK;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

}
