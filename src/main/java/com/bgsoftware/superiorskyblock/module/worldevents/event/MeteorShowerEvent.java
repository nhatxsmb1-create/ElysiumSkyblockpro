package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import org.bukkit.*; import org.bukkit.entity.*; import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

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
        broadcast("§e☄ Phát hiện dấu hiệu thiên thạch sắp rơi xuống đảo!");

        // 1. Mark target on the ground for 60 ticks (3 seconds)
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                ticks += 5;
                if (ticks >= 60 || !ground.getChunk().isLoaded()) {
                    cancel();
                    // 2. Spawn physical meteor falling down
                    spawnPhysicalMeteor(world, ground);
                    return;
                }

                sound(ground, 0.8f, 0.5f + (ticks / 60.0f) * 0.5f, "NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING");

                double radius = 3.0;
                for (int i = 0; i < 16; i++) {
                    double angle = (i * (Math.PI * 2 / 16)) + (ticks * 0.1);
                    Location p = ground.clone().add(Math.cos(angle) * radius, 0.2, Math.sin(angle) * radius);
                    fx(p, 1, "MOBSPAWNER_FLAMES", "FLAME");
                    if (rng.nextBoolean()) fx(p, 1, "LAVADRIP");
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void spawnPhysicalMeteor(World world, Location ground) {
        Location spawnLoc = ground.clone().add(0, 40, 0);
        FallingBlock meteor;
        try {
            meteor = world.spawnFallingBlock(spawnLoc, Material.valueOf("MAGMA"), (byte) 0);
        } catch (Exception e) {
            meteor = world.spawnFallingBlock(spawnLoc, Material.NETHERRACK, (byte) 0);
        }

        meteor.setDropItem(false);
        final FallingBlock finalMeteor = meteor;
        sound(spawnLoc, 1f, 0.4f, "FIREWORK_LAUNCH", "ENTITY_FIREWORK_ROCKET_LAUNCH");

        new BukkitRunnable() {
            @Override public void run() {
                if (!finalMeteor.isValid() || finalMeteor.isOnGround() || finalMeteor.getLocation().getY() <= ground.getY()) {
                    cancel();
                    finalMeteor.remove();
                    impact(world, ground);
                    return;
                }

                Location loc = finalMeteor.getLocation();
                fx(loc, 3, "FLAME");
                fx(loc, 2, "LARGE_SMOKE");
                if (rng.nextBoolean()) fx(loc, 1, "LAVADRIP");

                if (rng.nextDouble() < 0.2) {
                    sound(loc, 0.5f, 0.6f, "GHAST_FIREBALL", "ENTITY_GHAST_SHOOT");
                }

                for (Player p : getOnlinePlayers()) {
                    if (p.getWorld().equals(loc.getWorld()) && p.getLocation().distance(loc) < 2.0) {
                        p.damage(6.0);
                        p.setFireTicks(60);
                        p.setVelocity(p.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.5).setY(0.3));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
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
