import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritUpgradeMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('org.bukkit.Sound.ENTITY_PLAYER_LEVELUP', 'org.bukkit.Sound.valueOf("ENTITY_PLAYER_LEVELUP")')
# Add fallback to LEVEL_UP just in case
text = text.replace(
    'player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 1f, 1f);',
    'try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 1f, 1f); } catch (Exception ex) { try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("LEVEL_UP"), 1f, 1f); } catch (Exception ex2) {} }'
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
