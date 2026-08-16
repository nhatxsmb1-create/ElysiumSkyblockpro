package com.bgsoftware.superiorskyblock.module;

import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.module.bank.BankModule;
import com.bgsoftware.superiorskyblock.module.generators.GeneratorsModule;
import com.bgsoftware.superiorskyblock.module.missions.MissionsModule;
import com.bgsoftware.superiorskyblock.module.upgrades.UpgradesModule;
import com.bgsoftware.superiorskyblock.module.worldevents.WorldEventsModule;

import java.util.Locale;

public class BuiltinModules {

    public static final GeneratorsModule GENERATORS = new GeneratorsModule();
    public static final MissionsModule MISSIONS = new MissionsModule();
    public static final BankModule BANK = new BankModule();
    public static final UpgradesModule UPGRADES = new UpgradesModule();
    public static final WorldEventsModule WORLD_EVENTS = new WorldEventsModule();
    public static final com.bgsoftware.superiorskyblock.module.orestorage.OreStorageModule ORE_STORAGE = new com.bgsoftware.superiorskyblock.module.orestorage.OreStorageModule();
    public static final com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule TROPHIES = new com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule();

    private BuiltinModules() {

    }

    public static PluginModule getBuiltinModule(String name) {
        switch (name.toLowerCase(Locale.ENGLISH)) {
            case "generators":
                return GENERATORS;
            case "missions":
                return MISSIONS;
            case "bank":
                return BANK;
            case "upgrades":
                return UPGRADES;
            case "worldevents":
                return WORLD_EVENTS;
            case "orestorage":
                return ORE_STORAGE;
            case "trophies":
                return TROPHIES;
            default:
                return null;
        }
    }

}
