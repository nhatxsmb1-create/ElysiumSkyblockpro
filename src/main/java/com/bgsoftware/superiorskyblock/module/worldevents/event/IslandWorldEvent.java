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

    // ── HP Bar via BossBar (top of screen ONLY) ───────────────

    /**
     * Shows the boss HP bar exclusively at the TOP of the screen using BossBar API via reflection.
     * Action Bar fallback has been completely removed per request.
     */
    protected void trackHPBar(LivingEntity boss, String bossName) {
        final Object bossBar = tryCreateBossBar(bossName);
        if (bossBar == null) return;

        try {
            Method addPlayer = bossBar.getClass().getMethod("addPlayer", Player.class);
            for (Player p : getOnlinePlayers()) {
                addPlayer.invoke(bossBar, p);
            }
        } catch (Exception ignored) {}

        new BukkitRunnable() {
            @Override public void run() {
                if (!boss.isValid()) {
                    cancel();
                    removeBossBar(bossBar);
                    return;
                }
                double pct = Math.max(0.0, Math.min(1.0, boss.getHealth() / boss.getMaxHealth()));
                updateBossBar(bossBar, bossName, pct);
                
                // Maintain active player list on the BossBar
                try {
                    Method addPlayer = bossBar.getClass().getMethod("addPlayer", Player.class);
                    Method getPlayers = bossBar.getClass().getMethod("getPlayers");
                    java.util.Collection<?> active = (java.util.Collection<?>) getPlayers.invoke(bossBar);
                    
                    List<Player> current = getOnlinePlayers();
                    for (Player p : current) {
                        if (!active.contains(p)) {
                            addPlayer.invoke(bossBar, p);
                        }
                    }
                    
                    Method removePlayer = bossBar.getClass().getMethod("removePlayer", Player.class);
                    List<Player> copy = new ArrayList<>((java.util.Collection<Player>) active);
                    for (Player p : copy) {
                        if (!current.contains(p)) {
                            removePlayer.invoke(bossBar, p);
                        }
                    }
                } catch (Exception ignored) {}
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object tryCreateBossBar(String name) {
        try {
            ClassLoader cl = Bukkit.class.getClassLoader();
            Class<?> colorClass = cl.loadClass("org.bukkit.boss.BarColor");
            Class<?> styleClass = cl.loadClass("org.bukkit.boss.BarStyle");
            Class<?> flagClass  = cl.loadClass("org.bukkit.boss.BarFlag");
            
            Object green = Enum.valueOf((Class<Enum>) colorClass, "GREEN");
            Object solid = Enum.valueOf((Class<Enum>) styleClass, "SOLID");
            Object flags = java.lang.reflect.Array.newInstance(flagClass, 0);
            
            Method createBar = null;
            for (Method m : Bukkit.class.getMethods()) {
                if (m.getName().equals("createBossBar")) {
                    Class<?>[] pTypes = m.getParameterTypes();
                    // Must take String as 1st parameter to avoid NamespacedKey overloads in MC 1.20+
                    if (pTypes.length >= 3 && pTypes[0] == String.class) {
                        createBar = m;
                        break;
                    }
                }
            }
            
            if (createBar == null) return null;
            
            Object bar = createBar.invoke(null, name, green, solid, flags);
            if (bar != null) {
                bar.getClass().getMethod("setVisible", boolean.class).invoke(bar, true);
            }
            return bar;
        } catch (Throwable t) {
            plugin.getLogger().warning("[WorldEvents] Failed to create top BossBar: " + t.getMessage());
            return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void updateBossBar(Object bar, String name, double pct) {
        try {
            bar.getClass().getMethod("setProgress", double.class).invoke(bar, pct);
            
            ClassLoader cl = Bukkit.class.getClassLoader();
            Class<?> colorClass = cl.loadClass("org.bukkit.boss.BarColor");
            String colorName = pct > 0.6 ? "GREEN" : pct > 0.3 ? "YELLOW" : "RED";
            Object color = Enum.valueOf((Class<Enum>) colorClass, colorName);
            
            bar.getClass().getMethod("setColor", colorClass).invoke(bar, color);
            bar.getClass().getMethod("setTitle", String.class).invoke(bar, name + " §f- " + (int)(pct * 100) + "% HP");
        } catch (Throwable ignored) {}
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

    protected ItemStack createEventItem(org.bukkit.Material mat, String name, String rarity, String eventName, String description) {
        return createEventItem(mat, name, rarity, eventName, description, 1);
    }
    protected ItemStack createEventItem(org.bukkit.Material mat, String name, String rarity, String eventName, String description, int amount) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("\u00a78\u00a7m                                ");
            lore.add("\u00a77Ngu\u1ed3n g\u1ed1c: \u00a76" + eventName);
            lore.add("\u00a77Ph\u1ea9m ch\u1ea5t: " + rarity);
            lore.add("");
            String[] words = description.split(" ");
            StringBuilder line = new StringBuilder("\u00a7f");
            for (String word : words) {
                if (line.length() + word.length() > 30) {
                    lore.add(line.toString());
                    line = new StringBuilder("\u00a7f");
                }
                line.append(word).append(" ");
            }
            if (line.length() > 2) lore.add(line.toString());
            lore.add("");
            lore.add("\u00a7a\u2714 \u00a77D\u00f9ng đ\u1ec3 Trao \u0111\u1ed5i");
            lore.add("\u00a77t\u1ea1i khu v\u1ef1c: \u00a7e/warp trade");
            lore.add("\u00a78\u00a7m                                ");
            meta.setLore(lore);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            try { meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS); } catch (Exception ignored) {}
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack named(org.bukkit.Material mat, String name) { return named(mat, name, 1); }
    protected ItemStack named(org.bukkit.Material mat, String name, int amount) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); item.setItemMeta(meta); }
        return item;
    }

    /**
     * Rolls a trophy drop from the trophies module (if enabled) and
     * drops it at the given location. Called when a Mini Boss dies.
     */
    protected void dropTrophy(Location location, String trophyId) {
        try {
            com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule module =
                    com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule.get();
            if (module == null || !module.isEnabled() || !module.getTrophyManager().shouldDropTrophy())
                return;

            org.bukkit.inventory.ItemStack trophyItem = module.getTrophyManager().createTrophyItem(trophyId);
            if (trophyItem != null) {
                location.getWorld().dropItemNaturally(location, trophyItem);
                broadcast("§6🏆 §eMột Trophy đã rơi ra! Đặt nó lên đảo để mở Trophy Hall!");
            }
        } catch (Exception ignored) {
        }
    }

    private WorldEventsModule getModule() {
        return (WorldEventsModule) plugin.getModules().getModule("worldevents");
    }
}
