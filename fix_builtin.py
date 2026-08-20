import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/BuiltinModules.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace(
    'public static final com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule TROPHIES = new com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule();',
    'public static final com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule TROPHIES = new com.bgsoftware.superiorskyblock.module.trophies.TrophiesModule();\n    public static final com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule SPIRITS = new com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule();'
)

text = text.replace(
    'case "trophies":\n                return TROPHIES;',
    'case "trophies":\n                return TROPHIES;\n            case "spirits":\n                return SPIRITS;'
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
