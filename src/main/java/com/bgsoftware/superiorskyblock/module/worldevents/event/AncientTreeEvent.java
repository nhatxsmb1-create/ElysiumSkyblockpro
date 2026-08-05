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

public class AncientTreeEvent extends IslandWorldEvent {
    public AncientTreeEvent(Island island, Location center) {
        super(island, center, WorldEventType.ANCIENT_TREE);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§2🌳 §fMột §2§lCây Cổ Thụ §fhuyền bí đã mọc lên! §aDryad §fđang canh giữ nó!");
        countdown("§2Cây cổ thụ đang thức giấc...", () -> spawnBoss(onFinish));
    }

    private void spawnBoss(Runnable onFinish) {
        World world = center.getWorld();
        int maxDuration = 20 * 60 * 5;

        // Spiraling vine aura around the tree center
        BukkitRunnable aura = new BukkitRunnable() {
            double a = 0;
            int e = 0;
            @Override
            public void run() {
                e += 3;
                if (e > maxDuration) { cancel(); return; }
                a += 8;
                for (int l = 0; l < 6; l++) {
                    double r = Math.max(0.5, 3.0 - l * 0.4), ang = Math.toRadians(a + l * 30);
                    Location p = center.clone().add(Math.cos(ang) * r, l * 1.5, Math.sin(ang) * r);
                    fx(p, 1, "HAPPY_VILLAGER", "FIREWORKS_SPARK");
                    fx(p, 1, "MOBSPAWNER_FLAMES");
                }
                // Ground roots radiate outward
                if (e % 20 == 0) {
                    double rootAng = rng.nextDouble() * Math.PI * 2;
                    for (int i = 0; i < 8; i++) {
                        double dist = i * 0.8;
                        Location rp = center.clone().add(Math.cos(rootAng) * dist, 0.1, Math.sin(rootAng) * dist);
                        fx(rp, 1, "SLIME", "HAPPY_VILLAGER");
                    }
                }
            }
        };
        aura.runTaskTimer(plugin, 0L, 3L);

        Witch dryad = (Witch) world.spawnEntity(center.clone().add(0, 1, 0), EntityType.WITCH);
        dryad.setCustomName("§a🌿 Dryad Cổ Đại");
        dryad.setCustomNameVisible(true);
        double hp = scaledHP(100.0);
        dryad.setMaxHealth(hp);
        dryad.setHealth(hp);
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        sound(center, 1f, 0.6f, "DIG_GRASS", "BLOCK_GRASS_PLACE");
        trackHPBar(dryad, "§a🌿 Dryad Cổ Đại");

        int[] rootCooldown = {0};
        int[] pollenCooldown = {0};
        int[] sporesCooldown = {0};

        new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 20;
                if (!dryad.isValid()) {
                    cancel(); aura.cancel();
                    Location d = dryad.getLocation();
                    world.dropItemNaturally(d, named(Material.VINE, "§a§lTinh Chất Thiên Nhiên"));
                    world.dropItemNaturally(d, named(Material.SAPLING, "§2§lHạt Giống Cổ Rừng"));
                    world.dropItemNaturally(d, named(Material.EMERALD, "§a§lBụi Rừng Xanh"));
                    if (hasLootBonus()) {
                        world.dropItemNaturally(d, named(Material.NETHER_STAR, "§2§lPhước Lành Dryad"));
                        broadcast("§a🌳 §lPhần thưởng đặc biệt! §r§aPhước Lành Dryad đã rơi!");
                    }
                    broadcast("§a🌳 Dryad Cổ Đại đã bị đánh bại! Tinh Chất Thiên Nhiên đã rơi!");
                    sound(center, 1f, 1.6f, "ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH");
                    logResult("HOÀN THÀNH"); onFinish.run(); return;
                }

                Location dLoc = dryad.getLocation();
                rootCooldown[0] += 20;
                pollenCooldown[0] += 20;
                sporesCooldown[0] += 20;

                // ── ABILITY 1: Entangling Roots ─────────────────────────────────
                // Target a player, show animated roots crawling along the ground,
                // then fully lock them in place for 4 seconds.
                if (rootCooldown[0] >= 120) {
                    rootCooldown[0] = 0;
                    List<Player> players = getOnlinePlayers();
                    players.removeIf(p -> !p.getWorld().equals(dLoc.getWorld()) || p.getLocation().distance(dLoc) > 25.0);
                    if (!players.isEmpty()) {
                        Player target = players.get(rng.nextInt(players.size()));
                        Location tLoc = target.getLocation();
                        target.sendMessage("§2🌿 Rễ cây cổ đại đang trườn về phía bạn!");
                        // Animated roots creeping toward player from boss
                        new BukkitRunnable() {
                            int step = 0;
                            final int totalSteps = 12;
                            final Vector dir = tLoc.toVector().subtract(dLoc.toVector()).normalize();
                            @Override
                            public void run() {
                                step++;
                                if (step > totalSteps) {
                                    cancel();
                                    // ROOT LOCK: heavy slowness + visual burst
                                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 5));
                                    target.sendMessage("§2🌿 Rễ cây đã trói chặt chân bạn!");
                                    for (int i = 0; i < 20; i++) {
                                        double ang = i * (Math.PI * 2 / 20);
                                        Location rp = tLoc.clone().add(Math.cos(ang) * 1.5, 0.2, Math.sin(ang) * 1.5);
                                        fx(rp, 1, "SLIME");
                                        fx(rp, 1, "HAPPY_VILLAGER");
                                    }
                                    sound(tLoc, 1f, 0.7f, "WOOD_BREAK", "BLOCK_WOOD_BREAK");
                                    sound(tLoc, 0.7f, 0.5f, "DIG_GRASS", "BLOCK_GRASS_PLACE");
                                    return;
                                }
                                double frac = step / (double) totalSteps;
                                Location rootTip = dLoc.clone().add(dir.clone().multiply(frac * dLoc.distance(tLoc)));
                                fx(rootTip, 2, "SLIME");
                                fx(rootTip, 1, "HAPPY_VILLAGER");
                                sound(rootTip, 0.4f, 0.8f, "WOOD_BREAK", "BLOCK_WOOD_BREAK");
                            }
                        }.runTaskTimer(plugin, 0L, 2L);
                    }
                }

                // ── ABILITY 2: Healing Pollen + Poison Cloud ────────────────────
                // Dryad releases a burst of pollen that heals herself and poisons
                // nearby players. The cloud visually billows outward.
                if (pollenCooldown[0] >= 80) {
                    pollenCooldown[0] = 0;
                    double newHP = Math.min(dryad.getMaxHealth(), dryad.getHealth() + 8.0);
                    dryad.setHealth(newHP);
                    // Pollen burst visual: expanding ring
                    new BukkitRunnable() {
                        double radius = 0.5;
                        int t = 0;
                        @Override
                        public void run() {
                            t += 3;
                            radius = 0.5 + (t / 30.0) * 7.5;
                            if (t >= 30) { cancel(); return; }
                            for (int i = 0; i < 16; i++) {
                                double ang = i * (Math.PI * 2 / 16);
                                Location pp = dLoc.clone().add(Math.cos(ang) * radius, 0.8, Math.sin(ang) * radius);
                                fx(pp, 1, "HAPPY_VILLAGER");
                                fx(pp, 1, "SPELL_MOB", "SPELL_MOB_AMBIENT");
                            }
                            // Apply poison to players caught in expanding cloud
                            for (Player p : getOnlinePlayers()) {
                                if (!p.getWorld().equals(dLoc.getWorld())) continue;
                                if (Math.abs(p.getLocation().distance(dLoc) - radius) < 2.0) {
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                                    p.sendMessage("§a🌿 Phấn hoa độc chạm vào bạn!");
                                }
                            }
                        }
                    }.runTaskTimer(plugin, 0L, 3L);
                    sound(dLoc, 0.8f, 1.2f, "DIG_GRASS", "BLOCK_GRASS_PLACE");
                }

                // ── ABILITY 3: Thorn Spore Barrage ──────────────────────────────
                // Every 8 seconds, fling thorns (FallingBlock logs) in an arc at
                // players. Each spore marks its landing zone before impact.
                if (sporesCooldown[0] >= 160) {
                    sporesCooldown[0] = 0;
                    broadcast("§2🌿 Dryad phóng bào tử gai về phía người chơi!");
                    for (Player p : getOnlinePlayers()) {
                        if (!p.getWorld().equals(dLoc.getWorld())) continue;
                        if (p.getLocation().distance(dLoc) > 30.0) continue;
                        Location pLoc = p.getLocation();
                        // Ground target marker
                        new BukkitRunnable() {
                            int wt = 0;
                            @Override
                            public void run() {
                                wt += 4;
                                if (wt >= 40) { cancel(); flingSpore(dLoc, pLoc); return; }
                                for (int i = 0; i < 8; i++) {
                                    double ang = (i * Math.PI * 2 / 8) + wt * 0.1;
                                    Location mark = pLoc.clone().add(Math.cos(ang) * 1.8, 0.15, Math.sin(ang) * 1.8);
                                    fx(mark, 1, "SLIME");
                                    fx(mark, 1, "HAPPY_VILLAGER");
                                }
                                sound(pLoc, 0.3f, 0.6f + (wt / 40.0f * 0.4f), "WOOD_BREAK", "BLOCK_WOOD_BREAK");
                            }
                        }.runTaskTimer(plugin, 0L, 4L);
                    }
                }

                if (e >= maxDuration) {
                    cancel(); aura.cancel(); dryad.remove();
                    broadcast("§c🌳 Cây Cổ Thụ đã tàn lụi..."); logResult("HẾT GIỜ"); onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /** Fling a FallingBlock spore from boss toward target with leaf trail */
    private void flingSpore(Location from, Location target) {
        FallingBlock spore;
        try {
            spore = from.getWorld().spawnFallingBlock(from.clone().add(0, 2, 0), Material.valueOf("LOG"), (byte) 1);
        } catch (Exception ex) {
            spore = from.getWorld().spawnFallingBlock(from.clone().add(0, 2, 0), Material.SAND, (byte) 0);
        }
        spore.setDropItem(false);
        Vector dir = target.toVector().subtract(from.toVector()).normalize().multiply(1.4);
        dir.setY(dir.getY() + 0.4);
        spore.setVelocity(dir);

        final FallingBlock block = spore;
        new BukkitRunnable() {
            int life = 0;
            @Override
            public void run() {
                life += 2;
                if (!block.isValid() || block.isOnGround() || life > 60) {
                    cancel(); block.remove();
                    Location land = block.getLocation();
                    sound(land, 0.9f, 0.8f, "DIG_GRASS", "BLOCK_GRASS_PLACE");
                    fx(land, 10, "SLIME");
                    fx(land, 5, "HAPPY_VILLAGER");
                    for (Player p : getOnlinePlayers()) {
                        if (p.getWorld().equals(land.getWorld()) && p.getLocation().distance(land) < 2.0) {
                            p.damage(4.0);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
                            p.sendMessage("§2🌿 Bào tử gai cây cổ thụ ghim vào chân bạn!");
                        }
                    }
                    return;
                }
                Location loc = block.getLocation();
                fx(loc, 1, "HAPPY_VILLAGER");
                fx(loc, 1, "SLIME");
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
