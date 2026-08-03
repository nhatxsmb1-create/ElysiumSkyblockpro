package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class VolcanoEvent extends IslandWorldEvent {

    private static final int DURATION_TICKS = 20 * 60 * 4;
    private static final int ISLAND_RADIUS  = 60;

    public VolcanoEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§c🌋 §fA §cVolcano §ferupts near your island!");
        broadcast("§7The sky turns red... ash begins to fall.");

        World world = center.getWorld();
        Random rng  = new Random();

        BukkitRunnable ashTask = new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 4;
                if (elapsed >= DURATION_TICKS) { cancel(); return; }
                for (int i = 0; i < 6; i++) {
                    double ox = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                    double oz = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                    Location p = center.clone().add(ox, 18 + rng.nextInt(8), oz);
                    fx(p, 1, "LARGE_SMOKE");
                    fx(p, 1, "FLAME");
                }
            }
        };
        ashTask.runTaskTimer(plugin, 0L, 4L);

        BukkitRunnable meteorTask = new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 100;
                if (elapsed >= DURATION_TICKS) { cancel(); return; }
                double ox = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                double oz = (rng.nextDouble() - 0.5) * ISLAND_RADIUS;
                Location launchFrom = center.clone().add(ox, 40, oz);
                Fireball fb = (Fireball) world.spawnEntity(launchFrom, EntityType.FIREBALL);
                fb.setDirection(new Vector(0, -1, 0));
                fb.setYield(1.5f);
                fb.setIsIncendiary(false);
                sound(launchFrom, 0.5f, 0.6f, "FIREWORK_LAUNCH", "ENTITY_FIREWORK_ROCKET_LAUNCH");
            }
        };
        meteorTask.runTaskTimer(plugin, 40L, 100L);

        Location bossLoc = center.clone().add(0, 2, 0);
        Blaze boss = (Blaze) world.spawnEntity(bossLoc, EntityType.BLAZE);
        boss.setCustomName("§c🌋 Fire Golem");
        boss.setCustomNameVisible(true);
        boss.setMaxHealth(180.0);
        boss.setHealth(180.0);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));

        new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 20;
                if (!boss.isValid()) {
                    cancel(); ashTask.cancel(); meteorTask.cancel();
                    Location drop = boss.getLocation();
                    world.dropItemNaturally(drop, named(Material.MAGMA_CREAM, "§c§lMagma Crystal"));
                    world.dropItemNaturally(drop, named(Material.BLAZE_ROD,   "§6§lLava Core"));
                    world.dropItemNaturally(drop, named(Material.NETHERRACK,  "§4§lVolcanic Ore"));
                    if (rng.nextInt(100) < 30)
                        world.dropItemNaturally(drop, named(Material.NETHER_STAR, "§c§lInfernal Gem"));
                    broadcast("§a🌋 Fire Golem defeated! Volcanic loot has dropped!");
                    sound(center, 1f, 0.8f, "ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH");
                    onFinish.run(); return;
                }
                if (elapsed >= DURATION_TICKS) {
                    cancel(); ashTask.cancel(); meteorTask.cancel(); boss.remove();
                    broadcast("§c🌋 The volcano calmed down...");
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
