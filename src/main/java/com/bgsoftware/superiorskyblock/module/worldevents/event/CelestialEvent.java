package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * ☄ CELESTIAL EVENT
 * - Sky turns deep blue/purple (time set to midnight)
 * - Floating Crystals (End Crystals) appear around island
 * - Star Beast (Phantom boss) descends
 * - Drops: Star Fragment, Celestial Dust
 */
public class CelestialEvent extends IslandWorldEvent {

    private static final int CRYSTAL_COUNT   = 5;
    private static final int DURATION_TICKS  = 20 * 60 * 4;

    public CelestialEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        broadcast("§d☄ §fThe §dCelestial Event §fhas begun! The sky shimmers with starlight.");
        World world = center.getWorld();
        Random rng  = new Random();

        // Spawn floating End Crystals around island
        java.util.List<Entity> crystals = new java.util.ArrayList<>();
        for (int i = 0; i < CRYSTAL_COUNT; i++) {
            double a = i * (2 * Math.PI / CRYSTAL_COUNT);
            double r = 15 + rng.nextDouble() * 10;
            Location cLoc = center.clone().add(Math.cos(a) * r, 10 + rng.nextDouble() * 8, Math.sin(a) * r);
            EnderCrystal crystal = (EnderCrystal) world.spawnEntity(cLoc, EntityType.ENDER_CRYSTAL);
            crystal.setShowingBottom(false);
            crystals.add(crystal);
        }

        // Starlight particle effect
        BukkitRunnable starTask = new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 4;
                if (elapsed > DURATION_TICKS) { cancel(); return; }
                for (int i = 0; i < 10; i++) {
                    double ox = (rng.nextDouble() - 0.5) * 60;
                    double oz = (rng.nextDouble() - 0.5) * 60;
                    double oy = 5 + rng.nextDouble() * 20;
                    Location p = center.clone().add(ox, oy, oz);
                    world.spawnParticle(Particle.END_ROD,      p, 1, 0.05, 0.05, 0.05, 0.02);
                    world.spawnParticle(Particle.DRAGON_BREATH, p, 1, 0, 0, 0, 0.01);
                }
            }
        };
        starTask.runTaskTimer(plugin, 0L, 4L);

        // Spawn Star Beast (Phantom)
        Location bossLoc = center.clone().add(0, 20, 0);
        Phantom beast = (Phantom) world.spawnEntity(bossLoc, EntityType.PHANTOM);
        beast.setCustomName("§d✦ Star Beast");
        beast.setCustomNameVisible(true);
        beast.setMaxHealth(150.0);
        beast.setHealth(150.0);

        world.playSound(center, Sound.ENTITY_PHANTOM_AMBIENT, 1f, 0.5f);

        // Kill / timeout watcher
        new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 20;
                if (!beast.isValid()) {
                    cancel();
                    starTask.cancel();
                    crystals.forEach(c -> { if (c.isValid()) c.remove(); });
                    Location dropLoc = beast.getLocation();
                    world.dropItemNaturally(dropLoc, named(Material.GHAST_TEAR,  "§d§lStar Fragment"));
                    world.dropItemNaturally(dropLoc, named(Material.GLOWSTONE_DUST, "§e§lCelestial Dust"));
                    if (rng.nextInt(100) < 20) {
                        world.dropItemNaturally(dropLoc, named(Material.BEACON, "§b§lStellar Core"));
                    }
                    broadcast("§a☄ Star Beast defeated! Celestial rewards have dropped!");
                    world.playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.4f);
                    onFinish.run();
                    return;
                }
                if (elapsed >= DURATION_TICKS) {
                    cancel();
                    starTask.cancel();
                    beast.remove();
                    crystals.forEach(c -> { if (c.isValid()) c.remove(); });
                    broadcast("§c☄ The Celestial Event faded... Star Beast ascended.");
                    onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private ItemStack named(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }
}
