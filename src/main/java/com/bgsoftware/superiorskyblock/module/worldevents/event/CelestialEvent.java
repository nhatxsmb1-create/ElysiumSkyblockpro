package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.*;

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

        // Spawn 5 crystal markers in a ring
        List<ArmorStand> crystals = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            double a = i * (2 * Math.PI / 5);
            Location loc = center.clone().add(Math.cos(a) * 15, 10 + rng.nextInt(6), Math.sin(a) * 15);
            ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setCustomName("§b✦ Pha Lê Thiên Thể");
            stand.setCustomNameVisible(true);
            stand.setGravity(false);
            stand.setVisible(false);
            crystals.add(stand);
        }

        // Background star-field ambient effect
        BukkitRunnable stars = new BukkitRunnable() {
            int e = 0;
            int starCooldown = 0;

            @Override
            public void run() {
                e += 4;
                if (e > DURATION) { cancel(); return; }

                // Ambient sparkles across the whole island sky
                for (int i = 0; i < 5; i++) {
                    Location p = center.clone().add(
                            (rng.nextDouble() - .5) * 60,
                            8 + rng.nextDouble() * 20,
                            (rng.nextDouble() - .5) * 60);
                    fx(p, 1, "FIREWORKS_SPARK");
                    fx(p, 1, "WITCH_MAGIC", "SPELL_WITCH");
                }

                // Periodically launch a real shooting star with warning
                starCooldown -= 4;
                if (starCooldown <= 0) {
                    starCooldown = 40 + rng.nextInt(80); // every 2-6 seconds
                    Location target = center.clone().add(
                            (rng.nextDouble() - 0.5) * 50, 0,
                            (rng.nextDouble() - 0.5) * 50);
                    target.setY(target.getWorld().getHighestBlockYAt(target));
                    launchStar(target);
                }
            }
        };
        stars.runTaskTimer(plugin, 0L, 4L);

        Ghast beast = (Ghast) world.spawnEntity(center.clone().add(0, 20, 0), EntityType.GHAST);
        beast.setCustomName("§d✦ Ác Thú Sao");
        beast.setCustomNameVisible(true);
        double hp = scaledHP(150.0);
        beast.setMaxHealth(hp);
        beast.setHealth(hp);
        sound(center, 1f, 0.5f, "GHAST_MOAN", "ENTITY_GHAST_AMBIENT");
        trackHPBar(beast, "§d✦ Ác Thú Sao");

        new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 20;
                if (!beast.isValid()) {
                    cancel(); stars.cancel();
                    crystals.forEach(c -> { if (c.isValid()) c.remove(); });
                    Location d = beast.getLocation();
                    world.dropItemNaturally(d, named(Material.GHAST_TEAR, "§d§lMảnh Tinh Tú"));
                    world.dropItemNaturally(d, named(Material.GLOWSTONE_DUST, "§e§lBụi Thiên Thể"));
                    if (hasLootBonus()) {
                        world.dropItemNaturally(d, named(Material.NETHER_STAR, "§b§lLõi Thiên Hà"));
                        broadcast("§d✦ §lPhần thưởng đặc biệt! §r§dLõi Thiên Hà đã rơi!");
                    }
                    broadcast("§a✦ Ác Thú Sao đã bị đánh bại! Phần thưởng thiên thể đã rơi xuống!");
                    sound(center, 1f, 1.4f, "ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH");
                    logResult("HOÀN THÀNH"); onFinish.run(); return;
                }
                if (e >= DURATION) {
                    cancel(); stars.cancel(); beast.remove();
                    crystals.forEach(c -> { if (c.isValid()) c.remove(); });
                    broadcast("§c✦ Sự Kiện Thiên Thể đã tan biến..."); logResult("HẾT GIỜ"); onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /** Show a glowing ring warning, then drop a FallingBlock shooting star */
    private void launchStar(Location ground) {
        // Phase 1: glowing ring warning (1.5 seconds)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks += 3;
                if (ticks >= 30) {
                    cancel();
                    dropStarBlock(ground);
                    return;
                }
                double radius = 2.5;
                for (int i = 0; i < 12; i++) {
                    double angle = (i * (Math.PI * 2 / 12)) + ticks * 0.15;
                    Location p = ground.clone().add(Math.cos(angle) * radius, 0.15, Math.sin(angle) * radius);
                    fx(p, 1, "FIREWORKS_SPARK");
                    fx(p, 1, "WITCH_MAGIC", "SPELL_WITCH");
                }
                sound(ground, 0.4f, 0.5f + (ticks / 30.0f), "NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING");
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    /** Drop a real FallingBlock shooting star from Y+35 with particle trail */
    private void dropStarBlock(Location ground) {
        Location spawnLoc = ground.clone().add(15, 35, 15);
        FallingBlock star;
        try {
            star = ground.getWorld().spawnFallingBlock(spawnLoc, Material.valueOf("GLOWSTONE"), (byte) 0);
        } catch (Exception ex) {
            star = ground.getWorld().spawnFallingBlock(spawnLoc, Material.SAND, (byte) 0);
        }
        star.setDropItem(false);
        star.setVelocity(ground.toVector().subtract(spawnLoc.toVector()).normalize().multiply(1.8));

        final FallingBlock finalStar = star;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!finalStar.isValid() || finalStar.isOnGround()
                        || finalStar.getLocation().getY() <= ground.getY()) {
                    cancel();
                    finalStar.remove();
                    // Impact burst
                    sound(ground, 0.9f, 1.3f, "FIREWORK_BLAST", "ENTITY_FIREWORK_ROCKET_BLAST");
                    for (int i = 0; i < 3; i++) {
                        fx(ground.clone().add((rng.nextDouble() - 0.5) * 2, rng.nextDouble(), (rng.nextDouble() - 0.5) * 2),
                                5, "FIREWORKS_SPARK");
                    }
                    fx(ground, 8, "SPELL_INSTANT", "INSTANT_SPELL");
                    // Drop a little star dust
                    ground.getWorld().dropItemNaturally(ground.clone().add(0, 0.5, 0),
                            named(Material.GLOWSTONE_DUST, "§e✦ Bụi Sao"));
                    return;
                }
                Location loc = finalStar.getLocation();
                fx(loc, 2, "FIREWORKS_SPARK");
                fx(loc, 1, "WITCH_MAGIC", "SPELL_WITCH");
                fx(loc, 1, "PORTAL");
                // Damage players in the path
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
