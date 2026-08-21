import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketModule.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

# Update MarketItemInfo fields
text = text.replace(
    '        private final String materialName;',
    '        private final String category;\n        private final String materialName;'
)

text = text.replace(
    '        public MarketItemInfo(String materialName, double basePrice, double minPrice, double maxPrice, double dropRate, int recoveryRate) {',
    '        private final java.util.LinkedList<Double> priceHistory = new java.util.LinkedList<>();\n\n        public MarketItemInfo(String category, String materialName, double basePrice, double minPrice, double maxPrice, double dropRate, int recoveryRate) {\n            this.category = category;'
)

# Update Configuration parser
text = text.replace(
    'double basePrice = section.getDouble(key + ".base-price");',
    'String category = section.getString(key + ".category", "MINERAL");\n                    double basePrice = section.getDouble(key + ".base-price");'
)

text = text.replace(
    'items.put(key, new MarketItemInfo(key, basePrice, minPrice, maxPrice, dropRate, recoveryRate));',
    'items.put(key, new MarketItemInfo(category, key, basePrice, minPrice, maxPrice, dropRate, recoveryRate));'
)

# Add getters for new fields
getters = '''
        public String getCategory() { return category; }
        public java.util.List<Double> getPriceHistory() { return priceHistory; }
        public void addPriceHistory(double price) {
            priceHistory.addLast(price);
            if (priceHistory.size() > 10) {
                priceHistory.removeFirst();
            }
        }
'''
text = text.replace('        public Material getMaterial() {', getters + '\n        public Material getMaterial() {')

# Update load data to load history
load_history = '''
        for (Map.Entry<String, MarketItemInfo> entry : getConfiguration().getItems().entrySet()) {
            int poolSize = dataConfig.getInt("pool." + entry.getKey(), 0);
            entry.getValue().setPoolSize(poolSize);
            java.util.List<Double> hist = dataConfig.getDoubleList("history." + entry.getKey());
            if (hist != null) {
                for (double h : hist) entry.getValue().addPriceHistory(h);
            }
        }
'''
text = re.sub(
    r'        // Load pool sizes from dataConfig into Configuration\s*for \(Map.Entry<String, MarketItemInfo> entry : getConfiguration\(\)\.getItems\(\)\.entrySet\(\)\) \{\s*int poolSize = dataConfig.getInt\("pool\." \+ entry\.getKey\(\), 0\);\s*entry\.getValue\(\)\.setPoolSize\(poolSize\);\s*\}',
    load_history.strip(),
    text,
    flags=re.DOTALL
)

# Update saveData to save history
save_history = '''
        if (dataConfig != null && dataFile != null) {
            for (Map.Entry<String, MarketItemInfo> entry : getConfiguration().getItems().entrySet()) {
                dataConfig.set("pool." + entry.getKey(), entry.getValue().getPoolSize());
                dataConfig.set("history." + entry.getKey(), entry.getValue().getPriceHistory());
            }
'''
text = text.replace(
    '        if (dataConfig != null && dataFile != null) {\n            for (Map.Entry<String, MarketItemInfo> entry : getConfiguration().getItems().entrySet()) {\n                dataConfig.set("pool." + entry.getKey(), entry.getValue().getPoolSize());\n            }',
    save_history
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
