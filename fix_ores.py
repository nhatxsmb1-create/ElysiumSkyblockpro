import os
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/orestorage/StorageListener.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('"IRON_BLOCK", "GOLD_BLOCK",', '"IRON_BLOCK", "GOLD_BLOCK",\n              "COAL_ORE", "IRON_ORE", "GOLD_ORE", "DIAMOND_ORE", "EMERALD_ORE", "LAPIS_ORE", "REDSTONE_ORE",\n              "DEEPSLATE_COAL_ORE", "DEEPSLATE_IRON_ORE", "DEEPSLATE_GOLD_ORE", "DEEPSLATE_DIAMOND_ORE", "DEEPSLATE_EMERALD_ORE", "DEEPSLATE_LAPIS_ORE", "DEEPSLATE_REDSTONE_ORE",\n              "NETHER_QUARTZ_ORE", "NETHER_GOLD_ORE", "ANCIENT_DEBRIS",')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
