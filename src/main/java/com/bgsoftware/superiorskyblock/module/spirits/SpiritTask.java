package com.bgsoftware.superiorskyblock.module.spirits;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule.SpiritConfigInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SpiritTask extends BukkitRunnable {

    private final SuperiorSkyblockPlugin plugin;
    private final SpiritsModule module;
    private final Random random = new Random();
    
    private int tickCounter = 0;

    private static final Material[] MINER_DROPS = {
            Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE,
            Material.COAL, Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND
    };
    
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
            Island island = plugin.getGrid().getIsland(p);
            if (island != null) {
                activeIslands.add(island);
            }
        }

        for (Island island : activeIslands) {
            Map<Location, String> spirits = module.getSpiritManager().getPlacedSpiritLocations(island);
            if (spirits.isEmpty()) continue;

            for (Map.Entry<Location, String> entry : spirits.entrySet()) {
                Location loc = entry.getKey();
                String type = entry.getValue();
                SpiritConfigInfo info = module.getConfiguration().getSpirits().get(type);
                if (info == null) continue;

                if (loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                    spawnParticle(loc.clone().add(0.5, 1.2, 0.5), info.getParticle());
                }

                if (tickCounter % info.getActionIntervalTicks() == 0) {
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
            drop = MINER_DROPS[random.nextInt(MINER_DROPS.length)];
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
