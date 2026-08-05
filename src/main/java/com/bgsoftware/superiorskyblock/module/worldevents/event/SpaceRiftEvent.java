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

public class SpaceRiftEvent extends IslandWorldEvent {
    private static final int WARN = 20 * 30, WAVES = 3, MOB_PER_WAVE = 3, WAVE_INTERVAL = 45;

    public SpaceRiftEvent(Island island, Location center) {
        super(island, center, WorldEventType.SPACE_RIFT);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§5🌀 §fMột §5§lCổng Không Gian §fđã xuất hiện phía trên đảo!");
        broadcast("§7Một vết nứt đang xé toạc thực tại... Thứ gì đó sắp vượt qua.");
        countdown("§5Cổng đang mở ra...", () -> openRift(onFinish));
    }

    private void openRift(Runnable onFinish) {
        World world = center.getWorld();
        Location rift = center.clone().add(0, 25, 0);

        // Expanding void spiral: rings grow outward as the rift tears open
        BukkitRunnable opening = new BukkitRunnable() {
            double angle = 0;
            double radius = 0.5;
            int e = 0;
            @Override
            public void run() {
                e += 2;
                if (e > WARN) { cancel(); return; }
                angle += 20;
                radius = Math.min(6.0, 0.5 + (e / (double) WARN) * 5.5);

                for (int i = 0; i < 4; i++) {
                    double ang = Math.toRadians(angle + i * 90);
                    Location p = rift.clone().add(Math.cos(ang) * radius, 0, Math.sin(ang) * radius);
                    fx(p, 3, "PORTAL");
                    fx(p, 1, "WITCH_MAGIC", "SPELL_WITCH");
                }
                // Inner core glow
                fx(rift, 2, "PORTAL");

                // Gravity shockwave pulses every 3 seconds
                if (e % 60 == 0) {
                    sound(rift, 0.7f, 0.4f + (e / (float) WARN * 0.6f), "ENDERMAN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT");
                    for (Player p : getOnlinePlayers()) {
                        if (p.getWorld().equals(rift.getWorld())) {
                            double dist = p.getLocation().distance(rift);
                            if (dist < 20.0) {
                                p.sendMessage("§5§l🌀 Sóng hư không từ cổng xé toạc bạn!");
                                Vector pull = rift.toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.15);
                                pull.setY(0.08);
                                p.setVelocity(pull);
                            }
                        }
                    }
                }
            }
        };
        opening.runTaskTimer(plugin, 0L, 2L);
        sound(rift, 1f, 0.4f, "ENDERMAN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            opening.cancel();
            broadcast("§5🌀 §cCổng xé toạc! §5Thực Thể Hư Vô §ctràn ra!");
            sound(rift, 1f, 0.6f, "ENDERDRAGON_GROWL", "ENTITY_ENDER_DRAGON_GROWL");
            runWaves(world, rift, onFinish);
        }, WARN);
    }

    private void runWaves(World world, Location rift, Runnable onFinish) {
        List<LivingEntity> mobs = new ArrayList<>();
        int[] wavesDone = {0};
        double mobHP = scaledHP(60.0);

        Runnable spawnWave = () -> {
            wavesDone[0]++;
            broadcast("§5🌀 §fĐợt §e" + wavesDone[0] + "§5/" + WAVES + " §fđã xuất hiện từ cổng!");
            sound(rift, 1f, 0.7f, "ENDERMAN_SCREAM", "ENTITY_ENDERMAN_SCREAM");
            // Spawn effect: burst of portal particles
            for (int j = 0; j < 20; j++) {
                double ra = rng.nextDouble() * Math.PI * 2;
                double rr = rng.nextDouble() * 4;
                fx(rift.clone().add(Math.cos(ra) * rr, (rng.nextDouble() - 0.5) * 3, Math.sin(ra) * rr), 2, "PORTAL");
            }
            for (int i = 0; i < MOB_PER_WAVE; i++) {
                double a = Math.random() * Math.PI * 2;
                Location sp = rift.clone().add(Math.cos(a) * 3, -5, Math.sin(a) * 3);
                Enderman e = (Enderman) world.spawnEntity(sp, EntityType.ENDERMAN);
                e.setCustomName("§5Thực Thể Hư Vô §7[Đợt " + wavesDone[0] + "]");
                e.setCustomNameVisible(true);
                e.setMaxHealth(mobHP + wavesDone[0] * 10);
                e.setHealth(e.getMaxHealth());
                e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 0, false, false));
                mobs.add(e);
            }
        };

        for (int w = 0; w < WAVES; w++)
            plugin.getServer().getScheduler().runTaskLater(plugin, spawnWave::run, (long) w * WAVE_INTERVAL * 20L);

        // Constant void gravity pull + visual maelstrom
        BukkitRunnable voidMaelstrom = new BukkitRunnable() {
            double swirlAngle = 0;
            int e = 0;
            @Override
            public void run() {
                e += 4;
                if (wavesDone[0] >= WAVES && mobs.stream().noneMatch(Entity::isValid)) { cancel(); return; }

                // Spiral rings around rift
                swirlAngle += 12;
                for (int i = 0; i < 3; i++) {
                    double ang = Math.toRadians(swirlAngle + i * 120);
                    double r = 2.0 + Math.sin(e * 0.05) * 1.5;
                    Location p = rift.clone().add(Math.cos(ang) * r, 0, Math.sin(ang) * r);
                    fx(p, 2, "PORTAL");
                }
                // Inner eye of the rift
                fx(rift, 2, "PORTAL");
                fx(rift.clone().add(0, -1, 0), 1, "WITCH_MAGIC", "SPELL_WITCH");

                // Gravity pull on players every 2 seconds
                if (e % 40 == 0) {
                    for (Player p : getOnlinePlayers()) {
                        if (!p.getWorld().equals(rift.getWorld())) continue;
                        Location pLoc = p.getLocation();
                        double dist = pLoc.distance(rift);
                        if (dist < 30.0) {
                            double strength = dist < 15.0 ? 0.25 : 0.12;
                            p.sendMessage("§d§l🌀 Trọng lực hư vô đang hút bạn về phía cổng!");

                            // Levitation effect
                            PotionEffectType levitation = PotionEffectType.getByName("LEVITATION");
                            if (levitation != null) {
                                p.addPotionEffect(new PotionEffect(levitation, 30, dist < 10.0 ? 2 : 0));
                            }

                            Vector pull = rift.toVector().subtract(pLoc.toVector());
                            pull.setY(0);
                            if (pull.lengthSquared() > 0) pull.normalize().multiply(strength);
                            pull.setY(dist < 12.0 ? 0.2 : 0.08);
                            p.setVelocity(pull);
                            sound(pLoc, 0.5f, 0.6f, "ENDERMAN_TELEPORT", "ENTITY_ENDERMAN_TELEPORT");
                            fx(pLoc, 4, "PORTAL");
                        }
                    }
                }

                // Dimensional tear: random void shockwave burst every 6 seconds
                if (e % 120 == 0) {
                    sound(rift, 1f, 0.5f, "ENDERDRAGON_GROWL", "ENTITY_ENDER_DRAGON_GROWL");
                    for (int i = 0; i < 3; i++) {
                        double ra = rng.nextDouble() * Math.PI * 2;
                        double rr = 1 + rng.nextDouble() * 5;
                        fx(rift.clone().add(Math.cos(ra) * rr, (rng.nextDouble() - 0.5) * 4, Math.sin(ra) * rr), 5, "PORTAL");
                    }
                    broadcast("§5🌀 Cổng không gian rung chuyển!");
                    for (Player p : getOnlinePlayers()) {
                        if (p.getWorld().equals(rift.getWorld()) && p.getLocation().distance(rift) < 20.0) {
                            p.sendMessage("§5🌀 Chấn động hư không làm bạn mất phương hướng!");
                            PotionEffectType blindness = PotionEffectType.getByName("BLINDNESS");
                            if (blindness != null) p.addPotionEffect(new PotionEffect(blindness, 30, 0));
                        }
                    }
                }
            }
        };
        voidMaelstrom.runTaskTimer(plugin, 0L, 4L);

        long timeout = (long) (WAVES + 1) * WAVE_INTERVAL * 20L + 20 * 60L;
        new BukkitRunnable() {
            int e = 0;
            @Override
            public void run() {
                e += 20;
                if (mobs.stream().noneMatch(Entity::isValid) && wavesDone[0] >= WAVES) {
                    cancel(); voidMaelstrom.cancel();
                    Location drop = rift.clone().add(0, -5, 0);
                    world.dropItemNaturally(drop, named(Material.ENDER_PEARL, "§5§lThánh Vật Hư Vô"));
                    world.dropItemNaturally(drop, named(Material.EMERALD, "§d§lMảnh Cổng Không Gian"));
                    if (hasLootBonus()) {
                        world.dropItemNaturally(drop, named(Material.OBSIDIAN, "§5§lTinh Chất Hư Vô", 5));
                        broadcast("§d🌀 §lPhần thưởng đặc biệt! §r§dTinh Chất Hư Vô đã rơi!");
                    }
                    broadcast("§a🌀 Cổng Không Gian đã đóng lại! §5Thánh Vật Hư Vô §ađã rơi!");
                    sound(rift, 1f, 1.2f, "ENDERDRAGON_DEATH", "ENTITY_ENDER_DRAGON_DEATH");
                    logResult("HOÀN THÀNH"); onFinish.run(); return;
                }
                if (e >= timeout) {
                    cancel(); voidMaelstrom.cancel();
                    mobs.forEach(m -> { if (m.isValid()) m.remove(); });
                    broadcast("§c🌀 Cổng tự đóng lại... Thực thể đã rút lui."); logResult("HẾT GIỜ"); onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
