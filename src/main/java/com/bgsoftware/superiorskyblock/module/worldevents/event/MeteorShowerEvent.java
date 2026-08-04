package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*; import org.bukkit.entity.*; import org.bukkit.scheduler.BukkitRunnable;

public class MeteorShowerEvent extends IslandWorldEvent {
    private static final int COUNT=5, RADIUS=55, PICKUP_S=15;

    public MeteorShowerEvent(Island island, Location center) { super(island, center, WorldEventType.METEOR_SHOWER); }

    @Override public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin=plugin;
        broadcast("§e☄ §f§lMưa Thiên Thạch §fđang đến! Hãy quan sát bầu trời và chạy đến điểm rơi!");
        countdown("§eThiên thạch chuẩn bị rơi...", ()->shower(onFinish));
    }

    private void shower(Runnable onFinish) {
        World world=center.getWorld();
        new BukkitRunnable(){int n=0; @Override public void run(){
            if(n>=COUNT){cancel();broadcast("§e☄ Mưa Thiên Thạch đã kết thúc.");logResult("HOÀN THÀNH");onFinish.run();return;}
            n++;
            Location impact=center.clone().add((rng.nextDouble()-.5)*RADIUS,0,(rng.nextDouble()-.5)*RADIUS);
            impact.setY(world.getHighestBlockYAt(impact));
            dropMeteor(world,impact);
        }}.runTaskTimer(plugin,40L,20*20L);
    }

    private void dropMeteor(World world, Location ground) {
        broadcast("§e☄ Một thiên thạch đang rơi xuống! Chuẩn bị!");
        sound(ground.clone().add(0,50,0),1f,0.4f,"FIREWORK_LAUNCH","ENTITY_FIREWORK_ROCKET_LAUNCH");
        new BukkitRunnable(){double y=50; @Override public void run(){
            y-=3; Location cur=ground.clone().add(0,y,0);
            fx(cur,2,"FLAME"); fx(cur,1,"LAVADRIP");
            if(y<=0){cancel();impact(world,ground);}
        }}.runTaskTimer(plugin,0L,1L);
    }

    private void impact(World world, Location loc) {
        sound(loc,1f,0.7f,"EXPLODE","ENTITY_GENERIC_EXPLODE");
        fx(loc,3,"EXPLOSION_LARGE"); fx(loc,8,"LAVADRIP");
        broadcast("§6☄ Thiên thạch đã rơi tại §e("+loc.getBlockX()+", "+loc.getBlockZ()+")§6! Đến nhặt trong §c"+PICKUP_S+" giây§6!");

        Material[] opts={Material.DIAMOND,Material.EMERALD,Material.GOLD_INGOT,Material.IRON_INGOT};
        Material mat=opts[rng.nextInt(opts.length)];
        int qty=hasLootBonus()?3+rng.nextInt(3):1+rng.nextInt(3);
        Item lootItem=world.dropItem(loc.clone().add(0,1,0),named(mat,"§6§lQuặng Thiên Thạch ("+mat.name()+")",qty));
        lootItem.setPickupDelay(0);

        if(rng.nextInt(100)<20){
            Zombie mini=(Zombie)world.spawnEntity(loc,EntityType.ZOMBIE);
            mini.setCustomName("§6Golem Thiên Thạch"); mini.setCustomNameVisible(true);
            double hp=scaledHP(80.0); mini.setMaxHealth(hp); mini.setHealth(hp);
            trackHPBar(mini,"§6Golem Thiên Thạch");
        }
        plugin.getServer().getScheduler().runTaskLater(plugin,()->{
            if(lootItem.isValid()){lootItem.remove();broadcast("§c☄ Chiến lợi phẩm thiên thạch đã tan biến...");}
        },PICKUP_S*20L);
    }
}
