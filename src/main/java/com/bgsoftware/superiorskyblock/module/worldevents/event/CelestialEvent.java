package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CelestialEvent extends IslandWorldEvent {
    private static final int DURATION = 20 * 60 * 4;

    public CelestialEvent(Island island, Location center) {
        super(island, center, WorldEventType.CELESTIAL);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§d✦ §f§lSự Kiện Thiên Thể §fđã bắt đầu! Bầu trời lấp lánh ánh sao huyền ảo!");
        countdown("§dÁc Thú Sao đang hạ xuống...", () -> spawnBoss(onFinish));
    }

    private void spawnBoss(Runnable onFinish) {
        World world = center.getWorld();

        // Crystal pillars around center
        for (int i = 0; i < 5; i++) {
            double a = i * (2 * Math.PI / 5);
            Location loc = center.clone().add(Math.cos(a) * 14, 10 + rng.nextInt(5), Math.sin(a) * 14);
            ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setCustomName("§b✦ Pha Lê Thiên Thể");
            stand.setCustomNameVisible(true);
            stand.setGravity(false);
            stand.setVisible(false);
        }

        // Ambient star field effect (PORTAL + WITCH particles — always visible)
        BukkitRunnable stars = new BukkitRunnable() {
            int e = 0;
            int nextStarIn = 20;
            double angle = 0;

            @Override
            public void run() {
                e += 3;
                if (e > DURATION) { cancel(); return; }
                angle += 12;

                // Swirling star ring in the sky
                for (int i = 0; i < 8; i++) {
                    double ang = Math.toRadians(angle + i * 45);
                    double r = 6 + Math.sin(e * 0.05) * 3;
                    Location sky = center.clone().add(Math.cos(ang) * r, 22 + Math.sin(e * 0.08) * 4, Math.sin(ang) * r);
                    fx(sky, 1, "PORTAL");
                    particle(sky, 1, "PORTAL");
                    if (i % 2 == 0) {
                        particle(sky, 1, "FIREWORKS_SPARK");
                        fx(sky, 1, "FIREWORKS_SPARK");
                    }
                }

                // Random sparkles across island
                for (int i = 0; i < 3; i++) {
                    Location sp = center.clone().add(
                            (rng.nextDouble() - .5) * 50,
                            6 + rng.nextDouble() * 18,
                            (rng.nextDouble() - .5) * 50);
                    fx(sp, 1, "FIREWORKS_SPARK");
                }

                // Launch shooting star periodically
                nextStarIn -= 3;
                if (nextStarIn <= 0) {
                    nextStarIn = 35 + rng.nextInt(65);
                    // Target a random player
                    Location target = getPlayerNearbySpawn(35).clone();
                    target.setY(target.getWorld().getHighestBlockYAt(target));
                    launchShootingStar(target);
                }
            }
        };
        stars.runTaskTimer(plugin, 0L, 3L);

        // Boss — Ghast floating above island
        Location bossLoc = center.clone().add(0, 22, 0);
        Ghast beast = (Ghast) world.spawnEntity(bossLoc, EntityType.GHAST);
        beast.setCustomName("§d✦ Ác Thú Sao");
        beast.setCustomNameVisible(true);
        double hp = scaledHP(150.0);
        beast.setMaxHealth(hp);
        beast.setHealth(hp);
        sound(center, 1f, 0.5f, "GHAST_MOAN", "ENTITY_GHAST_AMBIENT");
        targetNearestPlayer(beast);
        trackHPBar(beast, "§d✦ Ác Thú Sao");

        new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 20;
                if (!beast.isValid()) {
                    cancel(); stars.cancel();
                    Location d = beast.getLocation();
                    world.dropItemNaturally(d, named(Material.GHAST_TEAR, "§d§lMảnh Tinh Tú"));
                    world.dropItemNaturally(d, named(Material.GLOWSTONE_DUST, "§e§lBụi Thiên Thể"));
                    if (hasLootBonus()) {
                        world.dropItemNaturally(d, named(Material.NETHER_STAR, "§b§lLõi Thiên Hà"));
                        broadcast("§d✦ §lPhần thưởng đặc biệt! §r§dLõi Thiên Hà đã rơi!");
                    }
                    broadcast("§a✦ Ác Thú Sao đã bị đánh bại!");
                    sound(center, 1f, 1.4f, "ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH");
                    logResult("HOÀN THÀNH"); onFinish.run(); return;
                }
                if (e >= DURATION) {
                    cancel(); stars.cancel(); beast.remove();
                    broadcast("§c✦ Sự Kiện Thiên Thể đã tan biến..."); logResult("HẾT GIỜ"); onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /** Spinning PORTAL+FIREWORK ring marker then FallingBlock shooting star */
    private void launchShootingStar(Location ground) {
        // Phase 1: ground marker ring (1.5s)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks += 4;
                if (ticks >= 30) {
                    cancel();
                    dropStarBlock(ground);
                    return;
                }
                for (int i = 0; i < 14; i++) {
                    double ang = (i * (Math.PI * 2 / 14)) + ticks * 0.18;
                    Location p = ground.clone().add(Math.cos(ang) * 2.8, 0.15, Math.sin(ang) * 2.8);
                    fx(p, 1, "PORTAL");
                    fx(p, 1, "FIREWORKS_SPARK");
                    particle(p, 1, "PORTAL");
                }
                sound(ground, 0.5f, 0.5f + (ticks / 30.0f * 0.7f), "NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING");
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private void dropStarBlock(Location ground) {
        // Drop from high above, straight down
        Location spawnLoc = ground.clone().add(0, 42, 0);
        FallingBlock star;
        try {
            star = ground.getWorld().spawnFallingBlock(spawnLoc, Material.valueOf("GLOWSTONE"), (byte) 0);
        } catch (Exception ex) {
            star = ground.getWorld().spawnFallingBlock(spawnLoc, Material.SAND, (byte) 0);
        }
        star.setDropItem(false);
        star.setVelocity(new Vector(
                (rng.nextDouble() - 0.5) * 0.05, -2.2, (rng.nextDouble() - 0.5) * 0.05));

        final FallingBlock finalStar = star;
        new BukkitRunnable() {
            int life = 0;
            @Override
            public void run() {
                life++;
                if (!finalStar.isValid() || finalStar.isOnGround()
                        || finalStar.getLocation().getY() <= ground.getY() + 0.5) {
                    cancel();
                    finalStar.remove();

                    // Impact burst
                    sound(ground, 0.9f, 1.3f, "FIREWORK_BLAST", "ENTITY_FIREWORK_ROCKET_BLAST");
                    for (int i = 0; i < 20; i++) {
                        double ang = rng.nextDouble() * Math.PI * 2;
                        double r   = rng.nextDouble() * 2.5;
                        Location p = ground.clone().add(Math.cos(ang) * r, rng.nextDouble(), Math.sin(ang) * r);
                        fx(p, 2, "FIREWORKS_SPARK");
                        particle(p, 2, "FIREWORKS_SPARK");
                    }
                    fx(ground, 3, "PORTAL");
                    particle(ground, 8, "PORTAL");
                    ground.getWorld().dropItemNaturally(ground.clone().add(0, 0.5, 0),
                            named(Material.GLOWSTONE_DUST, "§e✦ Bụi Sao"));
                    return;
                }

                Location loc = finalStar.getLocation();
                // Star trail: PORTAL + FIREWORK (very visible magic trail)
                fx(loc, 3, "FIREWORKS_SPARK");
                fx(loc, 2, "PORTAL");
                particle(loc, 3, "FIREWORKS_SPARK");
                particle(loc, 2, "PORTAL");

                // Hit players in path
                for (Player p : getOnlinePlayers()) {
                    if (p.getWorld().equals(loc.getWorld()) && p.getLocation().distance(loc) < 1.8) {
                        p.damage(4.0);
                        p.sendMessage("§d✦ Sao băng đâm trúng bạn!");
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
