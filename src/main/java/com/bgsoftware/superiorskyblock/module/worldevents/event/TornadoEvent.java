package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class TornadoEvent extends IslandWorldEvent {

    private static final int DURATION_TICKS = 20 * 60 * 3;

    public TornadoEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§b🌪 A §fTornado §bhas formed! Defeat the §eStorm Spirit§b!");

        World world = center.getWorld();
        Location bossLoc = center.clone().add(0, 3, 0);

        Zombie boss = (Zombie) world.spawnEntity(bossLoc, EntityType.ZOMBIE);
        boss.setCustomName("§b⚡ Storm Spirit");
        boss.setCustomNameVisible(true);
        boss.setMaxHealth(120.0);
        boss.setHealth(120.0);
        boss.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));

        // Tornado particle effect using Effect enum (1.8 compatible)
        BukkitRunnable particleTask = new BukkitRunnable() {
            double angle = 0;
            int elapsed = 0;
            @Override public void run() {
                elapsed += 2;
                if (elapsed >= DURATION_TICKS || !boss.isValid()) { cancel(); return; }
                angle += 15;
                for (int layer = 0; layer < 8; layer++) {
                    double yOff   = layer * 0.5;
                    double radius = 1.5 + layer * 0.3;
                    double a      = Math.toRadians(angle + layer * 22);
                    Location p    = center.clone().add(Math.cos(a) * radius, yOff, Math.sin(a) * radius);
                    fx(p, 1, "LARGE_SMOKE", "CLOUD");
                    fx(p, 1, "CRIT");
                }
            }
        };
        particleTask.runTaskTimer(plugin, 0L, 2L);

        new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 20;
                if (!boss.isValid()) {
                    cancel(); particleTask.cancel();
                    world.dropItemNaturally(boss.getLocation(), named(Material.NETHER_STAR, "§b§lStorm Core"));
                    broadcast("§a🌪 Storm Spirit defeated! §eStorm Core §ahas dropped!");
                    sound(center, 1f, 1.5f, "ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH");
                    onFinish.run(); return;
                }
                if (elapsed >= DURATION_TICKS) {
                    cancel(); particleTask.cancel(); boss.remove();
                    broadcast("§c🌪 The Tornado dissipated...");
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
