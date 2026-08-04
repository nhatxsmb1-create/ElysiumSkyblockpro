package com.bgsoftware.superiorskyblock.module.worldevents;

public enum WorldEventType {
    METEOR_SHOWER("☄ Mưa Thiên Thạch",    0,  30),
    ANCIENT_TREE ("🌳 Cây Cổ Thụ",         0,  25),
    CELESTIAL    ("✦ Sự Kiện Thiên Thể",   10,  20),
    TORNADO      ("🌪 Lốc Xoáy",            25,  20),
    INVASION     ("👹 Xâm Lược",            25,  15),
    VOLCANO      ("🌋 Núi Lửa",             50,  12),
    SPACE_RIFT   ("🌀 Cổng Không Gian",     60,  10);

    private final String displayName;
    private final int minInstability;
    private final int weight;

    WorldEventType(String displayName, int minInstability, int weight) {
        this.displayName    = displayName;
        this.minInstability = minInstability;
        this.weight         = weight;
    }

    public String getDisplayName()  { return displayName; }
    public int getMinInstability()  { return minInstability; }
    public int getWeight()          { return weight; }
}
