import os
import re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritTask.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

replacement = """        if (island == null) {
            player.sendMessage("\u00a7c\u0110\u1ea3o kh\u00f4ng t\u1ed3n t\u1ea1i ho\u1eb7c ch\u01b0a c\u00f3 \u0111\u1ea3o!");
            return;
        }
        
        Map<Location, PlacedSpirit> spirits = module.getSpiritManager().getPlacedSpiritLocations(island);
        if (spirits.isEmpty()) {
            player.sendMessage("\u00a7cKh\u00f4ng c\u00f3 Tinh Linh n\u00e0o \u0111\u01b0\u1ee3c \u0111\u1eb7t tr\u00ean \u0111\u1ea3o!");
            return;
        }

        Map<Material, Integer> totalDrops = new java.util.HashMap<>();"""

text = re.sub(
    r'if \(island == null\) return;\s*Map<Location, PlacedSpirit> spirits = module.getSpiritManager\(\)\.getPlacedSpiritLocations\(island\);\s*if \(spirits\.isEmpty\(\)\) return;\s*Map<Material, Integer> totalDrops = new java\.util\.HashMap<>\(\);',
    replacement,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
