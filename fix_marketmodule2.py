import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketModule.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

# Add ShopItemInfo
shop_item_info = '''
        private final Map<String, ShopItemInfo> shopItems = new LinkedHashMap<>();
        
        public Map<String, ShopItemInfo> getShopItems() {
            return shopItems;
        }

        public static class ShopItemInfo {
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
        }
'''
text = text.replace('private final Map<String, MarketItemInfo> items = new LinkedHashMap<>();', 'private final Map<String, MarketItemInfo> items = new LinkedHashMap<>();' + shop_item_info)

# Add to Configuration constructor
load_shop = '''
            if (config.contains("buy-shop")) {
                org.bukkit.configuration.ConfigurationSection shopSection = config.getConfigurationSection("buy-shop");
                for (String key : shopSection.getKeys(false)) {
                    String category = shopSection.getString(key + ".category", "BUILDING");
                    double buyPrice = shopSection.getDouble(key + ".buy-price");
                    shopItems.put(key, new ShopItemInfo(category, key, buyPrice));
                }
            }
'''
text = text.replace('org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection("items");', load_shop + 'org.bukkit.configuration.ConfigurationSection section = config.getConfigurationSection("items");')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
