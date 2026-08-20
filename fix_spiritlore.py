import os
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritManager.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace(
    'lore.add("\\u00a77\\u0110\\u1eb7t xu\\u1ed1ng \\u0111\\u1ea3o \\u0111\\u1ec3 kích ho\\u1ea1t.");',
    'lore.add("\\u00a77\\u0110\\u1eb7t xu\\u1ed1ng \\u0111\\u1ea3o \\u0111\\u1ec3 kích ho\\u1ea1t.");\n            if (info.getDescription() != null && !info.getDescription().isEmpty()) {\n                lore.add("");\n                for (String descLine : info.getDescription()) {\n                    lore.add(org.bukkit.ChatColor.translateAlternateColorCodes(\'&\', descLine));\n                }\n            }'
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
