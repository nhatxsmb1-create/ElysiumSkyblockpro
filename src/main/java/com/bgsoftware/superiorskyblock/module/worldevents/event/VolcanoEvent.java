package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * 🌋 VOLCANO EVENT
 * - Ash particles fall around island (LAVA_POP, SMOKE, FLAME)
 * - Meteorites (fireballs) rain down on random points
 * - Spawns Fire Golem (Blaze boss)
 * - Drops: Magma Crystal, Lava Core, Volcanic Ore
 */
public class VolcanoEvent extends IslandWorldEvent {

    private static final int DURATION_TICKS  = 20 * 60 * 4; // 4 min
    private static final int ISLAND_RADIUS   = 60;

    public VolcanoEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        broadcast("§c🌋 §fA §cVolcano §ferupts near your island! Survive the fire!");
        broadcast("§7The sky turns red... ash begins to fall.");

        World world = center.getWorld();
        Random rng  = new Random();

        // Ash / ember particles
        BukkitRunnable ashTask = new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 4;
                if (elapsed >= DURATION_TICKS) { cancel(); return; }
                for (int i = 0; i < 8; i++) {
                    double ox = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                    double oz = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                    Location p = center.clone().add(ox, 20 + rng.nextInt(10), oz);
                    world.spawnParticle(Particle.SMOKE_LARGE,  p, 1, 0.1, 0, 0.1, 0.02);
                    world.spawnParticle(Particle.FLAME,         p, 1, 0.1, 0, 0.1, 0.04);
                }
            }
        };
        ashTask.runTaskTimer(plugin, 0L, 4L);

        // Falling fireballs every 5 seconds
        BukkitRunnable meteorTask = new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 100;
                if (elapsed >= DURATION_TICKS) { cancel(); return; }

                double ox = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                double oz = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                Location launchFrom = center.clone().add(ox, 40, oz);

                // Small fireball dropping straight down
                Fireball fb = (Fireball) world.spawnEntity(launchFrom, EntityType.SMALL_FIREBALL);
                fb.setDirection(new org.bukkit.util.Vector(0, -1, 0));
                fb.setYield(1.5f);
                fb.setIsIncendiary(false);

                world.playSound(launchFrom, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 0.6f);
            }
        };
        meteorTask.runTaskTimer(plugin, 40L, 100L);

        // Spawn Fire Golem (Blaze)
        Location bossLoc = center.clone().add(0, 2, 0);
        Blaze boss = (Blaze) world.spawnEntity(bossLoc, EntityType.BLAZE);
        boss.setCustomName("§c🌋 Fire Golem");
        boss.setCustomNameVisible(true);
        boss.setMaxHealth(180.0);
        boss.setHealth(180.0);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));

        // Timeout / boss kill checker
        new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 20;
                if (!boss.isValid()) {
                    cancel();
                    ashTask.cancel();
                    meteorTask.cancel();
                    dropVolcanoLoot(world, boss.getLocation(), rng);
                    broadcast("§a🌋 Fire Golem defeated! Volcanic loot has dropped!");
                    world.playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 0.8f);
                    onFinish.run();
                    return;
                }
                if (elapsed >= DURATION_TICKS) {
                    cancel();
                    ashTask.cancel();
                    meteorTask.cancel();
                    boss.remove();
                    broadcast("§c🌋 The volcano calmed down... Fire Golem vanished.");
                    onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void dropVolcanoLoot(World world, Location loc, Random rng) {
        world.dropItemNaturally(loc, named(Material.MAGMA_CREAM,  "§c§lMagma Crystal"));
        world.dropItemNaturally(loc, named(Material.BLAZE_ROD,    "§6§lLava Core"));
        world.dropItemNaturally(loc, named(Material.NETHERRACK,   "§4§lVolcanic Ore"));
        if (rng.nextInt(100) < 30) { // 30% bonus drop
            world.dropItemNaturally(loc, named(Material.NETHER_STAR, "§c§lInfernal Gem"));
        }
    }

    private ItemStack named(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }
}
