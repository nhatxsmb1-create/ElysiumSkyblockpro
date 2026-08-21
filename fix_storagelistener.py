import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/orestorage/StorageListener.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace(
    '"IRON_BLOCK", "GOLD_BLOCK",',
    '"IRON_BLOCK", "GOLD_BLOCK", "DIAMOND_BLOCK", "EMERALD_BLOCK", "COAL_BLOCK", "LAPIS_BLOCK", "REDSTONE_BLOCK", "HAY_BLOCK", "MELON", "PUMPKIN",'
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
