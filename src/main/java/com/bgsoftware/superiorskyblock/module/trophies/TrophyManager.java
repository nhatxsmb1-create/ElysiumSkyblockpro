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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map;
import java.util.WeakHashMap;

public class TrophyManager {

    private static final String PDC_KEY = "trophies-placed";
    private static final String ENTRY_SEPARATOR = "|";
    private static final String FIELD_SEPARATOR = ",";
    private static final String ITEM_PREFIX = "\u00a76\u00a7l\ud83c\udfc6 Trophy: ";
    private static final String HIDDEN_PREFIX = "\u00a7f\u00a7f\u00a7f"; // Hidden prefix to identify trophies

    private final TrophiesModule module;
    private final Random random = new Random();

    // Cache for island potion effects to avoid heavy PDC reads every 8 seconds
    private final Map<Island, List<PotionEffect>> effectsCache = new WeakHashMap<>();
    
    // Cache for parsed PDC entries to optimize explosion events and loops
    private final Map<Island, List<String>> placedTrophiesCache = new WeakHashMap<>();

    public TrophyManager(TrophiesModule module) {
        this.module = module;
    }

    public void load() {
        effectsCache.clear();
        placedTrophiesCache.clear();
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
            // Modern name first, legacy fallback for old servers
            Material skull = Material.matchMaterial("PLAYER_HEAD");
            if (skull == null)
                skull = Material.matchMaterial("SKULL_ITEM");
            if (skull == null)
                skull = Material.BOOK;
            item = new ItemStack(skull);
            if (skull.name().equals("SKULL_ITEM"))
                item.setDurability((short) 3);
        }

        if (item.getType().name().contains("SKULL") || item.getType().name().contains("HEAD")) {
            if (item.getType().name().equals("SKULL_ITEM"))
                item.setDurability((short) 3);
            if (!info.getTexture().isEmpty())
                item = ItemSkulls.getPlayerHead(item, info.getTexture());
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ITEM_PREFIX + info.getName());
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Phần thưởng từ Island Event.");
            lore.add("\u00a77Đặt lên đảo để trưng bày trong");
            lore.add("\u00a77Trophy Hall và nhận buff!");
            lore.add("");
            lore.add("\u00a7eMỗi Trophy mang lại một buff riêng biệt.");
            
            // Add hidden ID to prevent anvil renaming exploits
            lore.add(encodeHiddenString(info.getId()));
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Identifies a trophy item by its exact hidden ID.
     */
    public String getTrophyId(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore())
            return null;
            
        for (String line : meta.getLore()) {
            if (line.startsWith(HIDDEN_PREFIX)) {
                return decodeHiddenString(line);
            }
        }
        return null;
    }

    public boolean shouldDropTrophy() {
        return random.nextDouble() * 100.0 < module.getConfiguration().getDropChance();
    }

    // ── Placed trophies storage (island PersistentDataContainer) ──

    public List<String> getPlacedTrophies(Island island) {
        return placedTrophiesCache.computeIfAbsent(island, this::loadPlacedTrophies);
    }

    private List<String> loadPlacedTrophies(Island island) {
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
        // Clear caches so they update on next tick
        effectsCache.remove(island);
        placedTrophiesCache.put(island, new ArrayList<>(entries));
    }

    public void addPlacedTrophy(Island island, String trophyId, Location location) {
        List<String> entries = new ArrayList<>(getPlacedTrophies(island));
        entries.add(trophyId + FIELD_SEPARATOR + locationKey(location));
        setPlacedTrophies(island, entries);
    }

    /**
     * Removes the placed trophy entry matching the location and returns
     * the trophy id that was removed, or null if nothing matched.
     */
    public String removePlacedTrophy(Island island, Location location) {
        List<String> entries = new ArrayList<>(getPlacedTrophies(island));
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

    public int getPlacedTrophyCount(Island island, String trophyId) {
        int count = 0;
        for (String entry : getPlacedTrophies(island)) {
            int splitIndex = entry.indexOf(FIELD_SEPARATOR);
            if (splitIndex > 0 && entry.substring(0, splitIndex).equals(trophyId))
                count++;
        }
        return count;
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
     * Potion effects granted by the placed trophies.
     * Heavily optimized with caching to prevent lag from 8-second loops.
     */
    public List<PotionEffect> getEffectsForIsland(Island island) {
        if (!effectsCache.containsKey(island)) {
            List<PotionEffect> result = new ArrayList<>();
            for (String trophyId : getPlacedTrophyIds(island)) {
                TrophyInfo info = module.getConfiguration().getTrophies().get(trophyId);
                if (info != null)
                    result.addAll(info.getPotions());
            }
            effectsCache.put(island, result);
        }
        return effectsCache.get(island);
    }

    private String encodeHiddenString(String str) {
        StringBuilder sb = new StringBuilder(HIDDEN_PREFIX);
        for (char c : str.toCharArray()) {
            String hex = Integer.toHexString(c);
            for (char h : hex.toCharArray()) {
                sb.append('\u00a7').append(h);
            }
            sb.append('\u00a7').append('k'); // separator
        }
        return sb.toString();
    }

    private String decodeHiddenString(String hidden) {
        if (!hidden.startsWith(HIDDEN_PREFIX)) return null;
        String stripped = hidden.replace("\u00a7", "");
        if (!stripped.startsWith("fff")) return null;
        stripped = stripped.substring(3);
        String[] parts = stripped.split("k");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            try {
                part = part.replaceAll("[^0-9a-fA-F]", ""); // Clean up any trailing Spigot formatting chars
                if (part.isEmpty()) continue;
                sb.append((char) Integer.parseInt(part, 16));
            } catch (Exception ignored) {}
        }
        return sb.toString();
    }

}
