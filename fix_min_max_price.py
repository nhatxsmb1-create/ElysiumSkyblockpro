import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

# Update openMarketItems
text = re.sub(
    r'(lore\.add\("\\u00a7eGi\\u00e1 thu mua: \\u00a7a\$" \+ String\.format\("%.2f", currentPrice\)\);\s*lore\.add\(""\);)',
    r'\1\n                    lore.add("\\u00a77\\u2191 Gi\\u00e1 \u0110\\u1ec9nh: \\u00a7a$" + String.format("%.2f", info.getMaxPrice()));\n                    lore.add("\\u00a77\\u2193 Gi\\u00e1 \u0110\\u00e1y: \\u00a7c$" + String.format("%.2f", info.getMinPrice()));\n                    lore.add("");',
    text,
    count=1
)

# Update openMarketSell
text = re.sub(
    r'(lore\.add\("\\u00a7bS\\u1ed1 l\\u01b0\\u1ee3ng server \\u0111\\u00e3 b\\u00e1n: \\u00a7f" \+ info\.getPoolSize\(\)\);\s*lore\.add\(""\);)',
    r'\1\n            lore.add("\\u00a77\\u2191 Gi\\u00e1 \u0110\\u1ec9nh (Cao nh\\u1ea5t): \\u00a7a$" + String.format("%.2f", info.getMaxPrice()));\n            lore.add("\\u00a77\\u2193 Gi\\u00e1 \u0110\\u00e1y (Th\\u1ea5p nh\\u1ea5t): \\u00a7c$" + String.format("%.2f", info.getMinPrice()));\n            lore.add("");',
    text,
    count=1
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
