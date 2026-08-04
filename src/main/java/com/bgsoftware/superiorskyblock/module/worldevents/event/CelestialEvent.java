package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*; import org.bukkit.entity.*; import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class CelestialEvent extends IslandWorldEvent {
    private static final int DURATION=20*60*4;

    public CelestialEvent(Island island, Location center) { super(island, center, WorldEventType.CELESTIAL); }

    @Override public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin=plugin;
        broadcast("§d✦ §f§lSự Kiện Thiên Thể §fđã bắt đầu! Bầu trời lấp lánh ánh sao huyền ảo!");
        countdown("§dÁc Thú Sao đang hạ xuống...", ()->spawnBoss(onFinish));
    }

    private void spawnBoss(Runnable onFinish) {
        World world=center.getWorld();
        List<ArmorStand> crystals=new ArrayList<>();
        for(int i=0;i<5;i++){double a=i*(2*Math.PI/5);
            Location loc=center.clone().add(Math.cos(a)*15,10+rng.nextInt(6),Math.sin(a)*15);
            ArmorStand stand=(ArmorStand)world.spawnEntity(loc,EntityType.ARMOR_STAND);
            stand.setCustomName("§b✦ Pha Lê Thiên Thể"); stand.setCustomNameVisible(true);
            stand.setGravity(false); stand.setVisible(false); crystals.add(stand);}

        BukkitRunnable stars=new BukkitRunnable(){int e=0; @Override public void run(){
            e+=4; if(e>DURATION){cancel();return;}
            for(int i=0;i<8;i++){
                Location p=center.clone().add((rng.nextDouble()-.5)*60,5+rng.nextDouble()*18,(rng.nextDouble()-.5)*60);
                fx(p,1,"FIREWORKS_SPARK"); fx(p,1,"WITCH_MAGIC","SPELL_WITCH");}
        }};
        stars.runTaskTimer(plugin,0L,4L);

        Ghast beast=(Ghast)world.spawnEntity(center.clone().add(0,20,0),EntityType.GHAST);
        beast.setCustomName("§d✦ Ác Thú Sao"); beast.setCustomNameVisible(true);
        double hp=scaledHP(150.0); beast.setMaxHealth(hp); beast.setHealth(hp);
        sound(center,1f,0.5f,"GHAST_MOAN","ENTITY_GHAST_AMBIENT");
        trackHPBar(beast,"§d✦ Ác Thú Sao");

        new BukkitRunnable(){int e=0; @Override public void run(){
            e+=20;
            if(!beast.isValid()){
                cancel(); stars.cancel(); crystals.forEach(c->{if(c.isValid())c.remove();});
                Location d=beast.getLocation();
                world.dropItemNaturally(d,named(Material.GHAST_TEAR,"§d§lMảnh Tinh Tú"));
                world.dropItemNaturally(d,named(Material.GLOWSTONE_DUST,"§e§lBụi Thiên Thể"));
                if(hasLootBonus()){world.dropItemNaturally(d,named(Material.NETHER_STAR,"§b§lLõi Thiên Hà"));
                    broadcast("§d✦ §lPhần thưởng đặc biệt! §r§dLõi Thiên Hà đã rơi!");}
                broadcast("§a✦ Ác Thú Sao đã bị đánh bại! Phần thưởng thiên thể đã rơi xuống!");
                sound(center,1f,1.4f,"ENDERDRAGON_DEATH","ENTITY_ENDER_DRAGON_DEATH");
                logResult("HOÀN THÀNH"); onFinish.run(); return;
            }
            if(e>=DURATION){cancel();stars.cancel();beast.remove();
                crystals.forEach(c->{if(c.isValid())c.remove();});
                broadcast("§c✦ Sự Kiện Thiên Thể đã tan biến..."); logResult("HẾT GIỜ"); onFinish.run();}
        }}.runTaskTimer(plugin,20L,20L);
    }
}
