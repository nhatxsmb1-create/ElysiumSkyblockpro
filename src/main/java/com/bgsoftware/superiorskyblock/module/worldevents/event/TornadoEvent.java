package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*; import org.bukkit.entity.*; import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;

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

        BukkitRunnable particles = new BukkitRunnable() {
            double a=0; int e=0;
            @Override public void run() {
                e+=2; if(e>=DURATION||!boss.isValid()){cancel();return;}
                a+=15;
                for(int l=0;l<8;l++){
                    double ang=Math.toRadians(a+l*22),r=1.5+l*0.3;
                    Location p=center.clone().add(Math.cos(ang)*r,l*0.5,Math.sin(ang)*r);
                    fx(p,1,"LARGE_SMOKE","CLOUD"); fx(p,1,"CRIT");
                }
            }
        };
        particles.runTaskTimer(plugin,0L,2L);

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
