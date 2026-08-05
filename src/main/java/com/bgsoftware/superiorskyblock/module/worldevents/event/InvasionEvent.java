package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*; import org.bukkit.entity.*; import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.*;

public class InvasionEvent extends IslandWorldEvent {
    private static final int[][] WAVES={{4,0},{4,1},{2,2},{1,3}};

    public InvasionEvent(Island island, Location center) { super(island, center, WorldEventType.INVASION); }

    @Override public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin=plugin;
        broadcast("§c👹 §f§lĐảo của bạn đang bị §c§lXÂM LƯỢC§f! Hãy bảo vệ nó!");
        countdown("§cQuân xâm lược đang đến...", ()->spawnWave(0,onFinish));
    }

    private void spawnWave(int idx, Runnable onFinish) {
        World world=center.getWorld();
        if(idx>=WAVES.length){
            world.dropItemNaturally(center.clone().add(0,1,0),named(Material.IRON_INGOT,"§c§lCúp Bảo Vệ Đảo"));
            world.dropItemNaturally(center.clone().add(0,1,0),named(Material.GOLD_NUGGET,"§6§lXu Chiến Lợi Phẩm",5));
            if(hasLootBonus()){world.dropItemNaturally(center.clone().add(0,1,0),named(Material.DIAMOND,"§c§lKim Cương Chỉ Huy",3));
                broadcast("§c👹 §lPhần thưởng đặc biệt! §r§cKim Cương Chỉ Huy đã rơi!");}
            broadcast("§a👹 Quân xâm lược đã bị đẩy lui! §cCúp Bảo Vệ Đảo §ađã rơi!");
            sound(center,1f,1f,"LEVEL_UP","ENTITY_PLAYER_LEVELUP");
            logResult("HOÀN THÀNH"); onFinish.run(); return;
        }

        broadcast("§c👹 §fĐợt §e"+(idx+1)+"§c/§e"+WAVES.length+" §fquân tấn công ào đến!");
        sound(center,0.7f,0.8f,"ENDERDRAGON_WINGS","ENTITY_ENDER_DRAGON_FLAP");

        List<LivingEntity> waveMobs=new ArrayList<>();
        for(int i=0;i<WAVES[idx][0];i++){
            double a=rng.nextDouble()*Math.PI*2;
            Location loc=center.clone().add(Math.cos(a)*10,1,Math.sin(a)*10);
            loc.setY(world.getHighestBlockYAt(loc)+1);
            LivingEntity mob=spawnMob(world,loc,WAVES[idx][1],idx);
            if(mob!=null){
                waveMobs.add(mob);
                if(WAVES[idx][1]==3) trackHPBar(mob,"§4⚔ Tướng Tổng Chỉ Huy");
            }
        }

        new BukkitRunnable(){int e=0; @Override public void run(){
            e+=20;
            if(waveMobs.stream().noneMatch(Entity::isValid)){
                cancel(); broadcast("§a👹 Đợt "+(idx+1)+" đã bị tiêu diệt!");
                plugin.getServer().getScheduler().runTaskLater(plugin,()->spawnWave(idx+1,onFinish),60L);
                return;
            }

            // Wave 2 Arrow Rain (idx == 1)
            if (idx == 1 && e % 100 == 0) {
                boolean skeletonsAlive = waveMobs.stream().anyMatch(m -> m instanceof Skeleton && m.isValid());
                if (skeletonsAlive) {
                    for (Player p : getOnlinePlayers()) {
                        Location pLoc = p.getLocation();
                        p.sendMessage("§c🎯 Mưa tên từ trên trời đang rơi xuống vị trí của bạn!");
                        sound(pLoc, 0.8f, 1.2f, "SHOOT_ARROW", "ENTITY_ARROW_SHOOT");
                        for (int i = 0; i < 3; i++) {
                            Location arrowLoc = pLoc.clone().add((rng.nextDouble() - 0.5) * 4, 12 + rng.nextDouble() * 4, (rng.nextDouble() - 0.5) * 4);
                            Arrow arrow = p.getWorld().spawnArrow(arrowLoc, new Vector(0, -1, 0), 1.2f, 12f);
                            arrow.setShooter(null);
                        }
                    }
                }
            }

            // Wave 4 Boss Slam (idx == 3)
            if (idx == 3 && e % 100 == 0) {
                for (LivingEntity m : waveMobs) {
                    if (m instanceof IronGolem && m.isValid()) {
                        Location gLoc = m.getLocation();
                        broadcast("§4§l⚔ Tướng Chỉ Huy đang tụ lực đập đất!");
                        fx(gLoc.clone().add(0, 0.5, 0), 10, "MOBSPAWNER_FLAMES");
                        sound(gLoc, 1f, 0.5f, "BURNING", "BLOCK_FIRE_AMBIENT");
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            if (m.isValid()) {
                                sound(gLoc, 1.2f, 0.6f, "ANVIL_LAND", "BLOCK_ANVIL_LAND", "EXPLODE");
                                fx(gLoc, 5, "EXPLOSION_LARGE");
                                for (Player p : getOnlinePlayers()) {
                                    if (p.getWorld().equals(gLoc.getWorld())) {
                                        double dist = p.getLocation().distance(gLoc);
                                        if (dist < 8.0) {
                                            p.damage(4.0);
                                            Vector knock = p.getLocation().toVector().subtract(gLoc.toVector());
                                            knock.setY(0);
                                            if (knock.lengthSquared() > 0) {
                                                knock.normalize().multiply(0.8);
                                            }
                                            knock.setY(0.4);
                                            p.setVelocity(knock);
                                            p.sendMessage("§c§l⚔ Bạn bị chấn động bởi cú đập đất của Tướng Chỉ Huy!");
                                        }
                                    }
                                }
                            }
                        }, 20L);
                        break;
                    }
                }
            }

            if(e>=20*60*3){cancel();waveMobs.forEach(m->{if(m.isValid())m.remove();});
                broadcast("§c👹 Đảo đã bị quân xâm lược tràn ngập..."); logResult("THẤT BẠI"); onFinish.run();}
        }}.runTaskTimer(plugin,20L,20L);
    }

    private LivingEntity spawnMob(World world,Location loc,int type,int wave){
        double scale=scaledHP(1.0);
        switch(type){
            case 0:{Zombie z=(Zombie)world.spawnEntity(loc,EntityType.ZOMBIE);
                z.setCustomName("§cQuân Xâm Lược §7[Đợt "+(wave+1)+"]");z.setCustomNameVisible(true);
                z.setMaxHealth((30+wave*5)*scale);z.setHealth(z.getMaxHealth());return z;}
            case 1:{Skeleton s=(Skeleton)world.spawnEntity(loc,EntityType.SKELETON);
                s.setCustomName("§cCung Thủ Bóng Tối §7[Đợt "+(wave+1)+"]");s.setCustomNameVisible(true);
                s.setMaxHealth((25+wave*5)*scale);s.setHealth(s.getMaxHealth());return s;}
            case 2:{PigZombie v=(PigZombie)world.spawnEntity(loc,EntityType.PIG_ZOMBIE);
                v.setCustomName("§4§lTinh Nhuệ Chiến Binh");v.setCustomNameVisible(true);
                v.setMaxHealth(80*scale);v.setHealth(v.getMaxHealth());v.setAngry(true);
                v.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,Integer.MAX_VALUE,1,false,false));return v;}
            case 3:{IronGolem r=(IronGolem)world.spawnEntity(loc,EntityType.IRON_GOLEM);
                r.setCustomName("§4§l⚔ Tướng Tổng Chỉ Huy");r.setCustomNameVisible(true);
                r.setMaxHealth(200*scale);r.setHealth(r.getMaxHealth());return r;}
            default:return null;
        }
    }
}
