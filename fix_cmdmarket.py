import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/CmdMarket.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('return "elysium.player.market";', 'return "";')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
