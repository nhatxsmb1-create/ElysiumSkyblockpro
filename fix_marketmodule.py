import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketModule.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

old_shop_info = """        public static class ShopItemInfo {
            private final String category;
            private final Material material;
            private final double buyPrice;

            public ShopItemInfo(String category, String material, double buyPrice) {
                this.category = category;
                this.buyPrice = buyPrice;
                Material mat;
                try {
                    mat = Material.valueOf(material.toUpperCase());
                } catch (Exception e) {
                    mat = Material.STONE;
                }
                this.material = mat;
            }

            public String getCategory() { return category; }
            public Material getMaterial() { return material; }
            public double getBuyPrice() { return buyPrice; }
        }"""

new_shop_info = """        public static class ShopItemInfo {
            private final String category;
            private final Material material;
            private final double buyPrice;
            private int poolSize = 0;

            public ShopItemInfo(String category, String material, double buyPrice) {
                this.category = category;
                this.buyPrice = buyPrice;
                Material mat;
                try { mat = Material.valueOf(material.toUpperCase()); } catch (Exception e) { mat = Material.STONE; }
                this.material = mat;
            }

            public String getCategory() { return category; }
            public Material getMaterial() { return material; }
            public double getBuyPrice() { return buyPrice; }
            
            public void setPoolSize(int size) { this.poolSize = Math.max(0, size); }
            public int getPoolSize() { return poolSize; }
            public void addPoolSize(int amount) { this.poolSize = Math.max(0, this.poolSize + amount); }
            
            public double getCurrentBuyPrice() {
                // Tang 2% gia cho moi 1000 item duoc mua
                return buyPrice * (1.0 + (poolSize / 1000.0) * 0.02);
            }
        }"""

text = text.replace(old_shop_info, new_shop_info)

target = "for (Map.Entry<String, MarketItemInfo> entry : getConfiguration().getItems().entrySet()) {"

# onEnable replacement
new_load_logic = """for (Map.Entry<String, Configuration.ShopItemInfo> entry : getConfiguration().getShopItems().entrySet()) {
            int poolSize = dataConfig.getInt("shop_pool." + entry.getKey(), 0);
            entry.getValue().setPoolSize(poolSize);
        }
        for (Map.Entry<String, MarketItemInfo> entry : getConfiguration().getItems().entrySet()) {"""
text = text.replace(target, new_load_logic, 1)

# saveData replacement
new_save_logic = """for (Map.Entry<String, Configuration.ShopItemInfo> entry : getConfiguration().getShopItems().entrySet()) {
                dataConfig.set("shop_pool." + entry.getKey(), entry.getValue().getPoolSize());
            }
            for (Map.Entry<String, MarketItemInfo> entry : getConfiguration().getItems().entrySet()) {"""
text = text.replace(target, new_save_logic, 1)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
