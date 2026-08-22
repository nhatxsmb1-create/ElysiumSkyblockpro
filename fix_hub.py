import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

hub_btn = """
        ItemStack thuongVuBtn = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta thuongVuMeta = thuongVuBtn.getItemMeta();
        if (thuongVuMeta != null) {
            thuongVuMeta.setDisplayName("\\u00a7e\\u00a7l\\u272a TH\\u01af\\u01a0NG V\\u1ee4 B\\u1ea0C T\\u1ef6 \\u272a");
            List<String> lore = new ArrayList<>();
            lore.add("\\u00a77Click \\u0111\\u1ec3 xem H\\u1ee3p \\u0111\\u1ed3ng");
            lore.add("\\u00a77thu mua To\\u00e0n M\\u00e1y Ch\\u1ee7 h\\u00f4m nay.");
            thuongVuMeta.setLore(lore);
            thuongVuBtn.setItemMeta(thuongVuMeta);
        }
        inventory.setItem(13, thuongVuBtn);
"""

text = text.replace('inventory.setItem(15, marketBtn);', 'inventory.setItem(15, marketBtn);\n' + hub_btn)

text = text.replace('else if (slot == 15) openMarketCats(player);', 'else if (slot == 15) openMarketCats(player);\n            else if (slot == 13) { new DealMenu(module, plugin).open(player); }')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
