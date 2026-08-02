package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 🌳 ANCIENT TREE EVENT
 * - Giant leaf/vine particle "tree" appears at island center
 * - Spawns a Dryad (Witch) with regeneration + nature theme
 * - Killing Dryad drops Nature Essence + Woodland Seed
 */
public class AncientTreeEvent extends IslandWorldEvent {

    public AncientTreeEvent(Island island, Location center) {
        super(island, center);
    }

    @Override
    public void start(SuperiorSkyblockPlugin plugin, Runnable onFinish) {
        broadcast("§2🌳 §fAn §2Ancient Tree §fhas grown on your island! A §aDryad §fguards it!");
        World world = center.getWorld();

        // Grow "tree" visually with particles
        BukkitRunnable treeAura = new BukkitRunnable() {
            double angle = 0;
            int elapsed  = 0;
            @Override public void run() {
                elapsed += 3;
                if (elapsed > 20 * 60 * 5) { cancel(); return; }
                angle += 8;
                for (int layer = 0; layer < 6; layer++) {
                    double yOff = layer * 1.5;
                    double r    = Math.max(0.5, 3.0 - layer * 0.4);
                    double a    = Math.toRadians(angle + layer * 30);
                    Location p  = center.clone().add(Math.cos(a) * r, yOff, Math.sin(a) * r);
                    world.spawnParticle(Particle.VILLAGER_HAPPY, p, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.COMPOSTER,      p, 1, 0.1, 0.1, 0.1, 0);
                }
            }
        };
        treeAura.runTaskTimer(plugin, 0L, 3L);

        // Spawn Dryad (Witch)
        Location bossLoc = center.clone().add(0, 1, 0);
        Witch dryad = (Witch) world.spawnEntity(bossLoc, EntityType.WITCH);
        dryad.setCustomName("§a🌿 Ancient Dryad");
        dryad.setCustomNameVisible(true);
        dryad.setMaxHealth(100.0);
        dryad.setHealth(100.0);
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
        dryad.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,         Integer.MAX_VALUE, 0, false, false));

        world.playSound(center, Sound.BLOCK_GRASS_PLACE, 1f, 0.6f);

        new BukkitRunnable() {
            int elapsed = 0;
            @Override public void run() {
                elapsed += 20;
                if (!dryad.isValid()) {
                    cancel();
                    treeAura.cancel();
                    Location dropLoc = dryad.getLocation();
                    world.dropItemNaturally(dropLoc, named(Material.VINE,          "§a§lNature Essence"));
                    world.dropItemNaturally(dropLoc, named(Material.OAK_SAPLING,   "§2§lWoodland Seed"));
                    world.dropItemNaturally(dropLoc, named(Material.GREEN_DYE,     "§a§lForest Dust"));
                    broadcast("§a🌳 Ancient Dryad defeated! Nature Essence has dropped!");
                    world.playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.6f);
                    onFinish.run();
                    return;
                }
                if (elapsed >= 20 * 60 * 5) {
                    cancel();
                    treeAura.cancel();
                    dryad.remove();
                    broadcast("§c🌳 The Ancient Tree withered... Dryad vanished.");
                    onFinish.run();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private ItemStack named(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }
}
