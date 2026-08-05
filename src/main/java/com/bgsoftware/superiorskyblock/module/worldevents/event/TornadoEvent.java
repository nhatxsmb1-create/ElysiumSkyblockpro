package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class TornadoEvent extends IslandWorldEvent {
    private static final int DURATION = 20 * 60 * 3;

    public TornadoEvent(Island island, Location center) {
        super(island, center, WorldEventType.TORNADO);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§b🌪 §fMột §b§lLốc Xoáy §fkhổng lồ đang hình thành! Tiêu diệt §e§lHồn Bão§f!");
        countdown("§eĐang tạo lốc xoáy...", () -> spawnBoss(onFinish));
    }

    private void spawnBoss(Runnable onFinish) {
        World world = center.getWorld();

        // Spawn near a player rather than fixed center
        Location tornadoPos = getPlayerNearbySpawn(20).clone();

        Zombie boss = (Zombie) world.spawnEntity(tornadoPos.clone().add(0, 3, 0), EntityType.ZOMBIE);
        boss.setCustomName("§b⚡ Hồn Bão");
        boss.setCustomNameVisible(true);
        double hp = scaledHP(120.0);
        boss.setMaxHealth(hp);
        boss.setHealth(hp);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        targetNearestPlayer(boss);
        trackHPBar(boss, "§b⚡ Hồn Bão");

        BukkitRunnable tornado = new BukkitRunnable() {
            double a = 0;
            int e = 0;
            double driftAngle = rng.nextDouble() * Math.PI * 2;

            @Override
            public void run() {
                e += 2;
                if (e >= DURATION || !boss.isValid()) { cancel(); return; }
                a += 18; // rotation speed

                // ── Move tornado (random walk toward players) ──────────
                driftAngle += (rng.nextDouble() - 0.5) * 0.4;
                tornadoPos.add(Math.cos(driftAngle) * 0.18, 0, Math.sin(driftAngle) * 0.18);

                // Pull toward nearest player every 3 sec to stay engaged
                if (e % 60 == 0) {
                    Player nearest = null;
                    double best = Double.MAX_VALUE;
                    for (Player p : getOnlinePlayers()) {
                        double d = p.getLocation().distanceSquared(tornadoPos);
                        if (d < best) { best = d; nearest = p; }
                    }
                    if (nearest != null && tornadoPos.distance(nearest.getLocation()) > 20) {
                        Vector pull = nearest.getLocation().toVector()
                                .subtract(tornadoPos.toVector()).normalize().multiply(0.25);
                        tornadoPos.add(pull.getX(), 0, pull.getZ());
                    }
                }

                // Keep within 35 blocks of island center
                if (tornadoPos.distance(center) > 35.0) {
                    Vector back = center.toVector().subtract(tornadoPos.toVector()).normalize().multiply(0.3);
                    tornadoPos.add(back.getX(), 0, back.getZ());
                }

                int groundY = tornadoPos.getWorld().getHighestBlockYAt(tornadoPos);
                tornadoPos.setY(groundY);
                boss.teleport(tornadoPos.clone().add(0, 1.5, 0));
                targetNearestPlayer(boss);

                // ── TORNADO VISUAL: dense multi-layer helix ────────────
                // Layer 1-4: ground dust ring (SMOKE effect, always visible)
                for (int layer = 0; layer < 14; layer++) {
                    double layerAng = Math.toRadians(a + layer * 25.7);
                    double r = Math.max(0.3, layer * 0.55);
                    double yOff = layer * 0.6;

                    // Inner core smoke column
                    Location core = tornadoPos.clone().add(Math.cos(layerAng) * r, yOff, Math.sin(layerAng) * r);
                    fx(core, 1, "SMOKE");
                    if (layer < 4) fx(core, 1, "SNOW_SHOVEL", "CLOUD");

                    // Counter-spin outer debris ring
                    double counterAng = Math.toRadians(-a * 0.7 + layer * 30);
                    double outerR = r + 1.5;
                    Location debris = tornadoPos.clone().add(Math.cos(counterAng) * outerR, yOff * 0.8, Math.sin(counterAng) * outerR);
                    // Use CRIT for flying debris particles
                    fx(debris, 1, "CRIT");
                    // Every other layer, add magic particles
                    if (layer % 2 == 0) {
                        particle(debris, 1, "CLOUD");
                        particle(debris, 1, "SWEEP_ATTACK");
                    }
                }

                // Bottom ground swirl
                for (int i = 0; i < 12; i++) {
                    double ga = Math.toRadians(a * 2 + i * 30);
                    Location gp = tornadoPos.clone().add(Math.cos(ga) * 2.5, 0.1, Math.sin(ga) * 2.5);
                    fx(gp, 1, "SMOKE");
                    particle(gp, 1, "BLOCK_CRACK");
                }

                // Wind sound
                if (e % 8 == 0) {
                    sound(tornadoPos, 0.9f, 0.5f + rng.nextFloat() * 0.3f,
                            "GHAST_FIREBALL", "ENTITY_GHAST_SHOOT");
                }

                // ── PLAYER PULL: orbit + loft ──────────────────────────
                for (Player p : getOnlinePlayers()) {
                    if (!p.getWorld().equals(tornadoPos.getWorld())) continue;
                    double dist = p.getLocation().distance(tornadoPos);

                    if (dist < 18.0) {
                        Vector toCenter = tornadoPos.toVector().subtract(p.getLocation().toVector());
                        toCenter.setY(0);

                        // Tangent = perpendicular to pull = orbital spin
                        Vector tangent = new Vector(-toCenter.getZ(), 0, toCenter.getX());
                        if (tangent.lengthSquared() > 0) tangent.normalize();
                        if (toCenter.lengthSquared() > 0) toCenter.normalize();

                        double pullStr = dist < 8.0 ? 0.30 : 0.16;
                        double orbitStr = dist < 8.0 ? 0.25 : 0.18;
                        double liftStr  = dist < 8.0 ? 0.55 : 0.30;

                        Vector result = toCenter.multiply(pullStr).add(tangent.multiply(orbitStr));
                        result.setY(liftStr);
                        p.setVelocity(result);

                        if (e % 20 == 0) {
                            p.sendMessage("§b§l🌪 Bạn đang bị cuốn bay bởi lốc xoáy!");
                            sound(p.getLocation(), 0.5f, 0.8f, "BAT_LOOP", "ENTITY_BAT_LOOP");
                        }
                    }
                }
            }
        };
        tornado.runTaskTimer(plugin, 0L, 2L);

        new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 20;
                if (!boss.isValid()) {
                    cancel(); tornado.cancel();
                    world.dropItemNaturally(boss.getLocation(), named(Material.NETHER_STAR, "§b§lLõi Bão"));
                    if (hasLootBonus())
                        world.dropItemNaturally(boss.getLocation(), named(Material.GOLD_INGOT, "§b§lMảnh Sét"));
                    broadcast("§a🌪 Hồn Bão đã bị tiêu diệt! §eLõi Bão §ađã rơi xuống!");
                    sound(center, 1f, 1.5f, "ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH");
                    logResult("HOÀN THÀNH"); onFinish.run(); return;
                }
                if (e >= DURATION) {
                    cancel(); tornado.cancel(); boss.remove();
                    broadcast("§c🌪 Lốc Xoáy đã tan biến... Hồn Bão đã thoát.");
                    logResult("HẾT GIỜ"); onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
