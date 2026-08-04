package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*; import org.bukkit.entity.*; import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;

public class AncientTreeEvent extends IslandWorldEvent {
    public AncientTreeEvent(Island island, Location center) { super(island, center, WorldEventType.ANCIENT_TREE); }

    @Override public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin=plugin;
        broadcast("§2🌳 §fMột §2§lCây Cổ Thụ §fhuyền bí đã mọc lên! §aDryad §fđang canh giữ nó!");
        countdown("§2Cây cổ thụ đang thức giấc...", ()->spawnBoss(onFinish));
    }

    private void spawnBoss(Runnable onFinish) {
        World world=center.getWorld();
        BukkitRunnable aura=new BukkitRunnable(){double a=0; int e=0; @Override public void run(){
            e+=3; if(e>20*60*5){cancel();return;} a+=8;
            for(int l=0;l<6;l++){double r=Math.max(0.5,3.0-l*0.4),ang=Math.toRadians(a+l*30);
                Location p=center.clone().add(Math.cos(ang)*r,l*1.5,Math.sin(ang)*r);
                fx(p,1,"HAPPY_VILLAGER","FIREWORKS_SPARK"); fx(p,1,"MOBSPAWNER_FLAMES");}
        }};
        aura.runTaskTimer(plugin,0L,3L);

        Witch dryad=(Witch)world.spawnEntity(center.clone().add(0,1,0),EntityType.WITCH);
        dryad.setCustomName("§a🌿 Dryad Cổ Đại"); dryad.setCustomNameVisible(true);
        double hp=scaledHP(100.0); dryad.setMaxHealth(hp); dryad.setHealth(hp);
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,Integer.MAX_VALUE,0,false,false));
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,0,false,false));
        sound(center,1f,0.6f,"DIG_GRASS","BLOCK_GRASS_PLACE");
        trackHPBar(dryad,"§a🌿 Dryad Cổ Đại");

        new BukkitRunnable(){int e=0; @Override public void run(){
            e+=20;
            if(!dryad.isValid()){
                cancel(); aura.cancel();
                Location d=dryad.getLocation();
                world.dropItemNaturally(d,named(Material.VINE,"§a§lTinh Chất Thiên Nhiên"));
                world.dropItemNaturally(d,named(Material.SAPLING,"§2§lHạt Giống Cổ Rừng"));
                world.dropItemNaturally(d,named(Material.EMERALD,"§a§lBụi Rừng Xanh"));
                if(hasLootBonus()){world.dropItemNaturally(d,named(Material.NETHER_STAR,"§2§l축복 Phước Lành Dryad"));
                    broadcast("§a🌳 §lPhần thưởng đặc biệt! §r§aBúp Bê Phước Lành Dryad đã rơi!");}
                broadcast("§a🌳 Dryad Cổ Đại đã bị đánh bại! Tinh Chất Thiên Nhiên đã rơi!");
                sound(center,1f,1.6f,"ENDERDRAGON_DEATH","ENTITY_ENDER_DRAGON_DEATH");
                logResult("HOÀN THÀNH"); onFinish.run(); return;
            }
            if(e>=20*60*5){cancel();aura.cancel();dryad.remove();
                broadcast("§c🌳 Cây Cổ Thụ đã tàn lụi..."); logResult("HẾT GIỜ"); onFinish.run();}
        }}.runTaskTimer(plugin,20L,20L);
    }
}
