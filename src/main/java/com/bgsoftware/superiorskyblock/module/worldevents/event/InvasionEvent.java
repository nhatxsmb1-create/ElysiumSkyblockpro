package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InvasionEvent extends IslandWorldEvent {

    // wave format: {mob count, mob type}
    // types: 0=Zombie, 1=Skeleton, 2=PigZombie(Elite), 3=IronGolem(Boss)
    private static final int[][] WAVE_DEFS = {{4, 0}, {4, 1}, {2, 2}, {1, 3}};

    public InvasionEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        this.plugin = plugin;
        broadcast("§c👹 §fYour island is under §c§lINVASION§f! Defend it!");
        sound(center, 1f, 0.5f, "ENDERDRAGON_GROWL", "ENTITY_ENDER_DRAGON_GROWL");
        spawnWave(plugin, 0, onFinish);
    }

    private void spawnWave(SuperiorSkyblockPlugin plugin, int waveIdx, Runnable onFinish) {
        World world = center.getWorld();
        Random rng  = new Random();

        if (waveIdx >= WAVE_DEFS.length) {
            Location drop = center.clone().add(0, 1, 0);
            world.dropItemNaturally(drop, named(Material.IRON_INGOT, "§c§lRaid Trophy"));
            world.dropItemNaturally(drop, named(Material.GOLD_NUGGET, "§6§lInvasion Coin", 5));
            broadcast("§a👹 Invasion repelled! §cRaid Trophy §ahas dropped!");
            sound(center, 1f, 1f, "LEVEL_UP", "ENTITY_PLAYER_LEVELUP");
            onFinish.run();
            return;
        }

        int[] def    = WAVE_DEFS[waveIdx];
        int mobCount = def[0];
        int mobType  = def[1];

        broadcast("§c👹 Wave §e" + (waveIdx + 1) + "§c/§e" + WAVE_DEFS.length + " §carrives!");
        sound(center, 0.7f, 0.8f, "ENDERDRAGON_WINGS", "ENTITY_ENDER_DRAGON_FLAP");

        List<LivingEntity> waveMobs = new ArrayList<>();
        for (int i = 0; i < mobCount; i++) {
            double a = rng.nextDouble() * Math.PI * 2;
            double r = 8 + rng.nextDouble() * 5;
            Location loc = center.clone().add(Math.cos(a) * r, 1, Math.sin(a) * r);
            loc.setY(world.getHighestBlockYAt(loc) + 1);
            LivingEntity mob = spawnMob(world, loc, mobType, waveIdx);
            if (mob != null) waveMobs.add(mob);
        }

        new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 20;
                if (waveMobs.stream().noneMatch(Entity::isValid)) {
                    cancel();
                    broadcast("§a👹 Wave " + (waveIdx + 1) + " cleared!");
                    plugin.getServer().getScheduler().runTaskLater(plugin,
                            () -> spawnWave(plugin, waveIdx + 1, onFinish), 60L);
                    return;
                }
                if (elapsed >= 20 * 60 * 3) {
                    cancel();
                    waveMobs.forEach(m -> { if (m.isValid()) m.remove(); });
                    broadcast("§c👹 Invasion overwhelmed your island...");
                    onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private LivingEntity spawnMob(World world, Location loc, int type, int wave) {
        switch (type) {
            case 0: {
                Zombie z = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
                z.setCustomName("§cInvader §7[W" + (wave+1) + "]");
                z.setCustomNameVisible(true);
                z.setMaxHealth(30.0 + wave * 5); z.setHealth(z.getMaxHealth());
                return z;
            }
            case 1: {
                Skeleton s = (Skeleton) world.spawnEntity(loc, EntityType.SKELETON);
                s.setCustomName("§cSkeletal Archer §7[W" + (wave+1) + "]");
                s.setCustomNameVisible(true);
                s.setMaxHealth(25.0 + wave * 5); s.setHealth(s.getMaxHealth());
                return s;
            }
            case 2: {
                // PigZombie as Elite (exists in 1.8.8, aggressive by default when attacked)
                PigZombie elite = (PigZombie) world.spawnEntity(loc, EntityType.PIG_ZOMBIE);
                elite.setCustomName("§4§lElite Raider");
                elite.setCustomNameVisible(true);
                elite.setMaxHealth(80.0); elite.setHealth(80.0);
                elite.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
                elite.setAngry(true);
                return elite;
            }
            case 3: {
                // IronGolem as boss (exists in 1.8.8)
                IronGolem boss = (IronGolem) world.spawnEntity(loc, EntityType.IRON_GOLEM);
                boss.setCustomName("§4§l⚔ Siege Commander");
                boss.setCustomNameVisible(true);
                boss.setMaxHealth(200.0); boss.setHealth(200.0);
                return boss;
            }
            default: return null;
        }
    }

    private ItemStack named(Material mat, String name) { return named(mat, name, 1); }
    private ItemStack named(Material mat, String name, int amount) {
        ItemStack item = new ItemStack(mat, amount);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }
}
