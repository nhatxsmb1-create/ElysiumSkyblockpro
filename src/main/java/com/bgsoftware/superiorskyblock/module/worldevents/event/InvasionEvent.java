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
        if (idx >= WAVES.length) {
            Location drop = getPlayerNearbySpawn(5);
            center.getWorld().dropItemNaturally(drop, createEventItem(Material.IRON_INGOT, "§c§lCúp Bảo Vệ Đảo", "§e§lHiếm", "Sự kiện Xâm Lược", "Kỷ niệm chương vinh danh người anh hùng đã dũng cảm bảo vệ hòn đảo khỏi bầy yêu quái."));
            center.getWorld().dropItemNaturally(drop, createEventItem(Material.GOLD_NUGGET, "§6§lXu Chiến Lợi Phẩm", "§a§lThường", "Sự kiện Xâm Lược", "Đồng xu cổ được quân xâm lược mang theo.", 5));
            if (hasLootBonus()) {
                center.getWorld().dropItemNaturally(drop, createEventItem(Material.DIAMOND, "§b§lKim Cương Chỉ Huy", "§6§lHuyền Thoại", "Sự kiện Xâm Lược", "Viên kim cương cướp được từ tên thủ lĩnh quân xâm lược.", 3));
                broadcast("§c👹 §lPhần thưởng đặc biệt! §r§cKim Cương Chỉ Huy đã rơi!");
            }
            broadcast("§a👹 Quân xâm lược đã bị đẩy lui!");
            dropTrophy(drop, "invasion");
            sound(center, 1f, 1f, "LEVEL_UP", "ENTITY_PLAYER_LEVELUP");
            logResult("HOÀN THÀNH"); onFinish.run(); return;
        }

        World world = center.getWorld();
        broadcast("§c👹 §fĐợt §e" + (idx + 1) + "§c/§e" + WAVES.length + " §fquân tấn công ào đến!");
        sound(center, 0.7f, 0.8f, "ENDERDRAGON_WINGS", "ENTITY_ENDER_DRAGON_FLAP");

        List<LivingEntity> waveMobs = new ArrayList<>();

        for (int i = 0; i < WAVES[idx][0]; i++) {
            // Spawn each mob near a random player
            Location spawnLoc = getPlayerNearbySpawn(8);
            // Spawn flash effect
            for (int j = 0; j < 16; j++) {
                double ang = j * (Math.PI * 2 / 16);
                Location ring = spawnLoc.clone().add(Math.cos(ang) * 1.5, 0.1, Math.sin(ang) * 1.5);
                fx(ring, 1, "CRIT");
                particle(ring, 1, "CRIT");
            }
            fx(spawnLoc.clone().add(0, 1, 0), 4, "SMOKE");
            particle(spawnLoc.clone().add(0, 1, 0), 4, "SMOKE_LARGE");

            LivingEntity mob = spawnMob(world, spawnLoc, WAVES[idx][1], idx);
            if (mob != null) {
                targetNearestPlayer(mob);
                waveMobs.add(mob);
                if (WAVES[idx][1] == 3) trackHPBar(mob, "§4⚔ Tướng Tổng Chỉ Huy");
            }
        }

        int[] arrowCooldown  = {0};
        int[] slamCooldown   = {0};
        int[] retargetTicker = {0};

        new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 20;
                retargetTicker[0] += 20;

                // Re-target mobs every 5 seconds
                if (retargetTicker[0] >= 100) {
                    retargetTicker[0] = 0;
                    waveMobs.forEach(m -> { if (m.isValid()) targetNearestPlayer(m); });
                }

                // ── WAVE 2: Skeleton Arrow Rain ────────────────────────
                if (idx == 1) {
                    arrowCooldown[0] += 20;
                    boolean skelAlive = waveMobs.stream().anyMatch(m -> m instanceof Skeleton && m.isValid());
                    if (skelAlive && arrowCooldown[0] >= 80) {
                        arrowCooldown[0] = 0;
                        broadcast("§e🏹 Cung Thủ Bóng Tối khai hỏa! Né ngay!");
                        for (Player p : getOnlinePlayers()) {
                            Location pLoc = p.getLocation();
                            // Ground warning ring
                            for (int i = 0; i < 14; i++) {
                                double ang = i * (Math.PI * 2 / 14);
                                Location mark = pLoc.clone().add(Math.cos(ang) * 2.0, 0.15, Math.sin(ang) * 2.0);
                                fx(mark, 1, "CRIT");
                                particle(mark, 1, "CRIT");
                            }
                            sound(pLoc, 0.9f, 0.5f, "SHOOT_ARROW", "ENTITY_ARROW_SHOOT");
                            // 1-second delay so players can dodge
                            final Location frozenLoc = pLoc.clone();
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                for (int i = 0; i < 6; i++) {
                                    Location from = frozenLoc.clone().add(
                                            (rng.nextDouble() - 0.5) * 5,
                                            16 + rng.nextDouble() * 6,
                                            (rng.nextDouble() - 0.5) * 5);
                                    Arrow arrow = frozenLoc.getWorld().spawnArrow(from,
                                            new Vector(0, -1, 0), 1.6f, 6f);
                                    arrow.setShooter(null);
                                }
                            }, 20L);
                        }
                    }
                }

                // ── WAVE 3: Elite Warriors Leap ────────────────────────
                if (idx == 2) {
                    for (LivingEntity m : waveMobs) {
                        if (!(m instanceof PigZombie) || !m.isValid()) continue;
                        if (e % 60 == 0) {
                            Player nearest = getNearestPlayer(m.getLocation());
                            if (nearest != null && m.getLocation().distance(nearest.getLocation()) > 4) {
                                Vector leap = nearest.getLocation().toVector()
                                        .subtract(m.getLocation().toVector()).normalize().multiply(0.9);
                                leap.setY(0.55);
                                m.setVelocity(leap);
                                fx(m.getLocation(), 4, "CRIT");
                                particle(m.getLocation(), 4, "CRIT");
                                sound(m.getLocation(), 0.8f, 1.2f, "MOB_ZOMBIE_HURT", "ENTITY_ZOMBIE_HURT");
                            }
                        }
                    }
                }

                // ── WAVE 4: Iron Golem Ground Slam ─────────────────────
                if (idx == 3) {
                    slamCooldown[0] += 20;
                    for (LivingEntity m : waveMobs) {
                        if (!(m instanceof IronGolem) || !m.isValid()) continue;
                        Location gLoc = m.getLocation();

                        // Rotating fire aura
                        double aAng = e * 6;
                        for (int i = 0; i < 8; i++) {
                            double ang = Math.toRadians(aAng + i * 45);
                            Location fp = gLoc.clone().add(Math.cos(ang) * 3.5, 0.8, Math.sin(ang) * 3.5);
                            fx(fp, 1, "FLAME");
                            particle(fp, 1, "FLAME");
                        }

                        // Ground slam with wind-up ring
                        if (slamCooldown[0] >= 100) {
                            slamCooldown[0] = 0;
                            broadcast("§4§l⚔ Tướng Chỉ Huy đang tụ lực!");
                            new BukkitRunnable() {
                                int wt = 0;
                                final Location slamLoc = gLoc.clone();
                                @Override
                                public void run() {
                                    wt += 5;
                                    if (wt >= 25) {
                                        cancel();
                                        doSlam(slamLoc);
                                        return;
                                    }
                                    // Wind-up ring contracts inward
                                    double r = 7.0 - (wt / 25.0) * 5.0;
                                    for (int i = 0; i < 16; i++) {
                                        double ang = (i * (Math.PI * 2 / 16)) + wt * 0.4;
                                        Location ring = slamLoc.clone().add(Math.cos(ang) * r, 0.15, Math.sin(ang) * r);
                                        fx(ring, 1, "FLAME");
                                        particle(ring, 1, "FLAME");
                                    }
                                    sound(slamLoc, 0.5f + (wt / 25.0f) * 0.5f, 0.4f + (wt / 25.0f) * 0.3f,
                                            "ANVIL_LAND", "BLOCK_ANVIL_LAND");
                                }
                            }.runTaskTimer(plugin, 0L, 5L);
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

    private void doSlam(Location gLoc) {
        sound(gLoc, 1.2f, 0.5f, "EXPLODE", "ENTITY_GENERIC_EXPLODE");
        // Big explosion visual
        fx(gLoc, 5, "EXPLOSION_LARGE");
        particle(gLoc, 8, "EXPLOSION_LARGE");
        // Ground crack ring
        for (int i = 0; i < 20; i++) {
            double ang = i * (Math.PI * 2 / 20);
            Location ring = gLoc.clone().add(Math.cos(ang) * 5, 0.1, Math.sin(ang) * 5);
            fx(ring, 2, "SMOKE");
            particle(ring, 2, "SMOKE_LARGE");
        }
        for (Player p : getOnlinePlayers()) {
            if (!p.getWorld().equals(gLoc.getWorld())) continue;
            double dist = p.getLocation().distance(gLoc);
            if (dist < 10.0) {
                double dmg = dist < 4.0 ? 8.0 : 5.0;
                p.damage(dmg);
                Vector knock = p.getLocation().toVector().subtract(gLoc.toVector());
                knock.setY(0);
                if (knock.lengthSquared() > 0) knock.normalize().multiply(1.1);
                knock.setY(0.6);
                p.setVelocity(knock);
                p.sendMessage("§c§l⚔ Bạn bị hất tung bởi cú đập đất của Tướng Chỉ Huy!");
            }
        }
    }

    private Player getNearestPlayer(Location loc) {
        Player nearest = null; double minDist = Double.MAX_VALUE;
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
                v.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false)); return v;
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
