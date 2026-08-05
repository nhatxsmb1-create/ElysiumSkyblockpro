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

        // Boss spawns near a player
        Location bossSpawn = getPlayerNearbySpawn(8);

        // Spiraling vine aura (SLIME + FIREWORKS_SPARK — always visible)
        BukkitRunnable aura = new BukkitRunnable() {
            double angle = 0;
            int e = 0;
            @Override
            public void run() {
                e += 3;
                if (e > maxDuration) { cancel(); return; }
                angle += 10;
                for (int l = 0; l < 7; l++) {
                    double r   = Math.max(0.4, l * 0.6);
                    double ang = Math.toRadians(angle + l * 25.7);
                    Location p = bossSpawn.clone().add(Math.cos(ang) * r, l * 0.65, Math.sin(ang) * r);
                    fx(p, 1, "FIREWORKS_SPARK");
                    particle(p, 1, "FIREWORKS_SPARK");
                    if (l % 2 == 0) {
                        fx(p, 1, "SMOKE");
                        particle(p, 1, "SMOKE_LARGE");
                    }
                }
                // Ground root veins radiating outward
                if (e % 15 == 0) {
                    double rootDir = rng.nextDouble() * Math.PI * 2;
                    for (int step = 0; step < 8; step++) {
                        double dist = step * 0.9;
                        Location rp = bossSpawn.clone().add(Math.cos(rootDir) * dist, 0.1, Math.sin(rootDir) * dist);
                        fx(rp, 1, "FIREWORKS_SPARK");
                        particle(rp, 1, "FIREWORKS_SPARK");
                    }
                }
            }
        };
        aura.runTaskTimer(plugin, 0L, 3L);

        Witch dryad = (Witch) world.spawnEntity(bossSpawn, EntityType.WITCH);
        dryad.setCustomName("§a🌿 Dryad Cổ Đại");
        dryad.setCustomNameVisible(true);
        double hp = scaledHP(100.0);
        dryad.setMaxHealth(hp);
        dryad.setHealth(hp);
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        sound(bossSpawn, 1f, 0.6f, "DIG_GRASS", "BLOCK_GRASS_PLACE");
        targetNearestPlayer(dryad);
        trackHPBar(dryad, "§a🌿 Dryad Cổ Đại");

        int[] rootCooldown  = {0};
        int[] pollenCooldown = {0};
        int[] sporeCooldown  = {0};
        int[] retarget       = {0};

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
                    broadcast("§a🌳 Dryad Cổ Đại đã bị đánh bại!");
                    sound(center, 1f, 1.6f, "ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH");
                    logResult("HOÀN THÀNH"); onFinish.run(); return;
                }

                Location dLoc = dryad.getLocation();
                rootCooldown[0]  += 20;
                pollenCooldown[0] += 20;
                sporeCooldown[0]  += 20;
                retarget[0]       += 20;

                if (retarget[0] >= 80) { retarget[0] = 0; targetNearestPlayer(dryad); }

                // ── ABILITY 1: Entangling Roots ───────────────────────────
                // Animated roots crawl from boss toward target
                if (rootCooldown[0] >= 120) {
                    rootCooldown[0] = 0;
                    List<Player> candidates = new ArrayList<>(getOnlinePlayers());
                    candidates.removeIf(p -> !p.getWorld().equals(dLoc.getWorld())
                            || p.getLocation().distance(dLoc) > 22.0);
                    if (!candidates.isEmpty()) {
                        Player target = candidates.get(rng.nextInt(candidates.size()));
                        Location tLoc = target.getLocation().clone();
                        target.sendMessage("§2🌿 Rễ cây đang trườn về phía bạn!");
                        double totalDist = dLoc.distance(tLoc);
                        Vector dir = tLoc.toVector().subtract(dLoc.toVector()).normalize();

                        new BukkitRunnable() {
                            int step = 0; final int steps = 14;
                            @Override
                            public void run() {
                                step++;
                                if (step > steps) {
                                    cancel();
                                    // Root lock
                                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 5));
                                    target.sendMessage("§2🌿 Rễ cây trói chặt chân bạn!");
                                    for (int i = 0; i < 20; i++) {
                                        double ang = i * (Math.PI * 2 / 20);
                                        Location rp = tLoc.clone().add(Math.cos(ang) * 1.8, 0.15, Math.sin(ang) * 1.8);
                                        fx(rp, 1, "FIREWORKS_SPARK");
                                        particle(rp, 1, "FIREWORKS_SPARK");
                                    }
                                    sound(tLoc, 1f, 0.6f, "DIG_WOOD", "BLOCK_WOOD_BREAK");
                                    return;
                                }
                                double frac = step / (double) steps;
                                Location tip = dLoc.clone().add(dir.clone().multiply(frac * totalDist));
                                fx(tip, 2, "FIREWORKS_SPARK");
                                particle(tip, 2, "FIREWORKS_SPARK");
                                sound(tip, 0.4f, 0.7f, "DIG_WOOD", "BLOCK_WOOD_BREAK");
                            }
                        }.runTaskTimer(plugin, 0L, 2L);
                    }
                }

                // ── ABILITY 2: Healing Pollen Burst ──────────────────────
                // Expanding green ring that heals dryad + poisons players on contact
                if (pollenCooldown[0] >= 80) {
                    pollenCooldown[0] = 0;
                    double heal = Math.min(dryad.getMaxHealth(), dryad.getHealth() + 7.0);
                    dryad.setHealth(heal);

                    new BukkitRunnable() {
                        double radius = 0.5;
                        int t = 0;
                        @Override
                        public void run() {
                            t += 3;
                            radius = 0.5 + (t / 24.0) * 7.5;
                            if (t >= 24) { cancel(); return; }
                            for (int i = 0; i < 18; i++) {
                                double ang = i * (Math.PI * 2 / 18);
                                Location pp = dLoc.clone().add(Math.cos(ang) * radius, 0.7, Math.sin(ang) * radius);
                                fx(pp, 1, "FIREWORKS_SPARK");
                                particle(pp, 1, "FIREWORKS_SPARK");
                                particle(pp, 1, "SMOKE_LARGE");
                            }
                            for (Player p : getOnlinePlayers()) {
                                if (!p.getWorld().equals(dLoc.getWorld())) continue;
                                double pDist = p.getLocation().distance(dLoc);
                                if (Math.abs(pDist - radius) < 1.8) {
                                    p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                                    p.sendMessage("§a🌿 Phấn hoa độc chạm vào bạn!");
                                }
                            }
                        }
                    }.runTaskTimer(plugin, 0L, 3L);
                    sound(dLoc, 0.8f, 1.1f, "DIG_GRASS", "BLOCK_GRASS_PLACE");
                }

                // ── ABILITY 3: Thorn Spore Barrage ───────────────────────
                // Fling FallingBlock thorns toward every nearby player with FIREWORK marker
                if (sporeCooldown[0] >= 160) {
                    sporeCooldown[0] = 0;
                    broadcast("§2🌿 Dryad phóng bào tử gai!");
                    for (Player p : getOnlinePlayers()) {
                        if (!p.getWorld().equals(dLoc.getWorld())) continue;
                        if (p.getLocation().distance(dLoc) > 28.0) continue;
                        final Location pLoc = p.getLocation().clone();

                        // Marker ring at target
                        new BukkitRunnable() {
                            int wt = 0;
                            @Override
                            public void run() {
                                wt += 4;
                                if (wt >= 36) { cancel(); flingSpore(dLoc, pLoc); return; }
                                for (int i = 0; i < 10; i++) {
                                    double ang = (i * Math.PI * 2 / 10) + wt * 0.12;
                                    Location mark = pLoc.clone().add(Math.cos(ang) * 1.6, 0.15, Math.sin(ang) * 1.6);
                                    fx(mark, 1, "FIREWORKS_SPARK");
                                    particle(mark, 1, "FIREWORKS_SPARK");
                                }
                                sound(pLoc, 0.35f, 0.5f + (wt / 36.0f * 0.4f), "DIG_WOOD", "BLOCK_WOOD_BREAK");
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

    private void flingSpore(Location from, Location target) {
        FallingBlock spore;
        try {
            spore = from.getWorld().spawnFallingBlock(from.clone().add(0, 2, 0),
                    Material.valueOf("LOG"), (byte) 1);
        } catch (Exception ex) {
            try {
                spore = from.getWorld().spawnFallingBlock(from.clone().add(0, 2, 0),
                        Material.SAND, (byte) 0);
            } catch (Exception ex2) {
                spore = from.getWorld().spawnFallingBlock(from.clone().add(0, 2, 0),
                        Material.GRAVEL, (byte) 0);
            }
        }
        spore.setDropItem(false);
        Vector dir = target.toVector().subtract(from.toVector()).normalize().multiply(1.3);
        dir.setY(dir.getY() + 0.45);
        spore.setVelocity(dir);

        final FallingBlock block = spore;
        new BukkitRunnable() {
            int life = 0;
            @Override
            public void run() {
                life += 2;
                if (!block.isValid() || block.isOnGround() || life > 70) {
                    cancel(); block.remove();
                    Location land = block.getLocation();
                    sound(land, 0.9f, 0.8f, "DIG_WOOD", "BLOCK_WOOD_BREAK");
                    fx(land, 8, "FIREWORKS_SPARK");
                    particle(land, 8, "FIREWORKS_SPARK");
                    for (Player p : getOnlinePlayers()) {
                        if (p.getWorld().equals(land.getWorld()) && p.getLocation().distance(land) < 2.2) {
                            p.damage(4.0);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
                            p.sendMessage("§2🌿 Bào tử gai ghim vào chân bạn!");
                        }
                    }
                    return;
                }
                Location loc = block.getLocation();
                fx(loc, 1, "FIREWORKS_SPARK");
                particle(loc, 1, "FIREWORKS_SPARK");
                particle(loc, 1, "SMOKE_LARGE");
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
