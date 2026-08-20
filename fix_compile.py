import os

# Fix SpiritsModule.java
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritsModule.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('import com.bgsoftware.superiorskyblock.api.modules.BuiltinModule;', 'import com.bgsoftware.superiorskyblock.module.BuiltinModule;')
text = text.replace('import com.bgsoftware.superiorskyblock.api.modules.IModuleConfiguration;', 'import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

# Fix SpiritTask.java
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritTask.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('import org.bukkit.entity.Player;', 'import org.bukkit.entity.Player;\nimport com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;')
text = text.replace('Island island = plugin.getGrid().getIsland(p);', 'SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(p.getUniqueId());\n            Island island = sp.getIsland();')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

# Fix CmdSpirits.java
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/CmdSpirits.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('import org.bukkit.entity.Player;', 'import org.bukkit.entity.Player;\nimport com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;')
text = text.replace('Island island = plugin.getGrid().getIsland(player);', 'SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());\n        Island island = sp.getIsland();')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

# Fix SpiritsListener.java (just in case there's any similar issue)
