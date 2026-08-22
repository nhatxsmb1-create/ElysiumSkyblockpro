import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

old_lore_items = """                    List<String> lore = new ArrayList<>();
                    lore.add("\\u00a78\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584");
                    lore.add("\\u00a7eGi\\u00e1 mua: \\u00a7c$" + String.format("%.2f", info.getCurrentBuyPrice()));
                    lore.add("");
                    lore.add("\\u00a7a[\\u25b6] Click \\u0111\\u1ec3 Xem t\\u00f9y ch\\u1ecdn Mua");"""

new_lore_items = """                    List<String> lore = new ArrayList<>();
                    double currentPrice = info.getCurrentBuyPrice();
                    double basePrice = info.getBuyPrice();
                    String status = (currentPrice > basePrice) ? "\\u00a7c\\u2b06 L\\u1ea1m ph\\u00e1t (T\\u0103ng gi\\u00e1)" : "\\u00a7a\\u2714 B\\u00ecnh \\u1ed5n gi\\u00e1";
                    
                    lore.add("\\u00a78\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584");
                    lore.add("\\u00a77Tr\\u1ea1ng th\\u00e1i: " + status);
                    lore.add("\\u00a7eGi\\u00e1 mua hi\\u1ec7n t\\u1ea1i: \\u00a7c$" + String.format("%.2f", currentPrice));
                    lore.add("");
                    lore.add("\\u00a77L\\u01b0\\u1ee3ng mua to\\u00e0n Server: \\u00a7f" + info.getPoolSize());
                    lore.add("\\u00a77\\u2193 Gi\\u00e1 g\\u1ed1c: \\u00a7a$" + String.format("%.2f", basePrice));
                    lore.add("");
                    lore.add("\\u00a7a[\\u25b6] Click \\u0111\\u1ec3 Xem t\\u00f9y ch\\u1ecdn Mua");"""

text = text.replace(old_lore_items, new_lore_items)

old_buy_center = """        inventory.setItem(13, getDisplayItem(info.getMaterial(), "\\u00a7a\\u00a7l" + MarketModule.getVietnameseName(info.getMaterial()), "\\u00a7eGi\\u00e1 g\\u1ed1c: \\u00a7c$" + info.getCurrentBuyPrice()));"""

new_buy_center = """        ItemStack center = getDisplayItem(info.getMaterial(), "\\u00a7a\\u00a7l" + MarketModule.getVietnameseName(info.getMaterial()), "");
        ItemMeta cMeta = center.getItemMeta();
        if (cMeta != null) {
            List<String> lore = new ArrayList<>();
            double currentPrice = info.getCurrentBuyPrice();
            double basePrice = info.getBuyPrice();
            String status = (currentPrice > basePrice) ? "\\u00a7c\\u2b06 L\\u1ea1m ph\\u00e1t (T\\u0103ng gi\\u00e1)" : "\\u00a7a\\u2714 B\\u00ecnh \\u1ed5n gi\\u00e1";
            
            lore.add("");
            lore.add("\\u00a77Tr\\u1ea1ng th\\u00e1i: " + status);
            lore.add("\\u00a7eGi\\u00e1 mua hi\\u1ec7n t\\u1ea1i: \\u00a7c$" + String.format("%.2f", currentPrice) + " \\u00a77/ c\\u00e1i");
            lore.add("\\u00a77L\\u01b0\\u1ee3ng mua to\\u00e0n Server: \\u00a7f" + info.getPoolSize());
            lore.add("\\u00a77\\u2193 Gi\\u00e1 g\\u1ed1c: \\u00a7a$" + String.format("%.2f", basePrice));
            lore.add("");
            lore.add("\\u00a78\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584");
            cMeta.setLore(lore);
            center.setItemMeta(cMeta);
        }
        inventory.setItem(13, center);"""

text = text.replace(old_buy_center, new_buy_center)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
