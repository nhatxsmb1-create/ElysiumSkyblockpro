package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*; import org.bukkit.entity.*; import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class TornadoEvent extends IslandWorldEvent {
    private static final int DURATION = 20*60*3;

    public TornadoEvent(Island island, Location center) { super(island, center, WorldEventType.TORNADO); }

    @Override public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§b🌪 §fMột §b§lLốc Xoáy §fkhổng lồ đang hình thành! Tiêu diệt §e§lHồn Bão§f!");
        countdown("§eĐang tạo lốc xoáy...", () -> spawnBoss(onFinish));
    }

    private void spawnBoss(Runnable onFinish) {
        World world = center.getWorld();
        Zombie boss = (Zombie) world.spawnEntity(center.clone().add(0,3,0), EntityType.ZOMBIE);
        boss.setCustomName("§b⚡ Hồn Bão"); boss.setCustomNameVisible(true);
        double hp = scaledHP(120.0); boss.setMaxHealth(hp); boss.setHealth(hp);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        trackHPBar(boss, "§b⚡ Hồn Bão");

        Location tornadoPos = center.clone();

        BukkitRunnable particles = new BukkitRunnable() {
            double a = 0; 
            int e = 0;
            double angleDir = rng.nextDouble() * Math.PI * 2;

            @Override public void run() {
                e += 2; if (e >= DURATION || !boss.isValid()) { cancel(); return; }
                a += 15;

                // 1. Move tornado position in a smooth drifting random walk
                angleDir += (rng.nextDouble() - 0.5) * 0.5;
                double speed = 0.15;
                tornadoPos.add(Math.cos(angleDir) * speed, 0, Math.sin(angleDir) * speed);

                // Keep tornado within 30 blocks of center
                if (tornadoPos.distance(center) > 30.0) {
                    Vector back = center.toVector().subtract(tornadoPos.toVector()).normalize().multiply(0.2);
                    tornadoPos.add(back.getX(), 0, back.getZ());
                }

                tornadoPos.setY(tornadoPos.getWorld().getHighestBlockYAt(tornadoPos) + 1.0);
                boss.teleport(tornadoPos.clone().add(0, 0.5, 0));

                // 2. Spawn spiral tornado particles
                for (int l = 0; l < 12; l++) {
                    double ang = Math.toRadians(a + l * 20);
                    double r = 1.0 + l * 0.4;
                    Location p = tornadoPos.clone().add(Math.cos(ang) * r, l * 0.8, Math.sin(ang) * r);
                    fx(p, 1, "LARGE_SMOKE", "CLOUD");
                    fx(p, 1, "CRIT");
                }

                // Play wind sound
                if (e % 10 == 0) {
                    sound(tornadoPos, 0.8f, 0.7f, "GHAST_FIREBALL", "ENTITY_GHAST_SHOOT");
                }

                // 3. Pull players and spin them around the moving tornado
                for (Player p : getOnlinePlayers()) {
                    if (p.getWorld().equals(tornadoPos.getWorld())) {
                        double dist = p.getLocation().distance(tornadoPos);
                        if (dist < 15.0) {
                            Vector toCenter = tornadoPos.toVector().subtract(p.getLocation().toVector());
                            toCenter.setY(0);

                            Vector tangent = new Vector(-toCenter.getZ(), 0, toCenter.getX());
                            if (tangent.lengthSquared() > 0) {
                                tangent.normalize().multiply(0.28);
                            }
                            if (toCenter.lengthSquared() > 0) {
                                toCenter.normalize().multiply(0.18);
                            }

                            Vector result = toCenter.add(tangent);
                            result.setY(0.38);
                            p.setVelocity(result);

                            if (e % 20 == 0) {
                                p.sendMessage("§c§l🌪 Bạn đang bị cuốn bay bởi lốc xoáy!");
                                sound(p.getLocation(), 0.4f, 0.9f, "BAT_LOOP", "ENTITY_BAT_LOOP");
                            }
                        }
                    }
                }
            }
        };
        particles.runTaskTimer(plugin, 0L, 2L);

        new BukkitRunnable() {
            int e=0; @Override public void run() {
                e+=20;
                if(!boss.isValid()){
                    cancel(); particles.cancel();
                    world.dropItemNaturally(boss.getLocation(), named(Material.NETHER_STAR,"§b§lLõi Bão"));
                    if(hasLootBonus()) world.dropItemNaturally(boss.getLocation(), named(Material.GOLD_INGOT,"§b§lMảnh Sét"));
                    broadcast("§a🌪 Hồn Bão đã bị tiêu diệt! §eLõi Bão §ađã rơi xuống!");
                    sound(center,1f,1.5f,"ENDERDRAGON_DEATH","ENTITY_ENDER_DRAGON_DEATH");
                    logResult("HOÀN THÀNH"); onFinish.run(); return;
                }
                if(e>=DURATION){cancel();particles.cancel();boss.remove();
                    broadcast("§c🌪 Lốc Xoáy đã tan biến... Hồn Bão đã thoát.");
                    logResult("HẾT GIỜ"); onFinish.run();}
            }
        }.runTaskTimer(plugin,20L,20L);
    }
}
