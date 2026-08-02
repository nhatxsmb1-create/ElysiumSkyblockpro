package com.bgsoftware.superiorskyblock.module.worldevents;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.module.worldevents.event.*;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class WorldEventScheduler {

    private final SuperiorSkyblockPlugin plugin;
    private final WorldEventsModule module;
    private BukkitTask task;

    // Islands currently running an event – only one event per island at a time
    private final Set<UUID> activeEvents = new HashSet<>();

    public WorldEventScheduler(SuperiorSkyblockPlugin plugin, WorldEventsModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    public void start() {
        long intervalTicks = module.getConfiguration().getCheckIntervalSeconds() * 20L;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    private void tick() {
        WorldEventsModule.Configuration cfg = module.getConfiguration();
        Random rng = new Random();

        // Iterate over every loaded island
        for (Island island : SuperiorSkyblockAPI.getGrid().getIslands()) {
            UUID id = island.getUniqueId();
            if (activeEvents.contains(id)) continue; // already busy

            int instability = module.getInstabilityManager().getInstability(id);

            // Passive decay
            module.getInstabilityManager().addInstability(id, -cfg.getInstabilityDecayPerCheck());

            // Roll for event
            double chance = cfg.getBaseEventChance() + instability * cfg.getInstabilityChanceBonus();
            if (rng.nextDouble() * 100 > chance) continue;

            // Pick eligible event type weighted by instability + weight
            WorldEventType type = pickEventType(instability, rng);
            if (type == null) continue;

            triggerEvent(island, type);
        }
    }

    /** Weighted random pick among event types whose minInstability is satisfied. */
    private WorldEventType pickEventType(int instability, Random rng) {
        List<WorldEventType> eligible = new ArrayList<>();
        int totalWeight = 0;
        for (WorldEventType t : WorldEventType.values()) {
            if (instability >= t.getMinInstability()) {
                eligible.add(t);
                totalWeight += t.getWeight();
            }
        }
        if (eligible.isEmpty()) return null;

        int roll = rng.nextInt(totalWeight);
        int cursor = 0;
        for (WorldEventType t : eligible) {
            cursor += t.getWeight();
            if (roll < cursor) return t;
        }
        return eligible.get(eligible.size() - 1);
    }

    /** Publicly callable (e.g. from admin command) to force an event on an island. */
    public void triggerEvent(Island island, WorldEventType type) {
        Location center = island.getCenter(com.bgsoftware.superiorskyblock.world.Dimensions.NORMAL);
        if (center == null || center.getWorld() == null) return;

        List<SuperiorPlayer> members = island.getIslandMembers(true);

        // Notify island members
        String prefix = "§d§l[World Event] §r";
        for (SuperiorPlayer sp : members) {
            if (sp.isOnline() && sp.asPlayer() != null) {
                sp.asPlayer().sendTitle(
                        "§6" + type.getDisplayName(),
                        "§eA world event has appeared on your island!",
                        10, 60, 20
                );
                sp.asPlayer().sendMessage(prefix + "§6" + type.getDisplayName()
                        + " §fhas appeared on your island!");
            }
        }

        UUID islandId = island.getUniqueId();
        activeEvents.add(islandId);

        IslandWorldEvent event = createEvent(type, island, center);
        if (event != null) {
            event.start(plugin, () -> activeEvents.remove(islandId));
        } else {
            activeEvents.remove(islandId);
        }
    }

    private IslandWorldEvent createEvent(WorldEventType type, Island island, Location center) {
        switch (type) {
            case TORNADO:       return new TornadoEvent(island, center);
            case VOLCANO:       return new VolcanoEvent(island, center);
            case SPACE_RIFT:    return new SpaceRiftEvent(island, center);
            case METEOR_SHOWER: return new MeteorShowerEvent(island, center);
            case ANCIENT_TREE:  return new AncientTreeEvent(island, center);
            case INVASION:      return new InvasionEvent(island, center);
            case CELESTIAL:     return new CelestialEvent(island, center);
            default: return null;
        }
    }

    public boolean isActive(UUID islandId) {
        return activeEvents.contains(islandId);
    }
}
