import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/PlayerSpiritsMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('import com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule.SpiritConfigInfo;', 'import com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule.SpiritConfigInfo;\nimport com.bgsoftware.superiorskyblock.module.spirits.SpiritManager.PlacedSpirit;')
text = text.replace('Map<Location, String> placed = module.getSpiritManager().getPlacedSpiritLocations(island);', 'Map<Location, PlacedSpirit> placed = module.getSpiritManager().getPlacedSpiritLocations(island);')
text = text.replace('for (Map.Entry<Location, String> entry : placed.entrySet()) {', 'for (Map.Entry<Location, PlacedSpirit> entry : placed.entrySet()) {')
text = text.replace('String type = entry.getValue();', 'String type = entry.getValue().getType();')
text = text.replace('lore.add("  \\u00a77Y: \\u00a7f" + loc.getBlockY());', 'lore.add("  \\u00a77Y: \\u00a7f" + loc.getBlockY());\n                lore.add("  \\u00a7eC\\u1ea5p \\u0111\\u1ed9: \\u00a76" + entry.getValue().getLevel());')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
