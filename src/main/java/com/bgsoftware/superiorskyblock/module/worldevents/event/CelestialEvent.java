package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class CelestialEvent extends IslandWorldEvent {

    private static final int DURATION_TICKS = 20 * 60 * 4;

    public CelestialEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§d☄ §fThe §dCelestial Event §fhas begun! The sky shimmers!");
        World world = center.getWorld();
        Random rng  = new Random();

        // Floating Ghasts as "crystals" (Ghast exists in 1.8.8, flies, glows effect)
        // We use ArmorStands with custom names floating around as visual markers instead
        java.util.List<Entity> floaters = new java.util.ArrayList<>();
        int count = 5;
        for (int i = 0; i < count; i++) {
            double a = i * (2 * Math.PI / count);
            double r = 15 + rng.nextDouble() * 8;
            Location fLoc = center.clone().add(Math.cos(a) * r, 10 + rng.nextDouble() * 6, Math.sin(a) * r);
            ArmorStand stand = (ArmorStand) world.spawnEntity(fLoc, EntityType.ARMOR_STAND);
            stand.setCustomName("§b✦ Floating Crystal");
            stand.setCustomNameVisible(true);
            stand.setGravity(false);
            stand.setVisible(false);
            floaters.add(stand);
        }

        // Starlight particle effect
        BukkitRunnable starTask = new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 4;
                if (elapsed > DURATION_TICKS) { cancel(); return; }
                for (int i = 0; i < 8; i++) {
                    double ox = (rng.nextDouble() - 0.5) * 60;
                    double oz = (rng.nextDouble() - 0.5) * 60;
                    double oy = 5 + rng.nextDouble() * 18;
                    Location p = center.clone().add(ox, oy, oz);
                    fx(p, 1, "FIREWORKS_SPARK");
                    fx(p, 1, "WITCH_MAGIC", "SPELL_WITCH");
                }
            }
        };
        starTask.runTaskTimer(plugin, 0L, 4L);

        // Star Beast — Ghast (flying, scary, exists in 1.8.8)
        Location bossLoc = center.clone().add(0, 20, 0);
        Ghast beast = (Ghast) world.spawnEntity(bossLoc, EntityType.GHAST);
        beast.setCustomName("§d✦ Star Beast");
        beast.setCustomNameVisible(true);
        beast.setMaxHealth(150.0);
        beast.setHealth(150.0);

        sound(center, 1f, 0.5f, "GHAST_MOAN", "ENTITY_GHAST_AMBIENT");

        new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 20;
                if (!beast.isValid()) {
                    cancel(); starTask.cancel();
                    floaters.forEach(f -> { if (f.isValid()) f.remove(); });
                    Location drop = beast.getLocation();
                    world.dropItemNaturally(drop, named(Material.GHAST_TEAR,      "§d§lStar Fragment"));
                    world.dropItemNaturally(drop, named(Material.GLOWSTONE_DUST,  "§e§lCelestial Dust"));
                    if (rng.nextInt(100) < 20)
                        world.dropItemNaturally(drop, named(Material.NETHER_STAR, "§b§lStellar Core"));
                    broadcast("§a☄ Star Beast defeated! Celestial rewards dropped!");
                    sound(center, 1f, 1.4f, "ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH");
                    onFinish.run(); return;
                }
                if (elapsed >= DURATION_TICKS) {
                    cancel(); starTask.cancel(); beast.remove();
                    floaters.forEach(f -> { if (f.isValid()) f.remove(); });
                    broadcast("§c☄ The Celestial Event faded...");
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
