import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

replace1 = '''        ItemStack cropBtn;
        try {
            cropBtn = new ItemStack(Material.valueOf("GOLDEN_HOE"));
        } catch (Exception ex) {
            cropBtn = new ItemStack(Material.valueOf("GOLD_HOE"));
        }'''

text = text.replace('ItemStack cropBtn = new ItemStack(Material.GOLDEN_HOE);', replace1)
text = text.replace('} else if (clickedMat == Material.GOLDEN_HOE) {', '} else if (clickedMat.name().contains("HOE")) {')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
