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

public class InvasionEvent extends IslandWorldEvent {
    private static final int[][] WAVES = {{4, 0}, {4, 1}, {2, 2}, {1, 3}};

    public InvasionEvent(Island island, Location center) {
        super(island, center, WorldEventType.INVASION);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§c👹 §f§lĐảo của bạn đang bị §c§lXÂM LƯỢC§f! Hãy bảo vệ nó!");
        countdown("§cQuân xâm lược đang đến...", () -> spawnWave(0, onFinish));
    }

    private void spawnWave(int idx, Runnable onFinish) {
        World world = center.getWorld();
        if (idx >= WAVES.length) {
            world.dropItemNaturally(center.clone().add(0, 1, 0), named(Material.IRON_INGOT, "§c§lCúp Bảo Vệ Đảo"));
            world.dropItemNaturally(center.clone().add(0, 1, 0), named(Material.GOLD_NUGGET, "§6§lXu Chiến Lợi Phẩm", 5));
            if (hasLootBonus()) {
                world.dropItemNaturally(center.clone().add(0, 1, 0), named(Material.DIAMOND, "§c§lKim Cương Chỉ Huy", 3));
                broadcast("§c👹 §lPhần thưởng đặc biệt! §r§cKim Cương Chỉ Huy đã rơi!");
            }
            broadcast("§a👹 Quân xâm lược đã bị đẩy lui! §cCúp Bảo Vệ Đảo §ađã rơi!");
            sound(center, 1f, 1f, "LEVEL_UP", "ENTITY_PLAYER_LEVELUP");
            logResult("HOÀN THÀNH"); onFinish.run(); return;
        }

        broadcast("§c👹 §fĐợt §e" + (idx + 1) + "§c/§e" + WAVES.length + " §fquân tấn công ào đến!");
        sound(center, 0.7f, 0.8f, "ENDERDRAGON_WINGS", "ENTITY_ENDER_DRAGON_FLAP");

        List<LivingEntity> waveMobs = new ArrayList<>();
        for (int i = 0; i < WAVES[idx][0]; i++) {
            double a = rng.nextDouble() * Math.PI * 2;
            Location loc = center.clone().add(Math.cos(a) * 10, 1, Math.sin(a) * 10);
            loc.setY(world.getHighestBlockYAt(loc) + 1);
            // Spawn flash effect
            fx(loc, 5, "FIREWORKS_SPARK");
            LivingEntity mob = spawnMob(world, loc, WAVES[idx][1], idx);
            if (mob != null) {
                waveMobs.add(mob);
                if (WAVES[idx][1] == 3) trackHPBar(mob, "§4⚔ Tướng Tổng Chỉ Huy");
            }
        }

        // Wave-specific cooldowns
        int[] arrowCooldown = {0};
        int[] slamCooldown = {0};

        new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 20;

                // --- WAVE 1: Zombie Rush — charge toward nearest player ---
                if (idx == 0) {
                    for (LivingEntity m : waveMobs) {
                        if (!m.isValid()) continue;
                        // Ambient growl and smoke
                        if (rng.nextDouble() < 0.1) {
                            sound(m.getLocation(), 0.5f, 0.9f, "MOB_ZOMBIE_HURT", "ENTITY_ZOMBIE_HURT");
                            fx(m.getLocation(), 2, "LARGE_SMOKE");
                        }
                    }
                }

                // --- WAVE 2: Arrow Rain from Skeleton archers ---
                if (idx == 1) {
                    arrowCooldown[0] += 20;
                    boolean skeletonsAlive = waveMobs.stream().anyMatch(m -> m instanceof Skeleton && m.isValid());
                    if (skeletonsAlive && arrowCooldown[0] >= 80) {
                        arrowCooldown[0] = 0;
                        broadcast("§e🏹 Cung thủ bóng tối khai hỏa! Mưa tên từ trên trời!");
                        for (Player p : getOnlinePlayers()) {
                            Location pLoc = p.getLocation();
                            // Warning marker: ring of sparks at player's feet
                            for (int i = 0; i < 10; i++) {
                                double ang = i * (Math.PI * 2 / 10);
                                Location mark = pLoc.clone().add(Math.cos(ang) * 2, 0.1, Math.sin(ang) * 2);
                                fx(mark, 1, "FIREWORKS_SPARK");
                            }
                            sound(pLoc, 0.8f, 0.5f, "SHOOT_ARROW", "ENTITY_ARROW_SHOOT");
                            // Delay arrows by 1 second so player can dodge
                            final Location frozenLoc = pLoc.clone();
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                for (int i = 0; i < 5; i++) {
                                    Location arrowFrom = frozenLoc.clone().add(
                                            (rng.nextDouble() - 0.5) * 6, 18 + rng.nextDouble() * 5,
                                            (rng.nextDouble() - 0.5) * 6);
                                    Arrow arrow = frozenLoc.getWorld().spawnArrow(arrowFrom,
                                            new Vector(0, -1, 0), 1.5f, 8f);
                                    arrow.setShooter(null);
                                    arrow.setFireTicks(0);
                                }
                            }, 20L);
                        }
                    }
                }

                // --- WAVE 3: Elite Warriors — charge and leap at players ---
                if (idx == 2) {
                    for (LivingEntity m : waveMobs) {
                        if (!(m instanceof PigZombie) || !m.isValid()) continue;
                        if (rng.nextDouble() < 0.15) {
                            // Leap toward nearest player
                            Player nearest = getNearestPlayer(m.getLocation());
                            if (nearest != null) {
                                Vector leap = nearest.getLocation().toVector()
                                        .subtract(m.getLocation().toVector()).normalize().multiply(0.8);
                                leap.setY(0.5);
                                m.setVelocity(leap);
                                sound(m.getLocation(), 0.7f, 1.2f, "MOB_ZOMBIE_HURT", "ENTITY_ZOMBIE_HURT");
                                fx(m.getLocation(), 3, "FIREWORKS_SPARK");
                            }
                        }
                    }
                }

                // --- WAVE 4: Iron Golem Commander Ground Slam ---
                if (idx == 3) {
                    slamCooldown[0] += 20;
                    for (LivingEntity m : waveMobs) {
                        if (!(m instanceof IronGolem) || !m.isValid()) continue;
                        Location gLoc = m.getLocation();

                        // Rotating fire aura around boss
                        double aAng = e * 5;
                        for (int i = 0; i < 6; i++) {
                            double ang = Math.toRadians(aAng + i * 60);
                            Location fp = gLoc.clone().add(Math.cos(ang) * 3, 1, Math.sin(ang) * 3);
                            fx(fp, 1, "MOBSPAWNER_FLAMES");
                        }

                        // Ground slam every 5 seconds
                        if (slamCooldown[0] >= 100) {
                            slamCooldown[0] = 0;
                            broadcast("§4§l⚔ Tướng Chỉ Huy đang tụ lực đập đất!");
                            // Wind-up warning ring
                            new BukkitRunnable() {
                                int wt = 0;
                                @Override
                                public void run() {
                                    wt += 4;
                                    if (wt >= 20) {
                                        cancel();
                                        executeSlamExplosion(gLoc);
                                        return;
                                    }
                                    for (int i = 0; i < 12; i++) {
                                        double ang = (i * (Math.PI * 2 / 12)) + wt * 0.3;
                                        Location ring = gLoc.clone().add(Math.cos(ang) * 6, 0.1, Math.sin(ang) * 6);
                                        fx(ring, 1, "MOBSPAWNER_FLAMES");
                                    }
                                    sound(gLoc, 0.6f + (wt / 20.0f) * 0.4f, 0.4f + (wt / 20.0f) * 0.4f,
                                            "BURNING", "BLOCK_FIRE_AMBIENT");
                                }
                            }.runTaskTimer(plugin, 0L, 4L);
                        }
                        break;
                    }
                }

                if (waveMobs.stream().noneMatch(Entity::isValid)) {
                    cancel();
                    broadcast("§a👹 Đợt " + (idx + 1) + " đã bị tiêu diệt!");
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> spawnWave(idx + 1, onFinish), 60L);
                    return;
                }
                if (e >= 20 * 60 * 3) {
                    cancel(); waveMobs.forEach(mm -> { if (mm.isValid()) mm.remove(); });
                    broadcast("§c👹 Đảo đã bị quân xâm lược tràn ngập..."); logResult("THẤT BẠI"); onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /** Slam explosion centered at boss location */
    private void executeSlamExplosion(Location gLoc) {
        sound(gLoc, 1.2f, 0.5f, "EXPLODE", "ENTITY_GENERIC_EXPLODE");
        fx(gLoc, 8, "EXPLOSION_LARGE");
        fx(gLoc, 15, "LARGE_SMOKE");
        for (Player p : getOnlinePlayers()) {
            if (!p.getWorld().equals(gLoc.getWorld())) continue;
            double dist = p.getLocation().distance(gLoc);
            if (dist < 10.0) {
                double dmg = dist < 4.0 ? 8.0 : 4.0;
                p.damage(dmg);
                Vector knock = p.getLocation().toVector().subtract(gLoc.toVector());
                knock.setY(0);
                if (knock.lengthSquared() > 0) knock.normalize().multiply(1.0);
                knock.setY(0.55);
                p.setVelocity(knock);
                p.sendMessage("§c§l⚔ Bạn bị hất tung bởi cú đập đất của Tướng Chỉ Huy!");
            }
        }
    }

    /** Return nearest online player to a location */
    private Player getNearestPlayer(Location loc) {
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : getOnlinePlayers()) {
            if (!p.getWorld().equals(loc.getWorld())) continue;
            double d = p.getLocation().distanceSquared(loc);
            if (d < minDist) { minDist = d; nearest = p; }
        }
        return nearest;
    }

    private LivingEntity spawnMob(World world, Location loc, int type, int wave) {
        double scale = scaledHP(1.0);
        switch (type) {
            case 0: {
                Zombie z = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
                z.setCustomName("§cQuân Xâm Lược §7[Đợt " + (wave + 1) + "]"); z.setCustomNameVisible(true);
                z.setMaxHealth((30 + wave * 5) * scale); z.setHealth(z.getMaxHealth()); return z;
            }
            case 1: {
                Skeleton s = (Skeleton) world.spawnEntity(loc, EntityType.SKELETON);
                s.setCustomName("§cCung Thủ Bóng Tối §7[Đợt " + (wave + 1) + "]"); s.setCustomNameVisible(true);
                s.setMaxHealth((25 + wave * 5) * scale); s.setHealth(s.getMaxHealth()); return s;
            }
            case 2: {
                PigZombie v = (PigZombie) world.spawnEntity(loc, EntityType.PIG_ZOMBIE);
                v.setCustomName("§4§lTinh Nhuệ Chiến Binh"); v.setCustomNameVisible(true);
                v.setMaxHealth(80 * scale); v.setHealth(v.getMaxHealth()); v.setAngry(true);
                v.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
                return v;
            }
            case 3: {
                IronGolem r = (IronGolem) world.spawnEntity(loc, EntityType.IRON_GOLEM);
                r.setCustomName("§4§l⚔ Tướng Tổng Chỉ Huy"); r.setCustomNameVisible(true);
                r.setMaxHealth(200 * scale); r.setHealth(r.getMaxHealth()); return r;
            }
            default: return null;
        }
    }
}
