package com.bgsoftware.superiorskyblock.module.worldevents;

/**
 * Defines every island World Event type.
 * minInstability: instability threshold required for this event to be eligible.
 * weight:         relative chance weight when rolling which event fires.
 */
public enum WorldEventType {

    // Low-instability events (gentle, always available)
    METEOR_SHOWER("☄ Meteor Shower",    0,  30),
    ANCIENT_TREE ("🌳 Ancient Tree",    0,  25),
    CELESTIAL    ("☄ Celestial Event", 10,  20),

    // Mid-instability events
    TORNADO      ("🌪 Tornado",         25,  20),
    INVASION     ("👹 Invasion",         25,  15),

    // High-instability events (intense, rare)
    VOLCANO      ("🌋 Volcano",          50,  12),
    SPACE_RIFT   ("🌀 Space Rift",       60,  10);

    private final String displayName;
    private final int minInstability;
    private final int weight;

    WorldEventType(String displayName, int minInstability, int weight) {
        this.displayName = displayName;
        this.minInstability = minInstability;
        this.weight = weight;
    }

    public String getDisplayName() { return displayName; }
    public int getMinInstability()  { return minInstability; }
    public int getWeight()          { return weight; }
}
