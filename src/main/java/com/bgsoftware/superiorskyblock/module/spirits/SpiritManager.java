package com.bgsoftware.superiorskyblock.module.spirits;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule.SpiritConfigInfo;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class SpiritManager {

    private static final String PDC_KEY = "spirits-placed";
    private static final String ENTRY_SEPARATOR = "|";
    private static final String FIELD_SEPARATOR = ",";
    private static final String ITEM_PREFIX = "\u00a7f\u2728 Tinh Linh: ";
    private static final String AUTHENTIC_LORE = "Linh h\u1ed3n b\u1ea3o h\u1ed9 c\u1ee7a \u0111\u1ea3o";

    private final SpiritsModule module;
    
    private final Map<Island, List<String>> placedSpiritsCache = new WeakHashMap<>();

    public SpiritManager(com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin plugin, SpiritsModule module) {
        this.module = module;
    }

    public void load() {
        placedSpiritsCache.clear();
    }

    public void save() {
    }

    public ItemStack createSpiritItem(String type) {
        SpiritConfigInfo info = module.getConfiguration().getSpirits().get(type.toLowerCase());
        if (info == null) return null;

        ItemStack item;
        try {
            item = new ItemStack(Material.valueOf("PLAYER_HEAD"));
        } catch (Exception e) {
            item = new ItemStack(Material.matchMaterial("SKULL_ITEM"), 1, (short) 3);
        }

        if (!info.getTexture().isEmpty()) {
            item = ItemSkulls.getPlayerHead(item, info.getTexture());
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ITEM_PREFIX + org.bukkit.ChatColor.translateAlternateColorCodes('&', info.getName()));
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77Tr\u1ee3 th\u1ee7 \u0111\u1eafc l\u1ef1c gi\u00fap \u0111\u1ea3o ph\u00e1t tri\u1ec3n.");
            lore.add("\u00a77\u0110\u1eb7t xu\u1ed1ng \u0111\u1ea3o \u0111\u1ec3 kích ho\u1ea1t.");
            if (info.getDescription() != null && !info.getDescription().isEmpty()) {
                lore.add("");
                for (String descLine : info.getDescription()) {
                    lore.add(org.bukkit.ChatColor.translateAlternateColorCodes('&', descLine));
                }
            }
            lore.add("");
            lore.add("\u00a7e" + AUTHENTIC_LORE + ".");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    public String getSpiritType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.hasLore()) return null;
            
        boolean isAuthentic = false;
        for (String line : meta.getLore()) {
            if (line.contains(AUTHENTIC_LORE)) {
                isAuthentic = true;
                break;
            }
        }
        if (!isAuthentic) return null;
            
        String displayName = meta.getDisplayName();
        for (Map.Entry<String, SpiritConfigInfo> entry : module.getConfiguration().getSpirits().entrySet()) {
            String expectedName = ITEM_PREFIX + org.bukkit.ChatColor.translateAlternateColorCodes('&', entry.getValue().getName());
            if (displayName.equals(expectedName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Map<Location, String> getPlacedSpiritLocations(Island island) { 
        Map<Location, String> locs = new HashMap<>(); 
        for (String entry : getPlacedSpirits(island)) { 
            String[] parts = entry.split(FIELD_SEPARATOR); 
            if (parts.length >= 4) { 
                org.bukkit.World world = org.bukkit.Bukkit.getWorld(parts[1]); 
                if (world != null) { 
                    try { 
                        int x = Integer.parseInt(parts[2]); 
                        int y = Integer.parseInt(parts[3]); 
                        int z = Integer.parseInt(parts[4]); 
                        locs.put(new Location(world, x, y, z), parts[0]); 
                    } catch (Exception ignored) {} 
                } 
            } 
        } 
        return locs; 
    }

    public List<String> getPlacedSpirits(Island island) {
        return placedSpiritsCache.computeIfAbsent(island, this::loadPlacedSpirits);
    }

    private List<String> loadPlacedSpirits(Island island) {
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

    private void setPlacedSpirits(Island island, List<String> entries) {
        island.getPersistentDataContainer().put(PDC_KEY, PersistentDataType.STRING,
                String.join(ENTRY_SEPARATOR, entries));
        placedSpiritsCache.put(island, new ArrayList<>(entries));
    }

    public void addPlacedSpirit(Island island, String type, Location location) {
        List<String> entries = new ArrayList<>(getPlacedSpirits(island));
        entries.add(type + FIELD_SEPARATOR + locationKey(location));
        setPlacedSpirits(island, entries);
    }

    public String removePlacedSpirit(Island island, Location location) {
        List<String> entries = new ArrayList<>(getPlacedSpirits(island));
        String locationKey = locationKey(location);

        for (int i = 0; i < entries.size(); i++) {
            String entry = entries.get(i);
            int splitIndex = entry.indexOf(FIELD_SEPARATOR);
            if (splitIndex < 0) continue;
            
            if (entry.substring(splitIndex + 1).equals(locationKey)) {
                String type = entry.substring(0, splitIndex);
                entries.remove(i);
                setPlacedSpirits(island, entries);
                return type;
            }
        }
        return null;
    }

    public boolean isPlacedSpiritAt(Island island, Location location) {
        String locationKey = locationKey(location);
        for (String entry : getPlacedSpirits(island)) {
            int splitIndex = entry.indexOf(FIELD_SEPARATOR);
            if (splitIndex < 0) continue;
            if (entry.substring(splitIndex + 1).equals(locationKey))
                return true;
        }
        return false;
    }

    private static String locationKey(Location location) {
        return location.getWorld().getName() + FIELD_SEPARATOR + location.getBlockX() +
                FIELD_SEPARATOR + location.getBlockY() + FIELD_SEPARATOR + location.getBlockZ();
    }
}
