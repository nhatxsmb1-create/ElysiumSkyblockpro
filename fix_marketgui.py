import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

import re

new_update = """
    private void update() {
        inventory.clear();
        
        // Setup border with glass panes
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            border.setItemMeta(borderMeta);
        }
        
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }
        
        // Setup Info book at slot 4
        ItemStack infoBook = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoBook.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("\u00a7b\u00a7l\u2728 H\u01af\u1edaNG D\u1eaaN S\u00c0N CH\u1ee8NG KHO\u00c1N \u2728");
            List<String> infoLore = new ArrayList<>();
            infoLore.add("\u00a77\u0110\u00e2y l\u00e0 S\u00e0n Giao D\u1ecbch T\u00e0i Nguy\u00ean m\u1edf c\u1ee7a Server.");
            infoLore.add("\u00a77Gi\u00e1 c\u1ee7a v\u1eadt ph\u1ea9m \u00a7akh\u00f4ng c\u1ed1 \u0111\u1ecbnh\u00a77, m\u00e0 s\u1ebd");
            infoLore.add("\u00a7athay \u0111\u1ed5i li\u00ean t\u1ee5c \u00a77d\u1ef1a v\u00e0o h\u00e0nh \u0111\u1ed9ng c\u1ee7a ng\u01b0\u1eddi ch\u01a1i!");
            infoLore.add("");
            infoLore.add("\u00a7c\u2b07 N\u1ebfu nhi\u1ec1u ng\u01b0\u1eddi \u0111\u1ed5 x\u00f4 b\u00e1n\u00a77, gi\u00e1 s\u1ebd r\u1edbt th\u00ea th\u1ea3m.");
            infoLore.add("\u00a7a\u2b06 N\u1ebfu kh\u00f4ng ai b\u00e1n\u00a77, gi\u00e1 s\u1ebd d\u1ea7n h\u1ed3i ph\u1ee5c l\u00ean \u0111\u1ec9nh!");
            infoLore.add("");
            infoLore.add("\u00a7e\u27a4 \u00a7eH\u00e3y tr\u1edf th\u00e0nh con s\u00f3i gi\u00e0 ph\u1ed1 Wall B\u00e1n \u0111\u1ec9nh Mua \u0111\u00e1y!");
            infoMeta.setLore(infoLore);
            infoBook.setItemMeta(infoMeta);
        }
        inventory.setItem(4, infoBook);
        
        // Setup Close button at slot 49
        ItemStack closeBtn = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeBtn.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("\u00a7c\u00a7l\u2716 \u0110\u00f3ng Giao Di\u1ec7n");
            closeBtn.setItemMeta(closeMeta);
        }
        inventory.setItem(49, closeBtn);

        int[] innerSlots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
        int index = 0;
        
        for (Map.Entry<String, MarketModule.MarketItemInfo> entry : module.getConfiguration().getItems().entrySet()) {
            if (index >= innerSlots.length) break;
            MarketModule.MarketItemInfo info = entry.getValue();
            Material mat = info.getMaterial();
            if (mat == null) continue;

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("\u00a76\u00a7l" + MarketModule.getVietnameseName(mat));
                List<String> lore = new ArrayList<>();
                double currentPrice = info.getCurrentPrice();
                String status = currentPrice >= info.getBasePrice() ? "\u00a7a\u2b06 \u0110ang c\u00f3 gi\u00e1 (\u0110\u1ec9nh)" : "\u00a7c\u2b07 L\u1ea1m ph\u00e1t (R\u1edbt gi\u00e1)";
                
                lore.add("\u00a78\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
                lore.add("");
                lore.add("\u00a77Tr\u1ea1ng th\u00e1i: " + status);
                lore.add("\u00a7eGi\u00e1 thu mua hi\u1ec7n t\u1ea1i: \u00a7a$" + String.format("%.2f", currentPrice) + " \u00a77/ c\u00e1i");
                lore.add("");
                lore.add("\u00a77\u25b6 Gi\u00e1 th\u1ecb tr\u01b0\u1eddng g\u1ed1c: \u00a7f$" + info.getBasePrice());
                lore.add("\u00a77\u25b6 Gi\u00e1 tr\u1ea7n (Cao nh\u1ea5t): \u00a7a$" + info.getMaxPrice());
                lore.add("\u00a77\u25b6 Gi\u00e1 s\u00e0n (Th\u1ea5p nh\u1ea5t): \u00a7c$" + info.getMinPrice());
                lore.add("");
                lore.add("\u00a7bS\u1ed1 l\u01b0\u1ee3ng server \u0111\u00e3 b\u00e1n h\u00f4m nay: \u00a7f" + info.getPoolSize());
                lore.add("");
                lore.add("\u00a78\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
                lore.add("");
                lore.add("\u00a7a[\u25b6] Click Chu\u1ed9t Tr\u00e1i \u0111\u1ec3 b\u00e1n t\u1ea5t c\u1ea3 trong \u00a76T\u00fai");
                lore.add("\u00a7a[\u25b6] Click Chu\u1ed9t Ph\u1ea3i \u0111\u1ec3 b\u00e1n t\u1ea5t c\u1ea3 t\u1eeb \u00a7e/is kho");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(innerSlots[index++], item);
        }
    }
"""

text = re.sub(r'    private void update\(\) \{.*?(?=    @EventHandler)', new_update, text, flags=re.DOTALL)

# Also update click logic to handle BARRIER
click_logic = """
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;
        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;
        Material clickedMat = e.getCurrentItem().getType();
        
        if (clickedMat == Material.BLACK_STAINED_GLASS_PANE || clickedMat == Material.BOOK) {
            return;
        }
        if (clickedMat == Material.BARRIER) {
            e.getWhoClicked().closeInventory();
            return;
        }
"""

text = re.sub(r'    @EventHandler\s+public void onClick\(InventoryClickEvent e\) \{\s+if \(\!e\.getInventory\(\)\.equals\(inventory\)\) return;\s+e\.setCancelled\(true\);\s+if \(e\.getCurrentItem\(\) == null\) return;\s+Material clickedMat = e\.getCurrentItem\(\)\.getType\(\);', click_logic.strip(), text, flags=re.DOTALL)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
