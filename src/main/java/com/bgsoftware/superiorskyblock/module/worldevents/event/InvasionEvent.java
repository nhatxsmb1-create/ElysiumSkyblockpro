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

/**
 * 👹 INVASION EVENT
 * Wave 1  – 4 Zombies
 * Wave 2  – 4 Skeletons
 * Wave 3  – 2 Vindicators (Elites)
 * Wave 4  – 1 Warden-style boss (Ravager + potion)
 * Clear all waves → Raid Trophy + Invasion Coin drops
 */
public class InvasionEvent extends IslandWorldEvent {

    private static final int[][] WAVE_DEFS = {
            {4, 0}, // wave1: 4 zombies
            {4, 1}, // wave2: 4 skeletons
            {2, 2}, // wave3: 2 vindicators (elites)
            {1, 3}  // wave4: 1 boss
    };

    public InvasionEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        broadcast("§c👹 §fYour island is under §c§lINVASION§f! Defend it!");
        World world = center.getWorld();
        Random rng  = new Random();
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0.5f);

        spawnWave(plugin, world, rng, 0, onFinish);
    }

    private void spawnWave(SuperiorSkyblockPlugin plugin, World world, Random rng,
                           int waveIdx, Runnable onFinish) {
        if (waveIdx >= WAVE_DEFS.length) {
            // All waves cleared
            Location dropLoc = center.clone().add(0, 1, 0);
            world.dropItemNaturally(dropLoc, named(Material.SHIELD,      "§c§lRaid Trophy"));
            world.dropItemNaturally(dropLoc, named(Material.GOLD_NUGGET, "§6§lInvasion Coin", 5));
            broadcast("§a👹 Invasion repelled! §cRaid Trophy §ahas dropped!");
            world.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            onFinish.run();
            return;
        }

        int[] def      = WAVE_DEFS[waveIdx];
        int mobCount   = def[0];
        int mobType    = def[1];

        broadcast("§c👹 Wave §e" + (waveIdx + 1) + "§c/§e" + WAVE_DEFS.length + " §carrives!");
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.7f, 0.8f);

        List<LivingEntity> waveMobs = new ArrayList<>();
        for (int i = 0; i < mobCount; i++) {
            double a = rng.nextDouble() * Math.PI * 2;
            double r = 8 + rng.nextDouble() * 5;
            Location spawnLoc = center.clone().add(Math.cos(a) * r, 1, Math.sin(a) * r);
            spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);
            LivingEntity mob = spawnMob(world, spawnLoc, mobType, waveIdx);
            if (mob != null) waveMobs.add(mob);
        }

        // Poll for wave clear
        new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 20;
                boolean allDead = waveMobs.stream().noneMatch(Entity::isValid);
                if (allDead) {
                    cancel();
                    broadcast("§a👹 Wave " + (waveIdx + 1) + " cleared!");
                    // Short pause then next wave
                    plugin.getServer().getScheduler().runTaskLater(plugin,
                            () -> spawnWave(plugin, world, rng, waveIdx + 1, onFinish),
                            60L); // 3 second break between waves
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
            case 0: { // Zombie
                Zombie z = (Zombie) world.spawnEntity(loc, EntityType.ZOMBIE);
                z.setCustomName("§cInvader §7[W" + (wave+1) + "]");
                z.setCustomNameVisible(true);
                z.setMaxHealth(30.0 + wave * 5); z.setHealth(z.getMaxHealth());
                return z;
            }
            case 1: { // Skeleton
                Skeleton s = (Skeleton) world.spawnEntity(loc, EntityType.SKELETON);
                s.setCustomName("§cSkeletal Archer §7[W" + (wave+1) + "]");
                s.setCustomNameVisible(true);
                s.setMaxHealth(25.0 + wave * 5); s.setHealth(s.getMaxHealth());
                return s;
            }
            case 2: { // Vindicator Elite
                Vindicator v = (Vindicator) world.spawnEntity(loc, EntityType.VINDICATOR);
                v.setCustomName("§4§lElite Raider");
                v.setCustomNameVisible(true);
                v.setMaxHealth(80.0); v.setHealth(80.0);
                v.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
                return v;
            }
            case 3: { // Boss — Ravager
                Ravager boss = (Ravager) world.spawnEntity(loc, EntityType.RAVAGER);
                boss.setCustomName("§4§l⚔ Siege Commander");
                boss.setCustomNameVisible(true);
                boss.setMaxHealth(200.0); boss.setHealth(200.0);
                return boss;
            }
            default: return null;
        }
    }

    private ItemStack named(Material mat, String name) {
        return named(mat, name, 1);
    }

    private ItemStack named(Material mat, String name, int amount) {
        ItemStack item = new ItemStack(mat, amount);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }
}
