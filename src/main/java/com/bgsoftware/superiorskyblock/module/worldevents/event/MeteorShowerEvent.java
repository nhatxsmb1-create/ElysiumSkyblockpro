package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * 🌠 METEOR SHOWER
 * - 5 meteors fall at random island positions over 2 minutes
 * - First player to reach impact site within 15s gets rare ore + possible mini-boss
 */
public class MeteorShowerEvent extends IslandWorldEvent {

    private static final int METEOR_COUNT    = 5;
    private static final int ISLAND_RADIUS   = 55;
    private static final int PICKUP_WINDOW_S = 15; // seconds to reach impact

    public MeteorShowerEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        broadcast("§e🌠 §fA §eMeteor Shower §fis incoming! Watch the skies!");
        World world = center.getWorld();
        Random rng  = new Random();

        int[] remaining = {METEOR_COUNT};

        // Drop meteors every 20 seconds
        new BukkitRunnable() {
            int count = 0;
            @Override public void run() {
                if (count >= METEOR_COUNT) {
                    cancel();
                    if (remaining[0] == 0) {
                        broadcast("§a🌠 Meteor Shower complete!");
                        onFinish.run();
                    } else {
                        broadcast("§e🌠 The shower has passed.");
                        onFinish.run();
                    }
                    return;
                }
                count++;

                double ox = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                double oz = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                Location impactLoc = center.clone().add(ox, 0, oz);

                // Find ground
                impactLoc = findGround(impactLoc, world);

                dropMeteor(plugin, world, impactLoc, rng, remaining, onFinish);
            }
        }.runTaskTimer(plugin, 40L, 20 * 20L); // first drop 2s in, then every 20s
    }

    private void dropMeteor(SuperiorSkyblockPlugin plugin, World world,
                             Location groundLoc, Random rng,
                             int[] remaining, Runnable onFinish) {
        Location launchLoc = groundLoc.clone().add(0, 50, 0);
        broadcast("§e🌠 A meteor is incoming! Get ready!");
        world.playSound(launchLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 0.4f);

        // Falling trail
        new BukkitRunnable() {
            double y = 50;
            @Override public void run() {
                y -= 3;
                Location cur = groundLoc.clone().add(0, y, 0);
                world.spawnParticle(Particle.FLAME,       cur, 5,  0.3, 0.1, 0.3, 0.05);
                world.spawnParticle(Particle.LAVA,        cur, 3,  0.2, 0.1, 0.2, 0.01);
                if (y <= 0) {
                    cancel();
                    impact(plugin, world, groundLoc, rng, remaining, onFinish);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void impact(SuperiorSkyblockPlugin plugin, World world,
                        Location loc, Random rng, int[] remaining, Runnable onFinish) {
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.7f);
        world.spawnParticle(Particle.EXPLOSION_HUGE, loc, 3);
        world.spawnParticle(Particle.LAVA, loc, 20, 0.5, 0.5, 0.5, 0.1);
        broadcast("§6☄ Meteor impact at §e" + locStr(loc) + "§6! Reach it in §c" + PICKUP_WINDOW_S + "s§6!");

        // Drop loot that expires after PICKUP_WINDOW_S seconds
        Item lootItem = world.dropItem(loc.clone().add(0, 1, 0), randomMeteorLoot(rng));
        lootItem.setCustomName("§6§lMeteor Loot");
        lootItem.setPickupDelay(0);

        // 20% chance for mini-boss
        if (rng.nextInt(100) < 20) {
            Zombie mini = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
            mini.setCustomName("§6Meteor Golem");
            mini.setCustomNameVisible(true);
            mini.setMaxHealth(80.0);
            mini.setHealth(80.0);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (lootItem.isValid()) {
                lootItem.remove();
                broadcast("§c☄ The meteor loot crumbled before anyone reached it.");
            }
            remaining[0]--;
        }, PICKUP_WINDOW_S * 20L);
    }

    private ItemStack randomMeteorLoot(Random rng) {
        Material[] options = {Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT};
        Material mat = options[rng.nextInt(options.length)];
        ItemStack item = new ItemStack(mat, 1 + rng.nextInt(3));
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName("§6§lMeteor " + mat.name()); item.setItemMeta(meta); }
        return item;
    }

    private Location findGround(Location loc, World world) {
        Location check = loc.clone();
        check.setY(world.getHighestBlockYAt(check));
        return check;
    }

    private String locStr(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}
