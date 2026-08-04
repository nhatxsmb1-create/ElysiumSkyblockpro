package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventScheduler;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class IslandWorldEvent {

    protected final Island island;
    protected final Location center;
    protected final WorldEventType eventType;
    protected SuperiorSkyblockPlugin plugin;
    protected final Random rng = new Random();

    protected IslandWorldEvent(Island island, Location center, WorldEventType eventType) {
        this.island    = island;
        this.center    = center.clone();
        this.eventType = eventType;
    }

    public abstract void start(SuperiorSkyblockPlugin plugin, Runnable onFinish);

    // ── Scaling ──────────────────────────────────────────────

    /** Boss HP scaled by island level + current instability */
    protected double scaledHP(double base) {
        WorldEventsModule module = getModule();
        int instability = module.getInstabilityManager().getInstability(island.getUniqueId());
        double level = island.getIslandLevel().doubleValue();
        double levelMult = Math.min(level * module.getConfiguration().getBossHPPerLevel(), 1.0);
        double instabilityMult = instability / 100.0 * 0.3; // max +30% at 100%
        return base * (1.0 + levelMult + instabilityMult);
    }

    /** Returns true when instability is high enough AND random roll passes */
    protected boolean hasLootBonus() {
        WorldEventsModule module = getModule();
        int instability = module.getInstabilityManager().getInstability(island.getUniqueId());
        return instability >= module.getConfiguration().getBonusLootThreshold()
                && rng.nextDouble() < module.getConfiguration().getBonusLootChance();
    }

    // ── Countdown ─────────────────────────────────────────────

    protected void countdown(String subtitle, Runnable onComplete) {
        int secs = getModule().getConfiguration().getCountdownSeconds();
        new BukkitRunnable() {
            int remaining = secs;
            @Override public void run() {
                if (remaining <= 0) { cancel(); onComplete.run(); return; }
                String color = remaining <= 3 ? "§c" : "§e";
                broadcastTitle(color + remaining, subtitle);
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // ── HP Bar (action bar) ───────────────────────────────────

    /** Shows a live HP bar in the action bar and auto-stops when boss dies. */
    protected void trackHPBar(LivingEntity boss, String bossName) {
        new BukkitRunnable() {
            @Override public void run() {
                if (!boss.isValid()) { cancel(); return; }
                double pct = boss.getHealth() / boss.getMaxHealth();
                int filled  = (int)(pct * 20);
                String color = pct > 0.6 ? "§a" : pct > 0.3 ? "§e" : "§c";

                // Build bar without String.repeat() for Java 8 compatibility
                int filledCount = Math.max(0, filled);
                int emptyCount = Math.max(0, 20 - filledCount);
                StringBuilder filledBuilder = new StringBuilder(filledCount);
                for (int i = 0; i < filledCount; i++) filledBuilder.append('█');
                StringBuilder emptyBuilder = new StringBuilder(emptyCount);
                for (int i = 0; i < emptyCount; i++) emptyBuilder.append('█');
                String bar = color + filledBuilder.toString() + "§8" + emptyBuilder.toString();

                String msg   = bossName + " §f" + bar + " §7" + (int)(pct * 100) + "% HP";
                for (Player p : getOnlinePlayers())
                    plugin.getNMSPlayers().sendActionBar(p, msg);
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    // ── Logging ───────────────────────────────────────────────

    protected void logResult(String result) {
        WorldEventsModule module = getModule();
        int instability = module.getInstabilityManager().getInstability(island.getUniqueId());
        module.getWorldEventLogger().log(island, eventType, instability, result);
    }

    // ── Broadcast helpers ─────────────────────────────────────

    protected List<Player> getOnlinePlayers() {
        List<Player> list = new ArrayList<>();
        for (SuperiorPlayer sp : island.getIslandMembers(true))
            if (sp.isOnline() && sp.asPlayer() != null) list.add(sp.asPlayer());
        return list;
    }

    protected void broadcast(String msg) {
        getOnlinePlayers().forEach(p -> p.sendMessage("§d[World Event] §r" + msg));
    }

    protected void broadcastTitle(String title, String sub) {
        getOnlinePlayers().forEach(p ->
                plugin.getNMSPlayers().sendTitle(p, title, sub, 5, 30, 10));
    }

    protected void broadcastBar(String msg) {
        getOnlinePlayers().forEach(p ->
                plugin.getNMSPlayers().sendActionBar(p, msg));
    }

    /** Safe sound with fallback */
    protected void sound(Location loc, float vol, float pitch, String... names) {
        for (String name : names) {
            try { loc.getWorld().playSound(loc, Sound.valueOf(name), vol, pitch); return; }
            catch (Exception ignored) {}
        }
    }

    /** Safe particle via Effect enum (1.8 compatible) */
    protected void fx(Location loc, int count, String... effectNames) {
        for (String name : effectNames) {
            try { Effect eff = Effect.valueOf(name);
                for (int i = 0; i < count; i++) loc.getWorld().playEffect(loc, eff, 0);
                return;
            } catch (Exception ignored) {}
        }
    }

    protected ItemStack named(org.bukkit.Material mat, String name) { return named(mat, name, 1); }
    protected ItemStack named(org.bukkit.Material mat, String name, int amount) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }

    private WorldEventsModule getModule() {
        return (WorldEventsModule) plugin.getModules().getModule("worldevents");
    }
}
