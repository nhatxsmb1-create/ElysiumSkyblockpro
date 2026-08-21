import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

new_methods = '''    public void open(Player player) {
        openMain(player);
    }

    public void openMarket(Player player) {
        openMarketCategory(player, "MINERAL");
    }

    public void openBuyShop(Player player) {
        openShopMain(player, "BUILDING");
    }'''

text = text.replace('    public void open(Player player) {\n        openMain(player);\n    }', new_methods)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
