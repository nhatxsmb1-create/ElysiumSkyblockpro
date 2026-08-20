import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/ModulesManagerImpl.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace(
    'registerModule(BuiltinModules.TROPHIES);',
    'registerModule(BuiltinModules.TROPHIES);\n        registerModule(BuiltinModules.SPIRITS);'
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
