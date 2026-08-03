package com.bgsoftware.superiorskyblock.module.worldevents;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.module.worldevents.event.*;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class WorldEventScheduler {

    private final SuperiorSkyblockPlugin plugin;
    private final WorldEventsModule module;
    private BukkitTask task;
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

        for (Island island : SuperiorSkyblockAPI.getGrid().getIslands()) {
            UUID id = island.getUniqueId();
            if (activeEvents.contains(id)) continue;

            int instability = module.getInstabilityManager().getInstability(id);
            module.getInstabilityManager().addInstability(id, -cfg.getInstabilityDecayPerCheck());

            double chance = cfg.getBaseEventChance() + instability * cfg.getInstabilityChanceBonus();
            if (rng.nextDouble() * 100 > chance) continue;

            WorldEventType type = pickEventType(instability, rng);
            if (type == null) continue;

            triggerEvent(island, type);
        }
    }

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

    public void triggerEvent(Island island, WorldEventType type) {
        // Use fromEnvironment to get the overworld Dimension
        Dimension normalDim = Dimensions.fromEnvironment(World.Environment.NORMAL);
        Location center = island.getCenter(normalDim);
        if (center == null || center.getWorld() == null) return;

        for (SuperiorPlayer sp : island.getIslandMembers(true)) {
            if (sp.isOnline() && sp.asPlayer() != null) {
                plugin.getNMSPlayers().sendTitle(
                        sp.asPlayer(),
                        "§6" + type.getDisplayName(),
                        "§eA world event appeared on your island!",
                        10, 60, 20
                );
                sp.asPlayer().sendMessage("§d§l[World Event] §r§6" + type.getDisplayName()
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
