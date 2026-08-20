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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OreStorageMenu implements InventoryHolder {

    public enum Category { MINERALS, CROPS }

    private final Inventory inventory;
    private final OreStorageModule module;
    private final Island island;
    private Category currentCategory = Category.MINERALS;

    private static final Set<Material> CROPS = new HashSet<>(Arrays.asList(
            matchMaterial("WHEAT"), matchMaterial("CARROT", "CARROTS"),
            matchMaterial("POTATO", "POTATOES"), matchMaterial("SUGAR_CANE", "SUGAR_CANE_BLOCK"),
            matchMaterial("NETHER_WART", "NETHER_WARTS"), matchMaterial("MELON", "MELON_BLOCK"),
            matchMaterial("PUMPKIN"), matchMaterial("CACTUS")
    ));

    public OreStorageMenu(OreStorageModule module, Island island) {
        this.module = module;
        this.island = island;
        this.inventory = Bukkit.createInventory(this, 54, "\u00a78\u26cf \u00a7lKho \u0110\u1ea3o \u00a78\u25b6 " + (currentCategory == Category.MINERALS ? "Kho\u00e1ng S\u1ea3n" : "N\u00f4ng S\u1ea3n"));
        refresh();
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void setCategory(Category category) {
        this.currentCategory = category;
        refresh();
    }
    
    public void refresh() {
        inventory.clear();

        ItemStack filler = new ItemStack(matchMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"));
        if (filler.getType().name().equals("STAINED_GLASS_PANE")) filler.setDurability((short) 7);
        ItemMeta fillMeta = filler.getItemMeta();
        if (fillMeta != null) { fillMeta.setDisplayName("\u00a7f"); filler.setItemMeta(fillMeta); }

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, filler);
        }

        ItemStack tabMinerals = new ItemStack(matchMaterial("DIAMOND_PICKAXE"));
        ItemMeta minMeta = tabMinerals.getItemMeta();
        if (minMeta != null) {
            minMeta.setDisplayName("\u00a7b\u00a7l\u26cf KHO\u00c1NG S\u1ea2N");
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (currentCategory == Category.MINERALS) {
                lore.add("\u00a7a\u25b6 \u0110ang \u0111\u01b0\u1ee3c ch\u1ecdn");
            } else {
                lore.add("\u00a77Click \u0111\u1ec3 chuy\u1ec3n sang tab n\u00e0y!");
            }
            minMeta.setLore(lore);
            tabMinerals.setItemMeta(minMeta);
        }
        inventory.setItem(2, tabMinerals);

        ItemStack tabCrops = new ItemStack(matchMaterial("GOLDEN_HOE", "GOLD_HOE"));
        ItemMeta cropMeta = tabCrops.getItemMeta();
        if (cropMeta != null) {
            cropMeta.setDisplayName("\u00a7e\u00a7l\ud83c\udf3e N\u00d4NG S\u1ea2N");
            List<String> lore = new ArrayList<>();
            lore.add("");
            if (currentCategory == Category.CROPS) {
                lore.add("\u00a7a\u25b6 \u0110ang \u0111\u01b0\u1ee3c ch\u1ecdn");
            } else {
                lore.add("\u00a77Click \u0111\u1ec3 chuy\u1ec3n sang tab n\u00e0y!");
            }
            cropMeta.setLore(lore);
            tabCrops.setItemMeta(cropMeta);
        }
        inventory.setItem(6, tabCrops);

        UUID islandId = island.getUniqueId();
        int slot = 9;
        
        for (Material mat : StorageListener.TRACKABLE_MATERIALS) {
            if (mat == null || mat == Material.AIR) continue;
            
            boolean isCrop = CROPS.contains(mat);
            if (currentCategory == Category.MINERALS && isCrop) continue;
            if (currentCategory == Category.CROPS && !isCrop) continue;

            BigInteger amount = module.getStorageManager().getAmount(islandId, mat);
            if (amount.compareTo(BigInteger.ZERO) > 0) {
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("\u00a76\u00a7l" + prettyName(mat.name()));
                    List<String> lore = new ArrayList<>();
                    lore.add("\u00a77\u0110ang c\u00f3: \u00a7a\u00a7l" + String.format("%,d", amount));
                    lore.add("");
                    lore.add("\u00a7e\u25b6 \u00a7fChu\u1ed9t Tr\u00e1i: \u00a77R\u00fat 1");
                    lore.add("\u00a7e\u25b6 \u00a7fChu\u1ed9t Ph\u1ea3i: \u00a77R\u00fat 64");
                    lore.add("\u00a7e\u25b6 \u00a7fShift + Tr\u00e1i: \u00a77R\u00fat \u0111\u1ea7y t\u00fai");
                    lore.add("\u00a7e\u25b6 \u00a7fPh\u00edm Q (Drop): \u00a77C\u1ea5t \u0111\u1ed3 t\u1eeb t\u00fai");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(slot++, item);
                if (slot >= 54) break; 
            }
        }
    }

    private String prettyName(String name) {
        String[] words = name.toLowerCase().replace("_", " ").split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleClick(Player player, int slot, ClickType clickType, ItemStack currentItem) {
        if (slot == 2) {
            setCategory(Category.MINERALS);
            return;
        } else if (slot == 6) {
            setCategory(Category.CROPS);
            return;
        }
        
        if (currentItem == null || currentItem.getType() == Material.AIR || slot < 9) return;
        Material mat = currentItem.getType();
        
        if (!StorageListener.TRACKABLE_MATERIALS.contains(mat)) return;
        
        UUID islandId = island.getUniqueId();
        BigInteger stored = module.getStorageManager().getAmount(islandId, mat);

        if (clickType == ClickType.DROP) {
            int count = 0;
            for (ItemStack invItem : player.getInventory().getContents()) {
                if (invItem != null && invItem.getType() == mat && (!invItem.hasItemMeta() || (!invItem.getItemMeta().hasDisplayName() && !invItem.getItemMeta().hasLore()))) {
                    count += invItem.getAmount();
                    invItem.setAmount(0);
                }
            }
            if (count > 0) {
                module.getStorageManager().addAmount(islandId, mat, BigInteger.valueOf(count));
                player.sendMessage("\u00a7a\u0110\u00e3 c\u1ea5t " + count + " " + prettyName(mat.name()) + " v\u00e0o kho.");
            }
        } else {
            BigInteger amountToTake = BigInteger.ZERO;
            if (clickType == ClickType.LEFT) {
                amountToTake = BigInteger.ONE;
            } else if (clickType == ClickType.RIGHT) {
                amountToTake = BigInteger.valueOf(64);
            } else if (clickType == ClickType.SHIFT_LEFT) {
                amountToTake = stored;
                int maxFit = 0;
                for (int i = 0; i < 36; i++) {
                    ItemStack invItem = player.getInventory().getItem(i);
                    if (invItem == null || invItem.getType() == Material.AIR) {
                        maxFit += mat.getMaxStackSize();
                    } else if (invItem.getType() == mat) {
                        maxFit += (mat.getMaxStackSize() - invItem.getAmount());
                    }
                }
                if (amountToTake.compareTo(BigInteger.valueOf(maxFit)) > 0) {
                    amountToTake = BigInteger.valueOf(maxFit);
                }
            }

            if (amountToTake.compareTo(BigInteger.ZERO) > 0 && stored.compareTo(amountToTake) >= 0) {
                module.getStorageManager().removeAmount(islandId, mat, amountToTake);
                ItemStack give = new ItemStack(mat);
                
                int totalTake = amountToTake.intValue();
                while (totalTake > 0) {
                    int stack = Math.min(totalTake, mat.getMaxStackSize());
                    give.setAmount(stack);
                    player.getInventory().addItem(give.clone());
                    totalTake -= stack;
                }
                player.sendMessage("\u00a7a\u0110\u00e3 r\u00fat " + amountToTake + " " + prettyName(mat.name()) + ".");
            } else if (stored.compareTo(BigInteger.ZERO) == 0) {
                player.sendMessage("\u00a7cKh\u00f4ng \u0111\u1ee7 v\u1eadt ph\u1ea9m trong kho!");
            }
        }

        refresh();
    }
    
    private static Material matchMaterial(String... names) {
        for (String name : names) {
            try {
                Material material = Material.matchMaterial(name);
                if (material != null)
                    return material;
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
