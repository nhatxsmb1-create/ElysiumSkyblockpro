package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class VolcanoEvent extends IslandWorldEvent {
    private static final int DURATION = 20 * 60 * 4, RADIUS = 60;

    public VolcanoEvent(Island island, Location center) {
        super(island, center, WorldEventType.VOLCANO);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§c🌋 §f§lNúi Lửa §fbùng nổ! Tro bụi bắt đầu rơi xuống đảo!");
        broadcast("§7Bầu trời chuyển đỏ... Golem Lửa đang thức dậy!");
        countdown("§cNúi lửa đang phun trào...", () -> spawnBoss(onFinish));
    }

    private void spawnBoss(Runnable onFinish) {
        World world = center.getWorld();

        // Ambient ash/smoke across the island
        BukkitRunnable ash = new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 4;
                if (e >= DURATION) { cancel(); return; }
                for (int i = 0; i < 8; i++) {
                    Location p = center.clone().add(
                            (rng.nextDouble() - .5) * RADIUS,
                            18 + rng.nextInt(8),
                            (rng.nextDouble() - .5) * RADIUS);
                    fx(p, 1, "LARGE_SMOKE");
                    fx(p, 1, "FLAME");
                }
            }
        };
        ash.runTaskTimer(plugin, 0L, 4L);

        // Real lava geyser projectiles every 5 seconds
        BukkitRunnable geysers = new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 80;
                if (e >= DURATION) { cancel(); return; }
                // Ground warning then FallingBlock lava geyser
                Location from = center.clone().add(
                        (rng.nextDouble() - .5) * RADIUS * 0.6,
                        0, (rng.nextDouble() - .5) * RADIUS * 0.6);
                from.setY(world.getHighestBlockYAt(from));
                launchLavaGeyser(from);
            }
        };
        geysers.runTaskTimer(plugin, 40L, 80L);

        Blaze boss = (Blaze) world.spawnEntity(center.clone().add(0, 2, 0), EntityType.BLAZE);
        boss.setCustomName("§c🌋 Golem Lửa");
        boss.setCustomNameVisible(true);
        double hp = scaledHP(180.0);
        boss.setMaxHealth(hp);
        boss.setHealth(hp);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        trackHPBar(boss, "§c🌋 Golem Lửa");

        // Burning aura around boss
        BukkitRunnable heatAura = new BukkitRunnable() {
            int e = 0;
            double auraAngle = 0;
            @Override
            public void run() {
                e += 20;
                if (e >= DURATION || !boss.isValid()) { cancel(); return; }
                Location bossLoc = boss.getLocation();

                // Rotating fire ring around boss
                auraAngle += 30;
                for (int i = 0; i < 8; i++) {
                    double ang = Math.toRadians(auraAngle + i * 45);
                    Location fp = bossLoc.clone().add(Math.cos(ang) * 3, 1, Math.sin(ang) * 3);
                    fx(fp, 1, "FLAME");
                    fx(fp, 1, "LARGE_SMOKE");
                }

                for (Player p : getOnlinePlayers()) {
                    if (!p.getWorld().equals(bossLoc.getWorld())) continue;
                    double dist = p.getLocation().distance(bossLoc);
                    // Inner burn zone: < 10 blocks
                    if (dist < 10.0) {
                        p.sendMessage("§c§l🌋 Hơi nóng thiêu đốt cực mạnh!");
                        p.setFireTicks(60);
                        p.damage(2.0);
                        fx(p.getLocation(), 3, "FLAME");
                    }
                    // Outer warning zone: < 20 blocks, random flame jets
                    else if (dist < 20.0 && rng.nextDouble() < 0.3) {
                        p.sendMessage("§c🔥 Mặt đất dưới chân đang nóng lên!");
                        Location pLoc = p.getLocation().clone();
                        // Show 3-tick warning
                        fx(pLoc, 6, "MOBSPAWNER_FLAMES");
                        sound(pLoc, 0.6f, 0.5f, "FIZZ", "BLOCK_FIRE_EXTINGUISH");
                        // Delayed eruption under player's feet
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            if (!boss.isValid() || !p.isOnline()) return;
                            sound(pLoc, 0.9f, 0.8f, "EXPLODE", "ENTITY_GENERIC_EXPLODE");
                            fx(pLoc, 12, "LAVA");
                            fx(pLoc, 6, "LARGE_SMOKE");
                            if (p.getLocation().distance(pLoc) < 2.5) {
                                p.damage(5.0);
                                p.setFireTicks(100);
                                p.setVelocity(new Vector(0, 0.5, 0));
                            }
                        }, 30L);
                    }
                }
            }
        };
        heatAura.runTaskTimer(plugin, 0L, 20L);

        new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 20;
                if (!boss.isValid()) {
                    cancel(); ash.cancel(); geysers.cancel(); heatAura.cancel();
                    Location d = boss.getLocation();
                    world.dropItemNaturally(d, named(Material.MAGMA_CREAM, "§c§lTinh Thể Dung Nham"));
                    world.dropItemNaturally(d, named(Material.BLAZE_ROD, "§6§lLõi Nham Thạch"));
                    world.dropItemNaturally(d, named(Material.NETHERRACK, "§4§lQuặng Núi Lửa"));
                    if (hasLootBonus()) {
                        world.dropItemNaturally(d, named(Material.NETHER_STAR, "§c§lBảo Ngọc Địa Ngục"));
                        broadcast("§6🌋 §lPhần thưởng đặc biệt! §r§6Bảo Ngọc Địa Ngục đã rơi!");
                    }
                    broadcast("§a🌋 Golem Lửa đã bị tiêu diệt! Chiến lợi phẩm núi lửa đã rơi!");
                    sound(center, 1f, 0.8f, "ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH");
                    logResult("HOÀN THÀNH"); onFinish.run(); return;
                }
                if (e >= DURATION) {
                    cancel(); ash.cancel(); geysers.cancel(); heatAura.cancel(); boss.remove();
                    broadcast("§c🌋 Núi lửa đã nguội dần..."); logResult("HẾT GIỜ"); onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /** Ground warning ring then shoot a FallingBlock lava geyser upward */
    private void launchLavaGeyser(Location ground) {
        // Warning ring on the ground for 1.5 seconds
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks += 3;
                if (ticks >= 30) {
                    cancel();
                    shootGeyserBlock(ground);
                    return;
                }
                for (int i = 0; i < 10; i++) {
                    double angle = (i * (Math.PI * 2 / 10)) + ticks * 0.2;
                    Location p = ground.clone().add(Math.cos(angle) * 2.0, 0.2, Math.sin(angle) * 2.0);
                    fx(p, 1, "LAVA");
                    fx(p, 1, "FLAME");
                }
                sound(ground, 0.4f, 0.3f + (ticks / 30.0f * 0.5f), "FIZZ", "BLOCK_FIRE_EXTINGUISH");
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    /** Shoot FallingBlock magma upward like a geyser with a molten trail */
    private void shootGeyserBlock(Location ground) {
        FallingBlock lava;
        try {
            lava = ground.getWorld().spawnFallingBlock(ground.clone().add(0, 1, 0), Material.valueOf("MAGMA"), (byte) 0);
        } catch (Exception ex) {
            lava = ground.getWorld().spawnFallingBlock(ground.clone().add(0, 1, 0), Material.GRAVEL, (byte) 0);
        }
        lava.setDropItem(false);
        lava.setVelocity(new Vector(
                (rng.nextDouble() - 0.5) * 0.5,
                1.4 + rng.nextDouble() * 0.5,
                (rng.nextDouble() - 0.5) * 0.5));

        final FallingBlock block = lava;
        new BukkitRunnable() {
            int life = 0;
            @Override
            public void run() {
                life += 2;
                if (!block.isValid() || block.isOnGround() || life > 80) {
                    cancel();
                    block.remove();
                    Location land = block.getLocation();
                    sound(land, 1f, 0.7f, "EXPLODE", "ENTITY_GENERIC_EXPLODE");
                    fx(land, 15, "LAVA");
                    fx(land, 8, "LARGE_SMOKE");
                    for (Player p : getOnlinePlayers()) {
                        if (p.getWorld().equals(land.getWorld()) && p.getLocation().distance(land) < 2.5) {
                            p.damage(6.0);
                            p.setFireTicks(80);
                            p.setVelocity(new Vector(0, 0.4, 0));
                            p.sendMessage("§c🌋 Dung nham phun lên thiêu đốt bạn!");
                        }
                    }
                    return;
                }
                Location loc = block.getLocation();
                fx(loc, 2, "FLAME");
                fx(loc, 1, "LAVA");
                fx(loc, 1, "LARGE_SMOKE");
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
