package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.*;

public class SpaceRiftEvent extends IslandWorldEvent {
    private static final int WARN = 20 * 25, WAVES = 3, MOB_PER_WAVE = 3, WAVE_INTERVAL = 40;

    public SpaceRiftEvent(Island island, Location center) {
        super(island, center, WorldEventType.SPACE_RIFT);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§5🌀 §fMột §5§lCổng Không Gian §fxuất hiện trên đảo của bạn!");
        broadcast("§7Thực tại đang rạn nứt... Điều gì đó sắp xé toạc qua đó.");
        countdown("§5Cổng đang mở ra...", () -> openRift(onFinish));
    }

    private void openRift(Runnable onFinish) {
        World world = center.getWorld();
        // Rift appears above a player
        Location riftBase = getPlayerNearbySpawn(10);
        riftBase.setY(riftBase.getWorld().getHighestBlockYAt(riftBase) + 20);
        final Location rift = riftBase;

        // Expanding tear animation: PORTAL + SMOKE rings
        BukkitRunnable opening = new BukkitRunnable() {
            double angle = 0;
            double radius = 0.3;
            int e = 0;
            @Override
            public void run() {
                e += 2;
                if (e > WARN) { cancel(); return; }
                angle += 18;
                radius = Math.min(7.0, 0.3 + (e / (double) WARN) * 6.7);

                // Main rift portal ring
                for (int i = 0; i < 5; i++) {
                    double ang = Math.toRadians(angle + i * 72);
                    Location p = rift.clone().add(Math.cos(ang) * radius, 0, Math.sin(ang) * radius);
                    fx(p, 2, "PORTAL");
                    particle(p, 2, "PORTAL");
                }
                // Inner glow core
                fx(rift, 2, "PORTAL");
                particle(rift, 3, "PORTAL");

                // Growing smoke halo
                if (e % 10 == 0) {
                    for (int i = 0; i < 8; i++) {
                        double ang = rng.nextDouble() * Math.PI * 2;
                        Location halo = rift.clone().add(
                                Math.cos(ang) * (radius * 1.3), (rng.nextDouble() - 0.5) * 2,
                                Math.sin(ang) * (radius * 1.3));
                        fx(halo, 1, "SMOKE");
                        particle(halo, 1, "SMOKE_LARGE");
                    }
                }

                // Gravity pulse toward rift
                if (e % 50 == 0) {
                    sound(rift, 0.8f, 0.4f + (e / (float) WARN * 0.5f), "ENDERMAN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT");
                    for (Player p : getOnlinePlayers()) {
                        if (!p.getWorld().equals(rift.getWorld())) continue;
                        double dist = p.getLocation().distance(rift);
                        if (dist < 20.0) {
                            p.sendMessage("§5🌀 Cổng đang hút bạn lại!");
                            Vector pull = rift.toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.12);
                            pull.setY(0.06);
                            p.setVelocity(pull);
                        }
                    }
                }
            }
        };
        opening.runTaskTimer(plugin, 0L, 2L);
        sound(rift, 1f, 0.4f, "ENDERMAN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            opening.cancel();
            broadcast("§5🌀 §cCổng xé toạc! §5Thực Thể Hư Vô §ctràn ra!");
            sound(rift, 1f, 0.6f, "ENDERDRAGON_GROWL", "ENTITY_ENDER_DRAGON_GROWL");
            runWaves(world, rift, onFinish);
        }, WARN);
    }

    private void runWaves(World world, Location rift, Runnable onFinish) {
        List<LivingEntity> mobs = new ArrayList<>();
        int[] wavesDone = {0};
        double mobHP = scaledHP(60.0);

        Runnable spawnWave = () -> {
            wavesDone[0]++;
            broadcast("§5🌀 §fĐợt §e" + wavesDone[0] + "§5/" + WAVES + " §fxuất hiện từ cổng!");
            sound(rift, 1f, 0.7f, "ENDERMAN_SCREAM", "ENTITY_ENDERMAN_SCREAM");
            // Spawn burst: portal particles
            for (int j = 0; j < 30; j++) {
                double ra = rng.nextDouble() * Math.PI * 2;
                double rr = rng.nextDouble() * 5;
                Location sp = rift.clone().add(
                        Math.cos(ra) * rr, (rng.nextDouble() - 0.5) * 3, Math.sin(ra) * rr);
                fx(sp, 2, "PORTAL");
                particle(sp, 2, "PORTAL");
            }
            for (int i = 0; i < MOB_PER_WAVE; i++) {
                double a = Math.random() * Math.PI * 2;
                Location sp = rift.clone().add(Math.cos(a) * 3, -4, Math.sin(a) * 3);
                Enderman e = (Enderman) world.spawnEntity(sp, EntityType.ENDERMAN);
                e.setCustomName("§5Thực Thể Hư Vô §7[Đợt " + wavesDone[0] + "]");
                e.setCustomNameVisible(true);
                e.setMaxHealth(mobHP + wavesDone[0] * 12);
                e.setHealth(e.getMaxHealth());
                e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 0, false, false));
                targetNearestPlayer(e);
                mobs.add(e);
            }
        };

        for (int w = 0; w < WAVES; w++)
            plugin.getServer().getScheduler().runTaskLater(plugin, spawnWave::run, (long) w * WAVE_INTERVAL * 20L);

        // Void maelstrom: continuous spiral + gravity pull
        BukkitRunnable maelstrom = new BukkitRunnable() {
            double swirlAngle = 0;
            int e = 0;
            @Override
            public void run() {
                e += 4;
                if (wavesDone[0] >= WAVES && mobs.stream().noneMatch(Entity::isValid)) { cancel(); return; }
                swirlAngle += 15;

                // Spiral arms
                for (int arm = 0; arm < 3; arm++) {
                    double baseAng = Math.toRadians(swirlAngle + arm * 120);
                    for (int step = 1; step <= 4; step++) {
                        double r = step * 1.2;
                        double ang = baseAng + step * 0.3;
                        Location p = rift.clone().add(Math.cos(ang) * r, 0, Math.sin(ang) * r);
                        fx(p, 1, "PORTAL");
                        particle(p, 1, "PORTAL");
                    }
                }
                // Central eye
                fx(rift, 2, "PORTAL");
                particle(rift, 3, "PORTAL");

                // Pull + levitate players
                if (e % 30 == 0) {
                    for (Player p : getOnlinePlayers()) {
                        if (!p.getWorld().equals(rift.getWorld())) continue;
                        double dist = p.getLocation().distance(rift);
                        if (dist < 28.0) {
                            double str = dist < 12.0 ? 0.28 : 0.14;
                            p.sendMessage("§d§l🌀 Trọng lực hư vô kéo bạn lên!");
                            PotionEffectType lev = PotionEffectType.getByName("LEVITATION");
                            if (lev != null) p.addPotionEffect(new PotionEffect(lev, 25, dist < 10.0 ? 2 : 0));
                            Vector pull = rift.toVector().subtract(p.getLocation().toVector());
                            pull.setY(0);
                            if (pull.lengthSquared() > 0) pull.normalize().multiply(str);
                            pull.setY(dist < 10.0 ? 0.25 : 0.1);
                            p.setVelocity(pull);
                            fx(p.getLocation(), 3, "PORTAL");
                            particle(p.getLocation(), 3, "PORTAL");
                        }
                    }
                }

                // Re-target mobs every 3s
                if (e % 60 == 0) {
                    mobs.forEach(m -> { if (m.isValid()) targetNearestPlayer(m); });
                }

                // Dimensional shockwave every 8s
                if (e % 160 == 0) {
                    sound(rift, 1f, 0.5f, "ENDERDRAGON_GROWL", "ENTITY_ENDER_DRAGON_GROWL");
                    for (int i = 0; i < 30; i++) {
                        double ra = rng.nextDouble() * Math.PI * 2;
                        double rr = 1 + rng.nextDouble() * 7;
                        Location sp = rift.clone().add(
                                Math.cos(ra) * rr, (rng.nextDouble() - 0.5) * 5, Math.sin(ra) * rr);
                        fx(sp, 3, "PORTAL");
                        particle(sp, 3, "PORTAL");
                    }
                    broadcast("§5🌀 Cổng không gian rung chuyển!");
                    for (Player p : getOnlinePlayers()) {
                        if (p.getWorld().equals(rift.getWorld()) && p.getLocation().distance(rift) < 22.0) {
                            p.sendMessage("§5🌀 Chấn động hư không!");
                            PotionEffectType blind = PotionEffectType.getByName("BLINDNESS");
                            if (blind != null) p.addPotionEffect(new PotionEffect(blind, 25, 0));
                        }
                    }
                }
            }
        };
        maelstrom.runTaskTimer(plugin, 0L, 4L);

        long timeout = (long) (WAVES + 1) * WAVE_INTERVAL * 20L + 20 * 60L;
        new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 20;
                if (mobs.stream().noneMatch(Entity::isValid) && wavesDone[0] >= WAVES) {
                    cancel(); maelstrom.cancel();
                    Location drop = rift.clone().add(0, -5, 0);
                    world.dropItemNaturally(drop, named(Material.ENDER_PEARL, "§5§lThánh Vật Hư Vô"));
                    world.dropItemNaturally(drop, named(Material.EMERALD, "§d§lMảnh Cổng Không Gian"));
                    if (hasLootBonus()) {
                        world.dropItemNaturally(drop, named(Material.OBSIDIAN, "§5§lTinh Chất Hư Vô", 5));
                        broadcast("§d🌀 §lPhần thưởng đặc biệt! §r§dTinh Chất Hư Vô đã rơi!");
                    }
                    broadcast("§a🌀 Cổng Không Gian đã đóng lại!");
                    dropTrophy(drop, "space-rift");
                    sound(rift, 1f, 1.2f, "ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH");
                    logResult("HOÀN THÀNH"); onFinish.run(); return;
                }
                if (e >= timeout) {
                    cancel(); maelstrom.cancel(); mobs.forEach(m -> { if (m.isValid()) m.remove(); });
                    broadcast("§c🌀 Cổng tự đóng lại..."); logResult("HẾT GIỜ"); onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
