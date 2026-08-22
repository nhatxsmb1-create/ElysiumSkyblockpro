import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketModule.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

old_shop_info = """        public static class ShopItemInfo {
            private final String category;
            private final String materialName;
            private final double buyPrice;

            public ShopItemInfo(String category, String materialName, double buyPrice) {
                this.category = category;
                this.materialName = materialName;
                this.buyPrice = buyPrice;
            }

            public String getCategory() { return category; }
            public double getBuyPrice() { return buyPrice; }
            public Material getMaterial() {
                try {
                    return Material.valueOf(materialName);
                } catch (Exception ex) {
                    return Material.matchMaterial(materialName);
                }
            }
        }"""

new_shop_info = """        public static class ShopItemInfo {
            private final String category;
            private final String materialName;
            private final double buyPrice;
            private int poolSize = 0;

            public ShopItemInfo(String category, String materialName, double buyPrice) {
                this.category = category;
                this.materialName = materialName;
                this.buyPrice = buyPrice;
            }

            public String getCategory() { return category; }
            public double getBuyPrice() { return buyPrice; }
            public Material getMaterial() {
                try {
                    return Material.valueOf(materialName);
                } catch (Exception ex) {
                    return Material.matchMaterial(materialName);
                }
            }
            
            public void setPoolSize(int size) { this.poolSize = Math.max(0, size); }
            public int getPoolSize() { return poolSize; }
            public void addPoolSize(int amount) { this.poolSize = Math.max(0, this.poolSize + amount); }
            
            public double getCurrentBuyPrice() {
                return buyPrice * (1.0 + (poolSize / 1000.0) * 0.02);
            }
        }"""

text = text.replace(old_shop_info, new_shop_info)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
