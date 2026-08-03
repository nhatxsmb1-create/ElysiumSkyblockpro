package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class MeteorShowerEvent extends IslandWorldEvent {

    private static final int METEOR_COUNT    = 5;
    private static final int ISLAND_RADIUS   = 55;
    private static final int PICKUP_WINDOW_S = 15;

    public MeteorShowerEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§e🌠 §fA §eMeteor Shower §fis incoming! Watch the skies!");
        World world = center.getWorld();
        Random rng  = new Random();

        new BukkitRunnable() {
            int count = 0;
            @Override public void run() {
                if (count >= METEOR_COUNT) {
                    cancel();
                    broadcast("§e🌠 The shower has passed.");
                    onFinish.run();
                    return;
                }
                count++;
                double ox = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                double oz = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                Location impact = center.clone().add(ox, 0, oz);
                impact.setY(world.getHighestBlockYAt(impact));
                dropMeteor(plugin, world, impact, rng);
            }
        }.runTaskTimer(plugin, 40L, 20 * 20L);
    }

    private void dropMeteor(SuperiorSkyblockPlugin plugin, World world, Location ground, Random rng) {
        broadcast("§e🌠 A meteor is incoming!");
        sound(ground.clone().add(0, 50, 0), 1f, 0.4f, "FIREWORK_LAUNCH", "ENTITY_FIREWORK_ROCKET_LAUNCH");

        new BukkitRunnable() {
            double y = 50;
            @Override public void run() {
                y -= 3;
                Location cur = ground.clone().add(0, y, 0);
                fx(cur, 3, "FLAME");
                fx(cur, 2, "LAVADRIP");
                if (y <= 0) {
                    cancel();
                    impact(plugin, world, ground, rng);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void impact(SuperiorSkyblockPlugin plugin, World world, Location loc, Random rng) {
        sound(loc, 1f, 0.7f, "EXPLODE", "ENTITY_GENERIC_EXPLODE");
        fx(loc, 3, "EXPLOSION_LARGE");
        fx(loc, 10, "LAVADRIP");
        broadcast("§6☄ Meteor landed at §e" + fmt(loc) + "§6! Grab loot in §c" + PICKUP_WINDOW_S + "s§6!");

        Item loot = world.dropItem(loc.clone().add(0, 1, 0), randomLoot(rng));
        loot.setPickupDelay(0);

        if (rng.nextInt(100) < 20) {
            Zombie mini = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
            mini.setCustomName("§6Meteor Golem");
            mini.setCustomNameVisible(true);
            mini.setMaxHealth(80.0);
            mini.setHealth(80.0);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (loot.isValid()) {
                loot.remove();
                broadcast("§c☄ The meteor loot crumbled away...");
            }
        }, PICKUP_WINDOW_S * 20L);
    }

    private ItemStack randomLoot(Random rng) {
        Material[] opts = {Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT};
        Material mat = opts[rng.nextInt(opts.length)];
        ItemStack item = new ItemStack(mat, 1 + rng.nextInt(3));
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName("§6§lMeteor " + mat.name()); item.setItemMeta(meta); }
        return item;
    }

    private String fmt(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}
