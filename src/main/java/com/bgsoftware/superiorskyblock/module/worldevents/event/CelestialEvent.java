package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

        // Spawn Boss near a player on the ground, then float it up
        Location spawnBase = getPlayerNearbySpawn(15);
        Location bossLoc = spawnBase.clone().add(0, 18, 0);

        Ghast beast = (Ghast) world.spawnEntity(bossLoc, EntityType.GHAST);
        beast.setCustomName("§d✦ Ác Thú Sao");
        beast.setCustomNameVisible(true);
        double hp = scaledHP(150.0);
        beast.setMaxHealth(hp);
        beast.setHealth(hp);
        sound(bossLoc, 1.0f, 0.5f, "GHAST_MOAN", "ENTITY_GHAST_AMBIENT");
        targetNearestPlayer(beast);
        trackHPBar(beast, "§d✦ Ác Thú Sao");

        // Spawn 4 visible Ender Crystals (changed from 5 to 4 per user request)
        List<Entity> crystals = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            double a = i * (2 * Math.PI / 4);
            Location loc = spawnBase.clone().add(Math.cos(a) * 12, 4 + rng.nextInt(3), Math.sin(a) * 12);
            loc.setY(world.getHighestBlockYAt(loc) + 4);

            Entity crystal = null;
            try {
                crystal = world.spawnEntity(loc, EntityType.valueOf("ENDER_CRYSTAL"));
            } catch (Exception ex) {
                try {
                    crystal = world.spawnEntity(loc, EntityType.valueOf("END_CRYSTAL"));
                } catch (Exception ex2) {
                    ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
                    stand.setCustomName("§b✦ Pha Lê Thiên Thể");
                    stand.setCustomNameVisible(true);
                    stand.setGravity(false);
                    try {
                        stand.setHelmet(new org.bukkit.inventory.ItemStack(Material.valueOf("GLOWSTONE")));
                    } catch (Exception ignored) {}
                    crystal = stand;
                }
            }
            if (crystal != null) {
                // Mark with metadata so it doesn't break blocks on explosion if hit
                crystal.setMetadata("worldevent_star", new FixedMetadataValue(plugin, true));
                crystals.add(crystal);
                lightningEffect(loc);
            }
        }

        final double[] lastHP = {beast.getHealth()};

        // Ambient star field effect + Crystal beam connection + Boss shield mechanic
        BukkitRunnable stars = new BukkitRunnable() {
            int e = 0;
            int nextStarIn = 20;
            double angle = 0;

            @Override
            public void run() {
                e += 3;
                if (e > DURATION || !beast.isValid()) { cancel(); return; }
                angle += 12;

                // 1. Invulnerability Shield: Ghast is immune while crystals exist
                boolean crystalsAlive = crystals.stream().anyMatch(Entity::isValid);
                if (crystalsAlive) {
                    if (beast.getHealth() < lastHP[0]) {
                        beast.setHealth(lastHP[0]);
                        sound(beast.getLocation(), 0.9f, 1.4f, "ITEM_SHIELD_BLOCK", "ENTITY_SHIELD_BLOCK", "WITHER_SHOOT");
                        particle(beast.getLocation().add(0, 1.5, 0), 12, "WITCH", "WITCH_MAGIC", "SPELL_WITCH");
                        broadcastBar("§c§l✦ Ác Thú Sao đang được bảo vệ bởi các Pha Lê Thiên Thể!");
                    }
                } else {
                    lastHP[0] = beast.getHealth();
                }

                // 2. Swirling star ring in the sky
                for (int i = 0; i < 8; i++) {
                    double ang = Math.toRadians(angle + i * 45);
                    double r = 6 + Math.sin(e * 0.05) * 3;
                    Location sky = beast.getLocation().clone().add(Math.cos(ang) * r, 2.0 + Math.sin(e * 0.08) * 1.5, Math.sin(ang) * r);
                    particle(sky, 1, "PORTAL");
                    if (i % 2 == 0) {
                        particle(sky, 1, "FIREWORK", "FIREWORKS_SPARK");
                    }
                }

                // 3. Connect Crystal beams to Boss
                for (Entity c : crystals) {
                    if (c.isValid()) {
                        particle(c.getLocation().add(0, 0.5, 0), 1, "WITCH", "WITCH_MAGIC", "SPELL_WITCH");
                        try {
                            Method setBeamTarget = c.getClass().getMethod("setBeamTarget", Location.class);
                            setBeamTarget.invoke(c, beast.getLocation().add(0, 1.5, 0));
                        } catch (Exception ignored) {}
                    }
                }

                // 4. Launch shooting star periodically
                nextStarIn -= 3;
                if (nextStarIn <= 0) {
                    nextStarIn = 35 + rng.nextInt(65);
                    Location target = getPlayerNearbySpawn(35).clone();
                    launchShootingStar(target);
                }
            }
        };
        stars.runTaskTimer(plugin, 0L, 3L);

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
                    broadcast("§a✦ Ác Thú Sao đã bị đánh bại!");
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

    private void launchShootingStar(Location ground) {
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
                    particle(p, 1, "PORTAL");
                    particle(p, 1, "FIREWORK", "FIREWORKS_SPARK");
                }
                sound(ground, 0.5f, 0.5f + (ticks / 30.0f * 0.7f), "NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING");
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private void dropStarBlock(Location ground) {
        Location spawnLoc = ground.clone().add(0, 42, 0);
        FallingBlock star = null;
        try {
            star = ground.getWorld().spawnFallingBlock(spawnLoc, Material.valueOf("GLOWSTONE"), (byte) 0);
        } catch (Exception ex) {
            try {
                star = ground.getWorld().spawnFallingBlock(spawnLoc, Material.valueOf("SEA_LANTERN"), (byte) 0);
            } catch (Exception ex2) {
                // Ignore
            }
        }

        if (star == null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                sound(ground, 0.9f, 1.3f, "FIREWORK_BLAST", "ENTITY_FIREWORK_ROCKET_BLAST");
                particle(ground, 15, "FIREWORK", "FIREWORKS_SPARK");
                particle(ground, 8, "PORTAL");
            }, 20L);
            return;
        }

        star.setDropItem(false);
        // Mark star with metadata so it doesn't solidify or break blocks
        star.setMetadata("worldevent_star", new FixedMetadataValue(plugin, true));
        star.setVelocity(new Vector((rng.nextDouble() - 0.5) * 0.05, -2.2, (rng.nextDouble() - 0.5) * 0.05));

        final FallingBlock finalStar = star;
        new BukkitRunnable() {
            int life = 0;
            @Override
            public void run() {
                life++;
                boolean landed = !finalStar.isValid() || finalStar.isOnGround()
                        || finalStar.getLocation().getY() <= ground.getY() + 0.5;

                if (landed) {
                    cancel();
                    finalStar.remove();

                    sound(ground, 0.9f, 1.3f, "FIREWORK_BLAST", "ENTITY_FIREWORK_ROCKET_BLAST");
                    for (int i = 0; i < 20; i++) {
                        double ang = rng.nextDouble() * Math.PI * 2;
                        double r   = rng.nextDouble() * 2.5;
                        Location p = ground.clone().add(Math.cos(ang) * r, rng.nextDouble(), Math.sin(ang) * r);
                        particle(p, 2, "FIREWORK", "FIREWORKS_SPARK");
                    }
                    particle(ground, 8, "PORTAL");
                    ground.getWorld().dropItemNaturally(ground.clone().add(0, 0.5, 0),
                            named(Material.GLOWSTONE_DUST, "§e✦ Bụi Sao"));
                    return;
                }

                Location loc = finalStar.getLocation();
                particle(loc, 3, "FIREWORK", "FIREWORKS_SPARK");
                particle(loc, 2, "PORTAL");

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
