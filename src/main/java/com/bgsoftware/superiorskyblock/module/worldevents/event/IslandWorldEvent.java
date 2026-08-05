package com.bgsoftware.superiorskyblock.module.worldevents.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventType;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class IslandWorldEvent {

    protected final Island island;
    protected final Location center;
    protected final WorldEventType eventType;
    protected SuperiorSkyblockPlugin plugin;
    protected final Random rng = new Random();

    // ── Particle reflection cache ─────────────────────────────
    private static boolean particleInitDone = false;
    private static Method spawnParticleMethod = null;
    private static Class<?> particleClass = null;

    protected IslandWorldEvent(Island island, Location center, WorldEventType eventType) {
        this.island    = island;
        this.center    = center.clone();
        this.eventType = eventType;
    }

    public abstract void start(SuperiorSkyblockPlugin plugin, Runnable onFinish);

    // ── Scaling ──────────────────────────────────────────────

    protected double scaledHP(double base) {
        WorldEventsModule module = getModule();
        int instability = module.getInstabilityManager().getInstability(island.getUniqueId());
        double level = island.getIslandLevel().doubleValue();
        double levelMult = Math.min(level * module.getConfiguration().getBossHPPerLevel(), 1.0);
        double instabilityMult = instability / 100.0 * 0.3;
        return base * (1.0 + levelMult + instabilityMult);
    }

    protected boolean hasLootBonus() {
        WorldEventsModule module = getModule();
        int instability = module.getInstabilityManager().getInstability(island.getUniqueId());
        return instability >= module.getConfiguration().getBonusLootThreshold()
                && rng.nextDouble() < module.getConfiguration().getBonusLootChance();
    }

    // ── Spawn near player ────────────────────────────────────

    protected Location getPlayerNearbySpawn(double range) {
        List<Player> online = getOnlinePlayers();
        if (online.isEmpty()) return center.clone();
        Player target = online.get(rng.nextInt(online.size()));
        Location base = target.getLocation().clone();
        double angle = rng.nextDouble() * Math.PI * 2;
        double dist  = 3.0 + rng.nextDouble() * Math.max(1.0, range - 3.0);
        base.add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
        
        int highestY = base.getWorld().getHighestBlockYAt(base);
        if (highestY < target.getLocation().getBlockY() - 5) {
            base.setY(target.getLocation().getY());
        } else {
            base.setY(highestY + 1);
        }
        return base;
    }

    protected Location getNearestPlayerLocation() {
        List<Player> online = getOnlinePlayers();
        if (online.isEmpty()) return center.clone();
        Player nearest = online.get(0);
        double best = Double.MAX_VALUE;
        for (Player p : online) {
            double d = p.getLocation().distanceSquared(center);
            if (d < best) { best = d; nearest = p; }
        }
        return nearest.getLocation().clone();
    }

    protected void targetNearestPlayer(LivingEntity entity) {
        if (!(entity instanceof Mob)) return;
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : getOnlinePlayers()) {
            if (!p.getWorld().equals(entity.getWorld())) continue;
            double d = p.getLocation().distanceSquared(entity.getLocation());
            if (d < minDist) { minDist = d; nearest = p; }
        }
        if (nearest != null) ((Mob) entity).setTarget(nearest);
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

    // ── HP Bar via BossBar (top of screen) ───────────────────

    /**
     * Shows the boss HP bar at the TOP of the screen using the BossBar API natively.
     * Handles fallback to action bar if BossBar API is completely missing.
     */
    protected void trackHPBar(LivingEntity boss, String bossName) {
        try {
            // Native BossBar API call (clean, robust, displays at the top of the screen)
            final BossBar bar = Bukkit.createBossBar(bossName, BarColor.GREEN, BarStyle.SOLID);
            bar.setVisible(true);
            
            for (Player p : getOnlinePlayers()) {
                bar.addPlayer(p);
            }

            new BukkitRunnable() {
                @Override public void run() {
                    if (!boss.isValid()) {
                        cancel();
                        bar.removeAll();
                        bar.setVisible(false);
                        return;
                    }
                    double pct = Math.max(0.0, Math.min(1.0, boss.getHealth() / boss.getMaxHealth()));
                    bar.setProgress(pct);
                    
                    if (pct > 0.6) {
                        bar.setColor(BarColor.GREEN);
                    } else if (pct > 0.3) {
                        bar.setColor(BarColor.YELLOW);
                    } else {
                        bar.setColor(BarColor.RED);
                    }
                    
                    bar.setTitle(bossName + " §f- " + (int)(pct * 100) + "% HP");
                    
                    // Maintain player list
                    List<Player> current = getOnlinePlayers();
                    for (Player p : current) {
                        if (!bar.getPlayers().contains(p)) {
                            bar.addPlayer(p);
                        }
                    }
                    // Remove players who left
                    List<Player> copy = new ArrayList<>(bar.getPlayers());
                    for (Player p : copy) {
                        if (!current.contains(p)) {
                            bar.removePlayer(p);
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 10L);

        } catch (Throwable t) {
            // Action bar fallback if BossBar is not supported
            new BukkitRunnable() {
                @Override public void run() {
                    if (!boss.isValid()) { cancel(); return; }
                    double pct = boss.getHealth() / boss.getMaxHealth();
                    int filled  = (int)(pct * 20);
                    String color = pct > 0.6 ? "§a" : pct > 0.3 ? "§e" : "§c";
                    StringBuilder f = new StringBuilder();
                    StringBuilder e = new StringBuilder();
                    for (int i = 0; i < Math.max(0, filled); i++) f.append('█');
                    for (int i = 0; i < Math.max(0, 20 - filled); i++) e.append('█');
                    String msg = bossName + " §f" + color + f + "§8" + e + " §7" + (int)(pct * 100) + "%";
                    for (Player p : getOnlinePlayers())
                        plugin.getNMSPlayers().sendActionBar(p, msg);
                }
            }.runTaskTimer(plugin, 0L, 10L);
        }
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

    // ── Sound ─────────────────────────────────────────────────

    protected void sound(Location loc, float vol, float pitch, String... names) {
        for (String name : names) {
            try { loc.getWorld().playSound(loc, Sound.valueOf(name), vol, pitch); return; }
            catch (Exception ignored) {}
        }
    }

    // ── Particles ─────────────────────────────────────────────

    protected void particle(Location loc, int count, String... names) {
        ensureParticleMethod(loc);
        for (String name : names) {
            if (trySpawnParticle(loc, count, name)) return;
        }
        fx(loc, count, names);
    }

    protected void fx(Location loc, int count, String... effectNames) {
        for (String name : effectNames) {
            try {
                Effect eff = Effect.valueOf(name);
                for (int i = 0; i < count; i++) loc.getWorld().playEffect(loc, eff, 0);
                return;
            } catch (Exception ignored) {}
        }
    }

    protected void lightningEffect(Location loc) {
        try {
            loc.getWorld().getClass()
                    .getMethod("strikeLightningEffect", Location.class)
                    .invoke(loc.getWorld(), loc);
        } catch (Exception ignored) {
            try { loc.getWorld().strikeLightning(loc); } catch (Exception ignored2) {}
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void ensureParticleMethod(Location loc) {
        if (particleInitDone) return;
        particleInitDone = true;
        try {
            particleClass = Class.forName("org.bukkit.Particle");
            try {
                spawnParticleMethod = org.bukkit.World.class.getMethod(
                        "spawnParticle", particleClass, Location.class, int.class);
            } catch (NoSuchMethodException ex) {
                spawnParticleMethod = null;
            }
        } catch (Exception ignored) {}
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean trySpawnParticle(Location loc, int count, String name) {
        if (particleClass == null) return false;
        try {
            Object particleObj = Enum.valueOf((Class<Enum>) particleClass, name);
            if (spawnParticleMethod != null) {
                spawnParticleMethod.invoke(loc.getWorld(), particleObj, loc, count);
                return true;
            }
            Method m = org.bukkit.World.class.getMethod(
                    "spawnParticle", particleClass,
                    double.class, double.class, double.class, int.class);
            m.invoke(loc.getWorld(), particleObj, loc.getX(), loc.getY(), loc.getZ(), count);
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    // ── Item helpers ──────────────────────────────────────────

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
