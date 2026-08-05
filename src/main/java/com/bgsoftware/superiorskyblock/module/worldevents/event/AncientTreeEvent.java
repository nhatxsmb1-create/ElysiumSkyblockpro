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

            // Custom dryad abilities
            Location dLoc = dryad.getLocation();

            // 1. Entangling Roots every 100 ticks (5s)
            if (e % 100 == 0) {
                List<Player> players = getOnlinePlayers();
                if (!players.isEmpty()) {
                    Player target = players.get(rng.nextInt(players.size()));
                    if (target.getWorld().equals(dLoc.getWorld()) && target.getLocation().distance(dLoc) < 25.0) {
                        target.sendMessage("§2🌿 Rễ cây cổ đại đang trói chân bạn!");
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 4));
                        Location targetLoc = target.getLocation();
                        fx(targetLoc, 15, "SLIME", "HAPPY_VILLAGER");
                        sound(targetLoc, 1f, 0.8f, "WOOD_BREAK", "BLOCK_WOOD_BREAK");
                    }
                }
            }

            // 2. Healing Pollen / Poison every 60 ticks (3s)
            if (e % 60 == 0) {
                double newHP = Math.min(dryad.getMaxHealth(), dryad.getHealth() + 10.0);
                dryad.setHealth(newHP);
                fx(dLoc.clone().add(0, 1, 0), 10, "HAPPY_VILLAGER");
                sound(dLoc, 0.6f, 1.2f, "DIG_GRASS", "BLOCK_GRASS_PLACE");

                for (Player p : getOnlinePlayers()) {
                    if (p.getWorld().equals(dLoc.getWorld())) {
                        double dist = p.getLocation().distance(dLoc);
                        if (dist < 6.0) {
                            p.sendMessage("§a🌿 Bạn hít phải phấn hoa độc của Dryad!");
                            p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
                            fx(p.getLocation().add(0, 1, 0), 5, "SPELL_MOB", "SPELL_MOB_AMBIENT");
                            sound(p.getLocation(), 0.5f, 0.9f, "FIZZ", "BLOCK_FIRE_EXTINGUISH");
                        }
                    }
                }
            }

            if(e>=20*60*5){cancel();aura.cancel();dryad.remove();
                broadcast("§c🌳 Cây Cổ Thụ đã tàn lụi..."); logResult("HẾT GIỜ"); onFinish.run();}
        }}.runTaskTimer(plugin,20L,20L);
    }
}
