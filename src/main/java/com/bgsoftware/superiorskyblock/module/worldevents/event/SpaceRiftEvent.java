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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 🌀 SPACE RIFT EVENT
 * - Purple portal swirls appear 25 blocks above island center
 * - After 30s: 3 waves of Void Endermen spawn (Enderman with Wither effect)
 * - Survive all waves → Void Relic drops
 */
public class SpaceRiftEvent extends IslandWorldEvent {

    private static final int PORTAL_WARN_TICKS = 20 * 30; // 30 sec warning
    private static final int WAVE_COUNT        = 3;
    private static final int MOB_PER_WAVE      = 3;
    private static final int WAVE_INTERVAL_S   = 45;       // secs between waves

    public SpaceRiftEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        broadcast("§5🌀 §fA §5Space Rift §fhas appeared above your island!");
        broadcast("§7A rift is tearing open... Something is crossing over.");

        World world = center.getWorld();
        Location riftLoc = center.clone().add(0, 25, 0);

        // Portal swirl effect during warning phase
        BukkitRunnable swirlTask = new BukkitRunnable() {
            double angle = 0;
            int elapsed  = 0;
            @Override public void run() {
                elapsed += 2;
                if (elapsed > PORTAL_WARN_TICKS) { cancel(); return; }
                angle += 18;
                for (int i = 0; i < 3; i++) {
                    double a = Math.toRadians(angle + i * 120);
                    Location p = riftLoc.clone().add(Math.cos(a) * 2, 0, Math.sin(a) * 2);
                    world.spawnParticle(Particle.PORTAL,            p, 5, 0.1, 0.3, 0.1, 0.3);
                    world.spawnParticle(Particle.DRAGON_BREATH,     p, 2, 0.1, 0.1, 0.1, 0.02);
                }
            }
        };
        swirlTask.runTaskTimer(plugin, 0L, 2L);

        world.playSound(riftLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.4f);

        // After 30s: send waves
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            swirlTask.cancel();
            broadcast("§5🌀 §cThe Rift tears open! Void Entities pour through!");
            world.playSound(riftLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0.6f);
            runWaves(plugin, world, riftLoc, onFinish);
        }, PORTAL_WARN_TICKS);
    }

    private void runWaves(SuperiorSkyblockPlugin plugin, World world, Location riftLoc, Runnable onFinish) {
        List<LivingEntity> allMobs = new ArrayList<>();
        int[] wavesDone = {0};
        int[] mobsRemaining = {0};

        Runnable spawnNextWave = new Runnable() {
            @Override public void run() {
                wavesDone[0]++;
                broadcast("§5🌀 Wave §e" + wavesDone[0] + "§5/" + WAVE_COUNT + " §fhas arrived!");
                world.playSound(riftLoc, Sound.ENTITY_ENDERMAN_SCREAM, 1f, 0.7f);

                for (int i = 0; i < MOB_PER_WAVE; i++) {
                    double a = Math.random() * Math.PI * 2;
                    Location spawnAt = riftLoc.clone().add(
                            Math.cos(a) * 3, -5, Math.sin(a) * 3);

                    Enderman enderman = (Enderman) world.spawnEntity(spawnAt, EntityType.ENDERMAN);
                    enderman.setCustomName("§5Void Entity §7[W" + wavesDone[0] + "]");
                    enderman.setCustomNameVisible(true);
                    enderman.setMaxHealth(60.0 + wavesDone[0] * 20);
                    enderman.setHealth(enderman.getMaxHealth());
                    enderman.addPotionEffect(new PotionEffect(
                            PotionEffectType.WITHER, Integer.MAX_VALUE, 0, false, false));
                    allMobs.add(enderman);
                }
                mobsRemaining[0] = allMobs.stream().mapToInt(m -> m.isValid() ? 1 : 0).sum();
            }
        };

        // Schedule wave spawns
        for (int w = 0; w < WAVE_COUNT; w++) {
            final int wave = w;
            plugin.getServer().getScheduler().runTaskLater(plugin,
                    spawnNextWave::run, (long) wave * WAVE_INTERVAL_S * 20L);
        }

        // Poll for completion (all mobs dead)
        long totalDuration = (long) (WAVE_COUNT + 1) * WAVE_INTERVAL_S * 20L;
        new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 20;

                boolean allDead = allMobs.stream().noneMatch(Entity::isValid);
                if (allDead && wavesDone[0] >= WAVE_COUNT) {
                    cancel();
                    Location drop = riftLoc.clone().add(0, -5, 0);
                    world.dropItemNaturally(drop, named(Material.CHORUS_FRUIT, "§5§lVoid Relic"));
                    world.dropItemNaturally(drop, named(Material.END_CRYSTAL,  "§d§lRift Fragment"));
                    broadcast("§a🌀 Space Rift closed! §5Void Relic §ahas dropped!");
                    world.playSound(riftLoc, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.2f);
                    onFinish.run();
                    return;
                }
                if (elapsed >= totalDuration + 20 * 60) {
                    cancel();
                    allMobs.forEach(m -> { if (m.isValid()) m.remove(); });
                    broadcast("§c🌀 The Rift sealed itself... the entities retreated.");
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
