import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);', 'ItemStack border;\n        try {\n            border = new ItemStack(Material.valueOf("BLACK_STAINED_GLASS_PANE"));\n        } catch (Exception ex) {\n            border = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 15);\n        }')
text = text.replace('if (clickedMat == Material.BLACK_STAINED_GLASS_PANE || clickedMat == Material.BOOK) {', 'if (clickedMat.name().contains("GLASS_PANE") || clickedMat == Material.BOOK) {')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
