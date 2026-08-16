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
    private static final int DURATION = 20 * 60 * 4;

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

        // Spawn boss near a player
        Location bossSpawn = getPlayerNearbySpawn(12);

        // Continuous ash rain across island
        BukkitRunnable ash = new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 3;
                if (e >= DURATION) { cancel(); return; }
                for (int i = 0; i < 10; i++) {
                    Location p = center.clone().add(
                            (rng.nextDouble() - .5) * 60,
                            20 + rng.nextInt(6),
                            (rng.nextDouble() - .5) * 60);
                    fx(p, 1, "SMOKE");
                    particle(p, 1, "SMOKE_LARGE");
                    if (rng.nextBoolean()) fx(p, 1, "FLAME");
                }
            }
        };
        ash.runTaskTimer(plugin, 0L, 3L);

        // Lava geyser eruptions every 4 seconds near players
        BukkitRunnable geysers = new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 80;
                if (e >= DURATION) { cancel(); return; }
                Location geyserLoc = getPlayerNearbySpawn(25);
                geyserLoc.setY(world.getHighestBlockYAt(geyserLoc));
                launchLavaGeyser(geyserLoc);
            }
        };
        geysers.runTaskTimer(plugin, 60L, 80L);

        Blaze boss = (Blaze) world.spawnEntity(bossSpawn, EntityType.BLAZE);
        boss.setCustomName("§c🌋 Golem Lửa");
        boss.setCustomNameVisible(true);
        double hp = scaledHP(180.0);
        boss.setMaxHealth(hp);
        boss.setHealth(hp);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        targetNearestPlayer(boss);
        trackHPBar(boss, "§c🌋 Golem Lửa");

        // Heat aura + flame ring around boss
        BukkitRunnable heatAura = new BukkitRunnable() {
            int e = 0;
            double auraAngle = 0;
            @Override
            public void run() {
                e += 10;
                if (e >= DURATION || !boss.isValid()) { cancel(); return; }
                auraAngle += 20;
                Location bossLoc = boss.getLocation();

                // Rotating fire ring (FLAME — most visible effect)
                for (int i = 0; i < 10; i++) {
                    double ang = Math.toRadians(auraAngle + i * 36);
                    Location fp = bossLoc.clone().add(Math.cos(ang) * 3.5, 1, Math.sin(ang) * 3.5);
                    fx(fp, 1, "FLAME");
                    particle(fp, 1, "FLAME");
                    if (i % 2 == 0) {
                        particle(fp, 1, "SMOKE_LARGE");
                    }
                }

                // Damage players in burning aura
                if (e % 40 == 0) {
                    for (Player p : getOnlinePlayers()) {
                        if (!p.getWorld().equals(bossLoc.getWorld())) continue;
                        double dist = p.getLocation().distance(bossLoc);
                        if (dist < 8.0) {
                            p.sendMessage("§c§l🌋 Hơi nóng thiêu đốt cực mạnh!");
                            p.setFireTicks(80);
                            p.damage(2.5);
                            fx(p.getLocation(), 4, "FLAME");
                        }
                    }
                }

                targetNearestPlayer(boss);
            }
        };
        heatAura.runTaskTimer(plugin, 0L, 10L);

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
                    if (hasLootBonus()) {
                        world.dropItemNaturally(d, named(Material.NETHER_STAR, "§c§lBảo Ngọc Địa Ngục"));
                        broadcast("§6🌋 §lPhần thưởng đặc biệt! §r§6Bảo Ngọc Địa Ngục đã rơi!");
                    }
                    broadcast("§a🌋 Golem Lửa đã bị tiêu diệt!");
                    dropTrophy(d, "volcano");
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

    private void launchLavaGeyser(Location ground) {
        broadcast("§c🌋 §eDung nham phun lên gần bạn! §cCHẠY NGAY!");

        // Warning ring (FLAME)
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks += 4;
                if (ticks >= 40) {
                    cancel();
                    shootGeyser(ground);
                    return;
                }
                for (int i = 0; i < 16; i++) {
                    double ang = (i * (Math.PI * 2 / 16)) + ticks * 0.15;
                    Location p = ground.clone().add(Math.cos(ang) * 2.5, 0.2, Math.sin(ang) * 2.5);
                    fx(p, 1, "FLAME");
                    fx(p, 1, "LAVA");
                    particle(p, 1, "FLAME");
                }
                sound(ground, 0.5f, 0.3f + (ticks / 40.0f * 0.5f), "FIZZ", "BLOCK_FIRE_EXTINGUISH");
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private void shootGeyser(Location ground) {
        FallingBlock lava;
        try {
            lava = ground.getWorld().spawnFallingBlock(ground.clone().add(0, 1, 0), Material.valueOf("MAGMA"), (byte) 0);
        } catch (Exception ex) {
            lava = ground.getWorld().spawnFallingBlock(ground.clone().add(0, 1, 0), Material.GRAVEL, (byte) 0);
        }
        lava.setDropItem(false);
        lava.setMetadata("worldevent_geyser", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        lava.setVelocity(new Vector(
                (rng.nextDouble() - 0.5) * 0.4,
                1.5 + rng.nextDouble() * 0.4,
                (rng.nextDouble() - 0.5) * 0.4));

        final FallingBlock block = lava;
        new BukkitRunnable() {
            int life = 0;
            @Override
            public void run() {
                life += 2;
                if (!block.isValid() || block.isOnGround() || life > 80) {
                    cancel(); block.remove();
                    Location land = block.getLocation();
                    sound(land, 1f, 0.7f, "EXPLODE", "ENTITY_GENERIC_EXPLODE");
                    fx(land, 8, "LAVA");
                    fx(land, 6, "FLAME");
                    particle(land, 10, "LAVA");
                    particle(land, 6, "FLAME");
                    for (Player p : getOnlinePlayers()) {
                        if (p.getWorld().equals(land.getWorld()) && p.getLocation().distance(land) < 3.0) {
                            p.damage(7.0);
                            p.setFireTicks(100);
                            p.setVelocity(new Vector(0, 0.6, 0));
                            p.sendMessage("§c🌋 Dung nham phun lên thiêu đốt bạn!");
                        }
                    }
                    return;
                }
                Location loc = block.getLocation();
                fx(loc, 3, "FLAME");
                fx(loc, 2, "LAVA");
                particle(loc, 3, "FLAME");
                particle(loc, 2, "LAVA");
                particle(loc, 1, "SMOKE_LARGE");
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
