package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*; import org.bukkit.entity.*; import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class SpaceRiftEvent extends IslandWorldEvent {
    private static final int WARN=20*30, WAVES=3, MOB_PER_WAVE=3, WAVE_INTERVAL=45;

    public SpaceRiftEvent(Island island, Location center) { super(island, center, WorldEventType.SPACE_RIFT); }

    @Override public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin=plugin;
        broadcast("§5🌀 §fMột §5§lCổng Không Gian §fđã xuất hiện phía trên đảo!");
        broadcast("§7Một vết nứt đang xé toạc thực tại... Thứ gì đó sắp vượt qua.");
        countdown("§5Cổng đang mở ra...", ()->openRift(onFinish));
    }

    private void openRift(Runnable onFinish) {
        World world=center.getWorld(); Location rift=center.clone().add(0,25,0);
        BukkitRunnable swirl=new BukkitRunnable(){double a=0; int e=0; @Override public void run(){
            e+=2; if(e>WARN){cancel();return;} a+=18;
            for(int i=0;i<3;i++){double ang=Math.toRadians(a+i*120);
                Location p=rift.clone().add(Math.cos(ang)*2,0,Math.sin(ang)*2);
                fx(p,3,"PORTAL"); fx(p,1,"WITCH_MAGIC","SPELL_WITCH");}
        }};
        swirl.runTaskTimer(plugin,0L,2L);
        sound(rift,1f,0.4f,"ENDERMAN_TELEPORT","ENTITY_ENDERMAN_TELEPORT");

        plugin.getServer().getScheduler().runTaskLater(plugin,()->{
            swirl.cancel();
            broadcast("§5🌀 §cCổng xé toạc! §5Thực Thể Hư Vô §ctràn ra!");
            sound(rift,1f,0.6f,"ENDERDRAGON_GROWL","ENTITY_ENDER_DRAGON_GROWL");
            runWaves(world,rift,onFinish);
        },WARN);
    }

    private void runWaves(World world, Location rift, Runnable onFinish) {
        List<LivingEntity> mobs=new ArrayList<>();
        int[] wavesDone={0};
        double mobHP=scaledHP(60.0);

        Runnable spawnWave=()->{
            wavesDone[0]++;
            broadcast("§5🌀 §fĐợt §e"+wavesDone[0]+"§5/"+WAVES+" §fđã xuất hiện!");
            sound(rift,1f,0.7f,"ENDERMAN_SCREAM","ENTITY_ENDERMAN_SCREAM");
            for(int i=0;i<MOB_PER_WAVE;i++){
                double a=Math.random()*Math.PI*2;
                Location sp=rift.clone().add(Math.cos(a)*3,-5,Math.sin(a)*3);
                Enderman e=(Enderman)world.spawnEntity(sp,EntityType.ENDERMAN);
                e.setCustomName("§5Thực Thể Hư Vô §7[Đợt "+wavesDone[0]+"]"); e.setCustomNameVisible(true);
                e.setMaxHealth(mobHP+wavesDone[0]*10); e.setHealth(e.getMaxHealth());
                e.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,Integer.MAX_VALUE,0,false,false));
                mobs.add(e);
            }
        };

        for(int w=0;w<WAVES;w++)
            plugin.getServer().getScheduler().runTaskLater(plugin,spawnWave::run,(long)w*WAVE_INTERVAL*20L);

        long timeout=(long)(WAVES+1)*WAVE_INTERVAL*20L+20*60L;
        new BukkitRunnable(){int e=0; @Override public void run(){
            e+=20;
            if(mobs.stream().noneMatch(Entity::isValid)&&wavesDone[0]>=WAVES){
                cancel();
                Location drop=rift.clone().add(0,-5,0);
                world.dropItemNaturally(drop,named(Material.ENDER_PEARL,"§5§lThánh Vật Hư Vô"));
                world.dropItemNaturally(drop,named(Material.EMERALD,"§d§lMảnh Cổng Không Gian"));
                if(hasLootBonus()){world.dropItemNaturally(drop,named(Material.OBSIDIAN,"§5§lTinh Chất Hư Vô",5));
                    broadcast("§d🌀 §lPhần thưởng đặc biệt! §r§dTinh Chất Hư Vô đã rơi!");}
                broadcast("§a🌀 Cổng Không Gian đã đóng lại! §5Thánh Vật Hư Vô §ađã rơi!");
                sound(rift,1f,1.2f,"ENDERDRAGON_DEATH","ENTITY_ENDER_DRAGON_DEATH");
                logResult("HOÀN THÀNH"); onFinish.run(); return;
            }
            if(e>=timeout){cancel();mobs.forEach(m->{if(m.isValid())m.remove();});
                broadcast("§c🌀 Cổng tự đóng lại... Thực thể đã rút lui."); logResult("HẾT GIỜ"); onFinish.run();}
        }}.runTaskTimer(plugin,20L,20L);
    }
}
