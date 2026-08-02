package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 🌪 TORNADO EVENT
 * - Swirling particle vortex appears above island center
 * - Spawns a "Storm Spirit" zombie boss with custom name
 * - Killing it drops Storm Core (nether star named item)
 * - Duration: 3 minutes; event ends on boss kill or timeout
 */
public class TornadoEvent extends IslandWorldEvent {

    private static final int DURATION_TICKS = 20 * 60 * 3; // 3 min
    private static final int PARTICLE_TICK  = 2;

    public TornadoEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        broadcast("§b🌪 A §fTornado §bhas formed above your island! Defeat the §eStorm Spirit§b!");

        World world = center.getWorld();

        // Spawn Storm Spirit slightly above center
        Location bossLoc = center.clone().add(0, 3, 0);
        Zombie boss = (Zombie) world.spawnEntity(bossLoc, EntityType.ZOMBIE);
        boss.setCustomName("§b⚡ Storm Spirit");
        boss.setCustomNameVisible(true);
        boss.setMaxHealth(120.0);
        boss.setHealth(120.0);
        boss.setBaby(false);
        boss.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));

        List<BukkitRunnable> tasks = new ArrayList<>();

        // Tornado particle effect
        BukkitRunnable particleTask = new BukkitRunnable() {
            double angle = 0;
            int elapsed = 0;

            @Override
            public void run() {
                elapsed += PARTICLE_TICK;
                if (elapsed >= DURATION_TICKS || !boss.isValid()) {
                    cancel();
                    return;
                }

                angle += 15;
                for (int layer = 0; layer < 8; layer++) {
                    double yOff   = layer * 0.5;
                    double radius = 1.5 + layer * 0.3;
                    double a      = Math.toRadians(angle + layer * 22);
                    Location pLoc = center.clone().add(
                            Math.cos(a) * radius, yOff, Math.sin(a) * radius);
                    world.spawnParticle(Particle.CLOUD, pLoc, 1, 0, 0, 0, 0.01);
                    world.spawnParticle(Particle.SWEEP_ATTACK, pLoc, 1);
                }
            }
        };
        particleTask.runTaskTimer(plugin, 0L, PARTICLE_TICK);
        tasks.add(particleTask);

        // Kill-detect / timeout task
        new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                elapsed += 20;

                if (!boss.isValid()) {
                    // Boss killed — drop Storm Core
                    cancel();
                    tasks.forEach(BukkitRunnable::cancel);
                    ItemStack core = makeNamedItem(Material.NETHER_STAR, "§b§lStorm Core");
                    world.dropItemNaturally(boss.getLocation(), core);
                    broadcast("§a🌪 Storm Spirit defeated! §eStorm Core §ahas dropped!");
                    world.playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.5f);
                    onFinish.run();
                    return;
                }

                if (elapsed >= DURATION_TICKS) {
                    // Timeout
                    cancel();
                    tasks.forEach(BukkitRunnable::cancel);
                    boss.remove();
                    broadcast("§c🌪 The Tornado dissipated... Storm Spirit escaped.");
                    onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private ItemStack makeNamedItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }
}
