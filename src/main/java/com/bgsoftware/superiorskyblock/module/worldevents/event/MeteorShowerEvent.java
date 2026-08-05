package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MeteorShowerEvent extends IslandWorldEvent {
    private static final int COUNT = 5, RADIUS = 45, PICKUP_S = 15;

    public MeteorShowerEvent(Island island, Location center) {
        super(island, center, WorldEventType.METEOR_SHOWER);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§e☄ §f§lMưa Thiên Thạch §fđang đến! Hãy quan sát bầu trời và né tránh điểm rơi!");
        countdown("§eThiên thạch chuẩn bị rơi...", () -> shower(onFinish));
    }

    private void shower(Runnable onFinish) {
        World world = center.getWorld();
        new BukkitRunnable() {
            int n = 0;
            @Override
            public void run() {
                if (n >= COUNT) {
                    cancel();
                    broadcast("§e☄ Mưa Thiên Thạch đã kết thúc.");
                    logResult("HOÀN THÀNH");
                    onFinish.run();
                    return;
                }
                n++;
                // Target a location near a random player (height snapped safely inside getPlayerNearbySpawn)
                Location impact = getPlayerNearbySpawn(RADIUS).clone();
                dropMeteor(world, impact);
            }
        }.runTaskTimer(plugin, 40L, 20 * 18L);
    }

    private void dropMeteor(World world, Location ground) {
        broadcast("§c☄ §eThiên thạch sắp đánh trúng gần bạn! §cHÃY CHẠY!");

        // Phase 1: Glowing target marker for 3 seconds (60 ticks)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks += 4;
                if (ticks >= 60 || !ground.getChunk().isLoaded()) {
                    cancel();
                    spawnPhysicalMeteor(world, ground);
                    return;
                }

                float speed = 0.4f + (ticks / 60.0f) * 0.6f;
                sound(ground, 0.7f, speed, "NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING");

                double radius = 3.5;
                // Outer spinning ring
                for (int i = 0; i < 20; i++) {
                    double angle = (i * (Math.PI * 2.0 / 20)) + (ticks * 0.12);
                    Location ring = ground.clone().add(Math.cos(angle) * radius, 0.15, Math.sin(angle) * radius);
                    particle(ring, 1, "FLAME");
                }
                // Inner cross marker
                for (int r = 1; r <= 3; r++) {
                    particle(ground.clone().add(r, 0.15, 0), 1, "FLAME");
                    particle(ground.clone().add(-r, 0.15, 0), 1, "FLAME");
                    particle(ground.clone().add(0, 0.15, r), 1, "FLAME");
                    particle(ground.clone().add(0, 0.15, -r), 1, "FLAME");
                }
                // Lava drips if close to landing
                if (ticks > 30) {
                    particle(ground.clone().add(0, 0.3, 0), 3, "LAVA");
                }
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private void spawnPhysicalMeteor(World world, Location ground) {
        // Drop from directly above target ground location
        Location spawnLoc = ground.clone().add(0, 45, 0);

        FallingBlock meteor = null;
        try {
            meteor = world.spawnFallingBlock(spawnLoc, Material.valueOf("MAGMA"), (byte) 0);
        } catch (Exception e1) {
            try {
                meteor = world.spawnFallingBlock(spawnLoc, Material.valueOf("MAGMA_BLOCK"), (byte) 0);
            } catch (Exception e2) {
                try {
                    meteor = world.spawnFallingBlock(spawnLoc, Material.valueOf("NETHERRACK"), (byte) 0);
                } catch (Exception e3) {
                    try {
                        meteor = world.spawnFallingBlock(spawnLoc, Material.valueOf("RED_SANDSTONE"), (byte) 0);
                    } catch (Exception e4) {
                        // ignore
                    }
                }
            }
        }

        if (meteor == null) {
            // Safety fallback: run impact effect after flight time
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> impact(world, ground), 20L);
            return;
        }

        meteor.setDropItem(false);
        meteor.setVelocity(new Vector((rng.nextDouble() - 0.5) * 0.1, -2.5, (rng.nextDouble() - 0.5) * 0.1));

        sound(spawnLoc, 1f, 0.5f, "FIREWORK_LAUNCH", "ENTITY_FIREWORK_ROCKET_LAUNCH");

        final FallingBlock finalMeteor = meteor;
        new BukkitRunnable() {
            int life = 0;
            @Override
            public void run() {
                life++;
                boolean landed = !finalMeteor.isValid()
                        || finalMeteor.isOnGround()
                        || finalMeteor.getLocation().getY() <= ground.getY() + 0.5;

                if (landed) {
                    cancel();
                    finalMeteor.remove();
                    impact(world, ground);
                    return;
                }

                Location loc = finalMeteor.getLocation();

                // Fire + smoke trail
                particle(loc, 3, "FLAME");
                particle(loc, 2, "SMOKE_LARGE", "LARGE_SMOKE", "SMOKE");
                if (life % 3 == 0) {
                    particle(loc, 2, "LAVA");
                }

                // Whistle sound as it falls
                if (life % 5 == 0) {
                    sound(loc, 0.6f, 0.5f + (life * 0.02f), "GHAST_FIREBALL", "ENTITY_GHAST_SHOOT");
                }

                // Damage players it passes through
                for (Player p : getOnlinePlayers()) {
                    if (p.getWorld().equals(loc.getWorld()) && p.getLocation().distance(loc) < 2.0) {
                        p.damage(5.0);
                        p.setFireTicks(60);
                        p.sendMessage("§c☄ Thiên thạch đâm trúng bạn!");
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void impact(World world, Location loc) {
        sound(loc, 1f, 0.7f, "EXPLODE", "ENTITY_GENERIC_EXPLODE");
        lightningEffect(loc);

        // Explosion visual
        particle(loc, 8, "EXPLOSION_LARGE", "EXPLODE");

        // Fire scatter
        for (int i = 0; i < 24; i++) {
            double ang = rng.nextDouble() * Math.PI * 2;
            double r   = rng.nextDouble() * 3;
            Location p = loc.clone().add(Math.cos(ang) * r, rng.nextDouble() * 2, Math.sin(ang) * r);
            particle(p, 2, "FLAME");
        }

        // Lava spread ring
        for (int i = 0; i < 12; i++) {
            double ang = i * (Math.PI * 2 / 12);
            Location ring = loc.clone().add(Math.cos(ang) * 2, 0.1, Math.sin(ang) * 2);
            particle(ring, 2, "LAVA");
        }

        broadcast("§6☄ Thiên thạch đã rơi tại §e(" + loc.getBlockX() + ", " + loc.getBlockZ()
                + ")§6! Đến nhặt trong §c" + PICKUP_S + " giây§6!");

        Material[] opts = {Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT};
        Material mat = opts[rng.nextInt(opts.length)];
        int qty = hasLootBonus() ? 3 + rng.nextInt(3) : 1 + rng.nextInt(3);
        Item lootItem = world.dropItem(loc.clone().add(0, 1, 0),
                named(mat, "§6§lQuặng Thiên Thạch (" + mat.name() + ")", qty));
        lootItem.setPickupDelay(0);

        // Chance to spawn a crater guardian
        if (rng.nextInt(100) < 25) {
            Zombie mini = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
            mini.setCustomName("§6Golem Thiên Thạch");
            mini.setCustomNameVisible(true);
            double hpVal = scaledHP(80.0);
            mini.setMaxHealth(hpVal);
            mini.setHealth(hpVal);
            mini.setFireTicks(Integer.MAX_VALUE);
            targetNearestPlayer(mini);
            trackHPBar(mini, "§6Golem Thiên Thạch");
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (lootItem.isValid()) {
                lootItem.remove();
                broadcast("§c☄ Chiến lợi phẩm thiên thạch đã tan biến...");
            }
        }, PICKUP_S * 20L);
    }
}
