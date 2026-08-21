package com.bgsoftware.superiorskyblock.module.spirits;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule.SpiritConfigInfo;
import com.bgsoftware.superiorskyblock.module.spirits.SpiritManager.PlacedSpirit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SpiritTask extends BukkitRunnable {
    public void simulateOffline(Player player, long ticks) {
        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        Island island = sp.getIsland();
        if (island == null) return;
        
        Map<Location, PlacedSpirit> spirits = module.getSpiritManager().getPlacedSpiritLocations(island);
        if (spirits.isEmpty()) return;

        Map<Material, Integer> totalDrops = new java.util.HashMap<>();

        for (Map.Entry<Location, PlacedSpirit> entry : spirits.entrySet()) {
            String type = entry.getValue().getType();
            int level = entry.getValue().getLevel();
            SpiritConfigInfo info = module.getConfiguration().getSpirits().get(type);
            if (info == null) continue;

            int currentInterval = info.getActionIntervalTicks();
            if (info.getUpgrades().containsKey(level)) {
                currentInterval = info.getUpgrades().get(level).getIntervalTicks();
            }

            long actionsCount = ticks / currentInterval;
            if (actionsCount <= 0) continue;

            if (type.equalsIgnoreCase("miner")) {
                Dimension dim = Dimensions.fromEnvironment(World.Environment.NORMAL);
                Map<String, Integer> amounts = island.getGeneratorAmounts(dim);
                if (amounts != null && !amounts.isEmpty()) {
                    int totalWeight = 0;
                    for (Integer weight : amounts.values()) {
                        totalWeight += weight;
                    }
                    if (totalWeight > 0) {
                        for (long i = 0; i < actionsCount; i++) {
                            int rand = random.nextInt(totalWeight);
                            int current = 0;
                            Material drop = null;
                            for (Map.Entry<String, Integer> amt : amounts.entrySet()) {
                                current += amt.getValue();
                                if (rand < current) {
                                    try { drop = Material.matchMaterial(amt.getKey()); } catch (Exception ignored) {}
                                    break;
                                }
                            }
                            if (drop == null) drop = Material.COBBLESTONE;
                            totalDrops.put(drop, totalDrops.getOrDefault(drop, 0) + 1);
                        }
                    } else {
                        totalDrops.put(Material.COBBLESTONE, totalDrops.getOrDefault(Material.COBBLESTONE, 0) + (int)actionsCount);
                    }
                } else {
                    totalDrops.put(Material.COBBLESTONE, totalDrops.getOrDefault(Material.COBBLESTONE, 0) + (int)actionsCount);
                }
            } else if (type.equalsIgnoreCase("farmer")) {
                for (long i = 0; i < actionsCount; i++) {
                    Material drop = FARMER_DROPS[random.nextInt(FARMER_DROPS.length)];
                    totalDrops.put(drop, totalDrops.getOrDefault(drop, 0) + 1);
                }
            }
        }

        if (totalDrops.isEmpty()) return;

        for (Map.Entry<Material, Integer> drop : totalDrops.entrySet()) {
            BuiltinModules.ORE_STORAGE.getStorageManager().addAmount(island.getUniqueId(), drop.getKey(), BigInteger.valueOf(drop.getValue()));
        }
        
        player.sendMessage("§b✨ §e§lBÁO CÁO NGOẠI TUYẾN");
        player.sendMessage("§7Tinh linh của bạn đã làm việc chăm chỉ và đưa vào /is kho:");
        for (Map.Entry<Material, Integer> drop : totalDrops.entrySet()) {
            player.sendMessage("§a+ " + drop.getValue() + " §f" + drop.getKey().name());
        }
        try {
            player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 1f, 1f);
        } catch (Exception ex) {
            try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("LEVEL_UP"), 1f, 1f); } catch (Exception ignored) {}
        }
    }


    private final SuperiorSkyblockPlugin plugin;
    private final SpiritsModule module;
    private final Random random = new Random();
    
    private int tickCounter = 0;

    private static final Material[] FARMER_DROPS = {
            Material.WHEAT, Material.CARROT, Material.POTATO, Material.SUGAR_CANE, Material.MELON
    };

    public SpiritTask(SuperiorSkyblockPlugin plugin, SpiritsModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @Override
    public void run() {
        tickCounter += 20;

        Set<Island> activeIslands = new HashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(p.getUniqueId());
            Island island = sp.getIsland();
            if (island != null) {
                activeIslands.add(island);
            }
        }

        for (Island island : activeIslands) {
            Map<Location, PlacedSpirit> spirits = module.getSpiritManager().getPlacedSpiritLocations(island);
            if (spirits.isEmpty()) continue;

            for (Map.Entry<Location, PlacedSpirit> entry : spirits.entrySet()) {
                Location loc = entry.getKey();
                String type = entry.getValue().getType();
                int level = entry.getValue().getLevel();
                SpiritConfigInfo info = module.getConfiguration().getSpirits().get(type);
                if (info == null) continue;

                if (loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                    spawnParticle(loc.clone().add(0.5, 1.2, 0.5), info.getParticle());
                }

                int currentInterval = info.getActionIntervalTicks();
                if (info.getUpgrades().containsKey(level)) {
                    currentInterval = info.getUpgrades().get(level).getIntervalTicks();
                }
                
                if (tickCounter % currentInterval == 0) {
                    performSpiritAction(island, type);
                }
            }
        }
        
        if (tickCounter >= 1200) {
            tickCounter = 0;
        }
    }

    private void performSpiritAction(Island island, String type) {
        Material drop = null;
        if (type.equalsIgnoreCase("miner")) {
            Dimension dim = Dimensions.fromEnvironment(World.Environment.NORMAL);
            Map<String, Integer> amounts = island.getGeneratorAmounts(dim);
            
            if (amounts != null && !amounts.isEmpty()) {
                int totalWeight = 0;
                for (Integer weight : amounts.values()) {
                    totalWeight += weight;
                }
                
                if (totalWeight > 0) {
                    int rand = random.nextInt(totalWeight);
                    int current = 0;
                    for (Map.Entry<String, Integer> entry : amounts.entrySet()) {
                        current += entry.getValue();
                        if (rand < current) {
                            try {
                                drop = Material.matchMaterial(entry.getKey());
                            } catch (Exception ignored) {
                            }
                            break;
                        }
                    }
                }
            }
            
            if (drop == null) {
                drop = Material.COBBLESTONE; // fallback
            }
            
        } else if (type.equalsIgnoreCase("farmer")) {
            drop = FARMER_DROPS[random.nextInt(FARMER_DROPS.length)];
        }
        
        if (drop != null) {
            BuiltinModules.ORE_STORAGE.getStorageManager().addAmount(island.getUniqueId(), drop, BigInteger.ONE);
        }
    }

    private void spawnParticle(Location loc, String particleName) {
        if (particleName.equalsIgnoreCase("NONE")) return;
        try {
            Object particleEnum = Class.forName("org.bukkit.Particle").getMethod("valueOf", String.class).invoke(null, particleName.toUpperCase());
            loc.getWorld().getClass().getMethod("spawnParticle", Class.forName("org.bukkit.Particle"), Location.class, int.class, double.class, double.class, double.class, double.class)
                    .invoke(loc.getWorld(), particleEnum, loc, 3, 0.3, 0.3, 0.3, 0.02);
        } catch (Exception ignored) {
        }
    }
}
