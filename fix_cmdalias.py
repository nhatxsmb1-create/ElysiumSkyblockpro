import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/CmdMarket.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('java.util.Arrays.asList("market", "chungkhoan")', 'java.util.Arrays.asList("shop", "market", "chungkhoan")')
text = text.replace('return "market";', 'return "shop";')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
