import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('getNavBtn("\u00a7e\u25c0 Trang Tr\u01b0\u1edbc", Material.PAPER)', 'getNavBtn("\u00a7e\u25c0 Trang Tr\u01b0\u1edbc", Material.ARROW)')
text = text.replace('getNavBtn("\u00a7eTrang T\u1edbi \u25b6", Material.PAPER)', 'getNavBtn("\u00a7eTrang T\u1edbi \u25b6", Material.ARROW)')

text = text.replace('if (slot == base + 0 && clickedMat == Material.PAPER) {', 'if (slot == base + 0 && clickedMat == Material.ARROW) {')
text = text.replace('if (slot == base + 8 && clickedMat == Material.PAPER) {', 'if (slot == base + 8 && clickedMat == Material.ARROW) {')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
