import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/BuiltinModules.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('public static final com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule SPIRITS = new com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule();', 'public static final com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule SPIRITS = new com.bgsoftware.superiorskyblock.module.spirits.SpiritsModule();\n    public static final com.bgsoftware.superiorskyblock.module.market.MarketModule MARKET = new com.bgsoftware.superiorskyblock.module.market.MarketModule();')
text = text.replace('case "spirits":\n                return SPIRITS;', 'case "spirits":\n                return SPIRITS;\n            case "market":\n                return MARKET;')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
