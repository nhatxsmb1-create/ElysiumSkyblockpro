package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*; import org.bukkit.entity.*; import org.bukkit.potion.*;
import org.bukkit.scheduler.BukkitRunnable; import org.bukkit.util.Vector;

public class VolcanoEvent extends IslandWorldEvent {
    private static final int DURATION=20*60*4, RADIUS=60;

    public VolcanoEvent(Island island, Location center) { super(island, center, WorldEventType.VOLCANO); }

    @Override public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin=plugin;
        broadcast("§c🌋 §f§lNúi Lửa §fbùng nổ! Tro bụi bắt đầu rơi xuống đảo!");
        broadcast("§7Bầu trời chuyển đỏ... Quái vật lửa đang thức dậy!");
        countdown("§cNúi lửa đang phun trào...", ()->spawnBoss(onFinish));
    }

    private void spawnBoss(Runnable onFinish) {
        World world=center.getWorld();
        BukkitRunnable ash=new BukkitRunnable(){int e=0; @Override public void run(){
            e+=4; if(e>=DURATION){cancel();return;}
            for(int i=0;i<6;i++){Location p=center.clone().add((rng.nextDouble()-.5)*RADIUS,18+rng.nextInt(8),(rng.nextDouble()-.5)*RADIUS);
                fx(p,1,"LARGE_SMOKE"); fx(p,1,"FLAME");}
        }};
        ash.runTaskTimer(plugin,0L,4L);

        BukkitRunnable meteors=new BukkitRunnable(){int e=0; @Override public void run(){
            e+=100; if(e>=DURATION){cancel();return;}
            Location from=center.clone().add((rng.nextDouble()-.5)*RADIUS,40,(rng.nextDouble()-.5)*RADIUS);
            Fireball fb=(Fireball)world.spawnEntity(from,EntityType.FIREBALL);
            fb.setDirection(new Vector(0,-1,0)); fb.setYield(1.5f); fb.setIsIncendiary(false);
            sound(from,0.5f,0.6f,"FIREWORK_LAUNCH","ENTITY_FIREWORK_ROCKET_LAUNCH");
        }};
        meteors.runTaskTimer(plugin,40L,100L);

        Blaze boss=(Blaze)world.spawnEntity(center.clone().add(0,2,0),EntityType.BLAZE);
        boss.setCustomName("§c🌋 Golem Lửa"); boss.setCustomNameVisible(true);
        double hp=scaledHP(180.0); boss.setMaxHealth(hp); boss.setHealth(hp);
        boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,Integer.MAX_VALUE,1,false,false));
        trackHPBar(boss,"§c🌋 Golem Lửa");

        BukkitRunnable volcanicHeat = new BukkitRunnable() {
            int e = 0;
            @Override public void run() {
                e += 40; if (e >= DURATION || !boss.isValid()) { cancel(); return; }
                Location bossLoc = boss.getLocation();
                for (Player p : getOnlinePlayers()) {
                    if (p.getWorld().equals(bossLoc.getWorld())) {
                        double dist = p.getLocation().distance(bossLoc);
                        if (dist < 15.0) {
                            p.sendMessage("§c§l🌋 Hơi nóng núi lửa thiêu đốt bạn!");
                            p.setFireTicks(40);
                            p.damage(1.0);
                            fx(p.getLocation(), 3, "FLAME");
                            sound(p.getLocation(), 0.5f, 0.5f, "FIZZ", "BLOCK_FIRE_EXTINGUISH");
                        }
                        if (dist < 30.0 && rng.nextDouble() < 0.4) {
                            Location playerLoc = p.getLocation().clone();
                            p.sendMessage("§c🔥 Mặt đất dưới chân đang nóng lên!");
                            fx(playerLoc, 5, "MOBSPAWNER_FLAMES");
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                if (boss.isValid() && p.isOnline()) {
                                    sound(playerLoc, 0.8f, 0.8f, "EXPLODE", "ENTITY_GENERIC_EXPLODE");
                                    fx(playerLoc, 10, "LAVA", "LAVADRIP");
                                    if (p.getLocation().distance(playerLoc) < 2.0) {
                                        p.damage(4.0);
                                        p.setFireTicks(80);
                                        p.setVelocity(new Vector(0, 0.35, 0));
                                    }
                                }
                            }, 20L);
                        }
                    }
                }
            }
        };
        volcanicHeat.runTaskTimer(plugin, 0L, 40L);

        new BukkitRunnable(){int e=0; @Override public void run(){
            e+=20;
            if(!boss.isValid()){
                cancel(); ash.cancel(); meteors.cancel(); volcanicHeat.cancel();
                Location d=boss.getLocation();
                world.dropItemNaturally(d,named(Material.MAGMA_CREAM,"§c§lTinh Thể Dung Nham"));
                world.dropItemNaturally(d,named(Material.BLAZE_ROD,"§6§lLõi Nham Thạch"));
                world.dropItemNaturally(d,named(Material.NETHERRACK,"§4§lQuặng Núi Lửa"));
                if(hasLootBonus()){world.dropItemNaturally(d,named(Material.NETHER_STAR,"§c§lBảo Ngọc Địa Ngục"));
                    broadcast("§6🌋 §lPhần thưởng đặc biệt! §r§6Bảo Ngọc Địa Ngục đã rơi!");}
                broadcast("§a🌋 Golem Lửa đã bị tiêu diệt! Chiến lợi phẩm núi lửa đã rơi!");
                sound(center,1f,0.8f,"ENDERDRAGON_DEATH","ENTITY_ENDER_DRAGON_DEATH");
                logResult("HOÀN THÀNH"); onFinish.run(); return;
            }
            if(e>=DURATION){cancel();ash.cancel();meteors.cancel();volcanicHeat.cancel();boss.remove();
                broadcast("§c🌋 Núi lửa đã nguội dần..."); logResult("HẾT GIỜ"); onFinish.run();}
        }}.runTaskTimer(plugin,20L,20L);
    }
}
