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
    // We try multiple World#spawnParticle overloads at first use.
    private static boolean particleInitDone = false;
    private static Method spawnParticleMethod = null;  // (Particle, Location, int)
    private static Class<?> particleClass = null;

    // ── BossBar reflection cache ──────────────────────────────
    private static boolean bossBarChecked = false;
    private static Method createBossBarMethod = null;
    private static Class<?> barColorClass = null;
    private static Class<?> barStyleClass = null;

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

    /**
     * Return a location on solid ground within [minDist, minDist+range] blocks
     * of a random online player. Falls back to island center if no players online.
     */
    protected Location getPlayerNearbySpawn(double range) {
        List<Player> online = getOnlinePlayers();
        if (online.isEmpty()) return center.clone();
        Player target = online.get(rng.nextInt(online.size()));
        Location base = target.getLocation().clone();
        double angle = rng.nextDouble() * Math.PI * 2;
        double dist  = 3.0 + rng.nextDouble() * Math.max(1.0, range - 3.0);
        base.add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
        base.setY(base.getWorld().getHighestBlockYAt(base) + 1);
        return base;
    }

    /** Return the nearest online player's location (for targeting impacts) */
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

    /** Make a mob immediately target the nearest player */
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
     * Shows the boss HP bar at the TOP of the screen using the BossBar API (1.9+).
     * Falls back to action bar on 1.8 servers.
     */
    protected void trackHPBar(LivingEntity boss, String bossName) {
        // Try to create a BossBar — appears at top of screen, doesn't clash with skill bar
        Object bossBar = tryCreateBossBar(bossName);

        if (bossBar != null) {
            // Add all current online players to the bar
            try {
                Method addPlayer = bossBar.getClass().getMethod("addPlayer", Player.class);
                for (Player p : getOnlinePlayers()) addPlayer.invoke(bossBar, p);
            } catch (Exception ignored) {}

            final Object bar = bossBar;
            new BukkitRunnable() {
                @Override public void run() {
                    if (!boss.isValid()) {
                        cancel();
                        removeBossBar(bar);
                        return;
                    }
                    double pct = Math.max(0, boss.getHealth() / boss.getMaxHealth());
                    updateBossBar(bar, bossName, pct);
                    // Ensure any newly-joined players see the bar
                    try {
                        Method addPlayer = bar.getClass().getMethod("addPlayer", Player.class);
                        for (Player p : getOnlinePlayers()) addPlayer.invoke(bar, p);
                    } catch (Exception ignored) {}
                }
            }.runTaskTimer(plugin, 0L, 10L);

        } else {
            // Fallback: action bar
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object tryCreateBossBar(String name) {
        if (!bossBarChecked) {
            bossBarChecked = true;
            try {
                barColorClass = Class.forName("org.bukkit.boss.BarColor");
                barStyleClass = Class.forName("org.bukkit.boss.BarStyle");
                createBossBarMethod = Bukkit.class.getMethod(
                        "createBossBar", String.class, barColorClass, barStyleClass);
            } catch (Exception ignored) {}
        }
        if (createBossBarMethod == null) return null;
        try {
            Object green = Enum.valueOf((Class<Enum>) barColorClass, "GREEN");
            Object solid = Enum.valueOf((Class<Enum>) barStyleClass, "SOLID");
            return createBossBarMethod.invoke(null, name, green, solid);
        } catch (Exception ignored) { return null; }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void updateBossBar(Object bar, String name, double pct) {
        try {
            // setProgress
            bar.getClass().getMethod("setProgress", double.class).invoke(bar, pct);
            // setColor based on hp
            String colorName = pct > 0.6 ? "GREEN" : pct > 0.3 ? "YELLOW" : "RED";
            Object color = Enum.valueOf((Class<Enum>) barColorClass, colorName);
            bar.getClass().getMethod("setColor", barColorClass).invoke(bar, color);
            // setTitle with HP number
        } catch (Exception ignored) {}
    }

    private void removeBossBar(Object bar) {
        try {
            bar.getClass().getMethod("removeAll").invoke(bar);
            bar.getClass().getMethod("setVisible", boolean.class).invoke(bar, false);
        } catch (Exception ignored) {}
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

    /**
     * Spawn a particle using Bukkit's Particle API (1.9+) via reflection.
     * Tries multiple particle name aliases for cross-version compat.
     * Falls back to Effect enum for 1.8.
     */
    protected void particle(Location loc, int count, String... names) {
        ensureParticleMethod(loc);
        for (String name : names) {
            if (trySpawnParticle(loc, count, name)) return;
        }
        // Fallback: Effect enum (uses last name in list for backward compat)
        fx(loc, count, names);
    }

    /** Effect enum — works on 1.8 for names like SMOKE, MOBSPAWNER_FLAMES */
    protected void fx(Location loc, int count, String... effectNames) {
        for (String name : effectNames) {
            try {
                Effect eff = Effect.valueOf(name);
                for (int i = 0; i < count; i++) loc.getWorld().playEffect(loc, eff, 0);
                return;
            } catch (Exception ignored) {}
        }
    }

    /** Strike a cosmetic lightning bolt at location (very visible, no damage) */
    protected void lightningEffect(Location loc) {
        try {
            loc.getWorld().getClass()
                    .getMethod("strikeLightningEffect", Location.class)
                    .invoke(loc.getWorld(), loc);
        } catch (Exception ignored) {
            // fallback: normal lightning
            try { loc.getWorld().strikeLightning(loc); } catch (Exception ignored2) {}
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void ensureParticleMethod(Location loc) {
        if (particleInitDone) return;
        particleInitDone = true;
        try {
            particleClass = Class.forName("org.bukkit.Particle");
            // Try the simple 3-arg overload first
            try {
                spawnParticleMethod = loc.getWorld().getClass().getMethod(
                        "spawnParticle", particleClass, Location.class, int.class);
            } catch (NoSuchMethodException ex) {
                // Some Paper versions use different signatures; try double,double,double variant
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
            // Fallback: try the Location x,y,z double overload
            Method m = loc.getWorld().getClass().getMethod(
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
