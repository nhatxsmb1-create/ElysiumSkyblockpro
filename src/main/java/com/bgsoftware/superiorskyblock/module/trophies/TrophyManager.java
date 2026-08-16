package com.bgsoftware.superiorskyblock.module.trophies;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule.TrophyInfo;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class TrophyManager {

    private static final String PDC_KEY = "trophies-placed";
    private static final String ENTRY_SEPARATOR = "|";
    private static final String FIELD_SEPARATOR = ",";
    private static final String ITEM_PREFIX = "§6§l🏆 Trophy: ";

    private final TrophiesModule module;
    private final Random random = new Random();

    // display name -> trophyId (identification is done by exact item name,
    // following the same pattern used by StackedBlocksListener)
    private final Map<String, String> reverseTrophies = new HashMap<>();

    public TrophyManager(TrophiesModule module) {
        this.module = module;
    }

    public void load() {
        reverseTrophies.clear();
        for (TrophyInfo info : module.getConfiguration().getTrophies().values())
            reverseTrophies.put(ITEM_PREFIX + info.getName(), info.getId());
    }

    public Map<String, TrophyInfo> getTrophies() {
        return module.getConfiguration().getTrophies();
    }

    public ItemStack createTrophyItem(String id) {
        TrophyInfo info = module.getConfiguration().getTrophies().get(id.toLowerCase());
        if (info == null)
            return null;

        ItemStack item;
        try {
            item = new ItemStack(Material.valueOf(info.getMaterial()));
        } catch (IllegalArgumentException error) {
            item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        }

        if (item.getType().name().contains("SKULL") || item.getType().name().contains("HEAD")) {
            try {
                item.setDurability((short) 3);
            } catch (Exception ignored) {
            }
            if (!info.getTexture().isEmpty())
                item = ItemSkulls.getPlayerHead(item, info.getTexture());
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ITEM_PREFIX + info.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7Phần thưởng từ Island Event.");
            lore.add("§7Đặt lên đảo để trưng bày trong");
            lore.add("§7Trophy Hall và nhận buff!");
            lore.add("");
            lore.add("§eBộ sưu tập càng đủ, buff càng mạnh.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Identifies a trophy item by its exact display name.
     */
    public String getTrophyId(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName())
            return null;
        return reverseTrophies.get(meta.getDisplayName());
    }

    public boolean shouldDropTrophy() {
        return random.nextDouble() * 100.0 < module.getConfiguration().getDropChance();
    }

    // ── Placed trophies storage (island PersistentDataContainer) ──

    public List<String> getPlacedTrophies(Island island) {
        String data = island.getPersistentDataContainer().get(PDC_KEY, PersistentDataType.STRING);
        List<String> entries = new ArrayList<>();
        if (data != null && !data.isEmpty()) {
            for (String entry : data.split("\\" + ENTRY_SEPARATOR)) {
                if (!entry.isEmpty())
                    entries.add(entry);
            }
        }
        return entries;
    }

    private void setPlacedTrophies(Island island, List<String> entries) {
        island.getPersistentDataContainer().put(PDC_KEY, PersistentDataType.STRING,
                String.join(ENTRY_SEPARATOR, entries));
    }

    public void addPlacedTrophy(Island island, String trophyId, Location location) {
        List<String> entries = getPlacedTrophies(island);
        entries.add(trophyId + FIELD_SEPARATOR + locationKey(location));
        setPlacedTrophies(island, entries);
    }

    /**
     * Removes the placed trophy entry matching the location and returns
     * the trophy id that was removed, or null if nothing matched.
     */
    public String removePlacedTrophy(Island island, Location location) {
        List<String> entries = getPlacedTrophies(island);
        String locationKey = locationKey(location);

        for (int i = 0; i < entries.size(); i++) {
            String entry = entries.get(i);
            int splitIndex = entry.indexOf(FIELD_SEPARATOR);
            if (splitIndex < 0)
                continue;
            if (entry.substring(splitIndex + 1).equals(locationKey)) {
                String trophyId = entry.substring(0, splitIndex);
                entries.remove(i);
                setPlacedTrophies(island, entries);
                return trophyId;
            }
        }
        return null;
    }

    public boolean isPlacedTrophyAt(Island island, Location location) {
        String locationKey = locationKey(location);
        for (String entry : getPlacedTrophies(island)) {
            int splitIndex = entry.indexOf(FIELD_SEPARATOR);
            if (splitIndex < 0)
                continue;
            if (entry.substring(splitIndex + 1).equals(locationKey))
                return true;
        }
        return false;
    }

    private static String locationKey(Location location) {
        return location.getWorld().getName() + FIELD_SEPARATOR + location.getBlockX() +
                FIELD_SEPARATOR + location.getBlockY() + FIELD_SEPARATOR + location.getBlockZ();
    }

    /**
     * Number of distinct trophy types currently placed on the island.
     */
    public int getPlacedTrophyCount(Island island) {
        Set<String> distinct = new HashSet<>();
        for (String entry : getPlacedTrophies(island)) {
            int splitIndex = entry.indexOf(FIELD_SEPARATOR);
            if (splitIndex > 0)
                distinct.add(entry.substring(0, splitIndex));
        }
        return distinct.size();
    }

    /**
     * Distinct trophy ids currently placed on the island.
     */
    public Set<String> getPlacedTrophyIds(Island island) {
        Set<String> distinct = new HashSet<>();
        for (String entry : getPlacedTrophies(island)) {
            int splitIndex = entry.indexOf(FIELD_SEPARATOR);
            if (splitIndex > 0)
                distinct.add(entry.substring(0, splitIndex));
        }
        return distinct;
    }

    /**
     * Potion effects granted by the current collection tier.
     */
    public List<PotionEffect> getEffectsForIsland(Island island) {
        int count = getPlacedTrophyCount(island);
        List<PotionEffect> result = new ArrayList<>();
        for (Map.Entry<Integer, List<PotionEffect>> entry : module.getConfiguration().getEffectTiers().entrySet()) {
            if (count >= entry.getKey())
                result.addAll(entry.getValue());
        }
        return result;
    }

}
