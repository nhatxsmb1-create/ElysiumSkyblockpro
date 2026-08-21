import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritUpgradeMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace(
    'lore.add(color + "- " + cost.getValue() + "x " + cost.getKey().name());',
    'lore.add(color + "- " + cost.getValue() + "x " + SpiritsModule.getVietnameseName(cost.getKey()));'
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
