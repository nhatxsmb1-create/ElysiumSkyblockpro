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
    private static final String AUTHENTIC_LORE = "Má»—i Trophy mang láº¡i má»™t buff riĂªng biá»‡t";

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
            lore.add("\u00a77Pháº§n thÆ°á»Ÿng tá»« Island Event.");
            lore.add("\u00a77Äáº·t lĂªn Ä‘áº£o Ä‘á»ƒ trÆ°ng bĂ y trong");
            lore.add("\u00a77Trophy Hall vĂ  nháº­n buff!");
            lore.add("");
            lore.add("\u00a7e" + AUTHENTIC_LORE + ".");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Identifies a trophy item securely without relying on formatting codes
     * that can be stripped by Paper's Component serializer.
     */
    public String getTrophyId(ItemStack item) {
        if (item == null || !item.hasItemMeta())
            return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.hasLore())
            return null;
            
        // 1. Verify it has the authentic lore to prevent anvil spoofing.
        // Players cannot modify lore in vanilla anvils.
        boolean isAuthentic = false;
        for (String line : meta.getLore()) {
            if (line.contains(AUTHENTIC_LORE)) {
                isAuthentic = true;
                break;
            }
        }
        
        if (!isAuthentic)
            return null;
            
        // 2. Match the display name to find the Trophy ID
        String displayName = meta.getDisplayName();
        for (Map.Entry<String, TrophyInfo> entry : getTrophies().entrySet()) {
            String expectedName = ITEM_PREFIX + entry.getValue().getName();
            if (displayName.equals(expectedName)) {
                return entry.getKey();
            }
        }
        
        return null;
    }

    public boolean shouldDropTrophy() {
        return random.nextDouble() * 100.0 < module.getConfiguration().getDropChance();
    }

    // â”€â”€ Placed trophies storage (island PersistentDataContainer) â”€â”€

    public java.util.Map<Location, String> getPlacedTrophyLocations(Island island) { java.util.Map<Location, String> locs = new java.util.HashMap<>(); for (String entry : getPlacedTrophies(island)) { String[] parts = entry.split(FIELD_SEPARATOR); if (parts.length >= 4) { org.bukkit.World world = org.bukkit.Bukkit.getWorld(parts[1]); if (world != null) { try { int x = Integer.parseInt(parts[2]); int y = Integer.parseInt(parts[3]); int z = Integer.parseInt(parts[4]); locs.put(new Location(world, x, y, z), parts[0]); } catch (Exception ignored) {} } } } return locs; }

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

}

