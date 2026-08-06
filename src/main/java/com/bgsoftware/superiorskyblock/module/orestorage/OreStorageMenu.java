package com.bgsoftware.superiorskyblock.module.orestorage;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OreStorageMenu implements InventoryHolder {

    private final Inventory inventory;
    private final OreStorageModule module;
    private final Island island;

    public OreStorageMenu(OreStorageModule module, Island island) {
        this.module = module;
        this.island = island;
        this.inventory = Bukkit.createInventory(this, 54, "§8Kho Chứa Quặng");
        refresh();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void refresh() {
        inventory.clear();
        UUID islandId = island.getUniqueId();
        int slot = 0;
        for (Material mat : StorageListener.TRACKABLE_MATERIALS) {
            BigInteger amount = module.getStorageManager().getAmount(islandId, mat);
            if (amount.compareTo(BigInteger.ZERO) > 0) {
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§e§l" + mat.name());
                    List<String> lore = new ArrayList<>();
                    lore.add("§7Số lượng: §a" + amount.toString());
                    lore.add("");
                    lore.add("§f▪ Click Trái: §7Rút 1");
                    lore.add("§f▪ Click Phải: §7Rút 64");
                    lore.add("§f▪ Shift + Click Trái: §7Rút tất cả");
                    lore.add("§f▪ Phím Q (Drop): §7Cất đồ từ túi vào kho");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(slot++, item);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleClick(Player player, int slot, ClickType clickType, ItemStack currentItem) {
        if (currentItem == null || currentItem.getType() == Material.AIR) return;
        Material mat = currentItem.getType();
        UUID islandId = island.getUniqueId();
        BigInteger stored = module.getStorageManager().getAmount(islandId, mat);

        if (clickType == ClickType.DROP) {
            // Deposit from inventory
            int count = 0;
            for (ItemStack invItem : player.getInventory().getContents()) {
                if (invItem != null && invItem.getType() == mat) {
                    count += invItem.getAmount();
                    invItem.setAmount(0);
                }
            }
            if (count > 0) {
                module.getStorageManager().addAmount(islandId, mat, BigInteger.valueOf(count));
                player.sendMessage("§aĐã cất " + count + " " + mat.name() + " vào kho.");
                refresh();
            }
            return;
        }

        if (stored.compareTo(BigInteger.ZERO) <= 0) return;

        int toWithdraw = 0;
        if (clickType == ClickType.LEFT) {
            toWithdraw = 1;
        } else if (clickType == ClickType.RIGHT) {
            toWithdraw = 64;
        } else if (clickType == ClickType.SHIFT_LEFT) {
            toWithdraw = countFreeSpace(player, mat);
        }

        if (toWithdraw <= 0) {
            player.sendMessage("§cTúi đồ của bạn đã đầy!");
            return;
        }

        // Clamp to stored
        if (BigInteger.valueOf(toWithdraw).compareTo(stored) > 0) {
            toWithdraw = stored.intValue();
        }

        if (toWithdraw > 0) {
            module.getStorageManager().removeAmount(islandId, mat, BigInteger.valueOf(toWithdraw));
            giveItem(player, mat, toWithdraw);
            refresh();
        }
    }

    private int countFreeSpace(Player player, Material mat) {
        int space = 0;
        int maxStack = mat.getMaxStackSize();
        ItemStack[] contents = player.getInventory().getContents();
        // Slots 0-35 are the main storage (hotbar 0-8, main 9-35)
        for (int i = 0; i < 36; i++) {
            ItemStack item = (i < contents.length) ? contents[i] : null;
            if (item == null || item.getType() == Material.AIR) {
                space += maxStack;
            } else if (item.getType() == mat && item.getAmount() < maxStack) {
                space += (maxStack - item.getAmount());
            }
        }
        return space;
    }

    private void giveItem(Player player, Material mat, int amount) {
        int maxStack = mat.getMaxStackSize();
        while (amount > 0) {
            int give = Math.min(amount, maxStack);
            player.getInventory().addItem(new ItemStack(mat, give));
            amount -= give;
        }
    }
}
