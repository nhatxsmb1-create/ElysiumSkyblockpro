import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

new_chart = '''            lore.add("\u00a7d\u25bc Bi\u1ec3u \u0111\u1ed3 bi\u1ebfn \u0111\u1ed9ng gi\u00e1 \u25bc");
            
            List<Double> hist = new ArrayList<>(info.getPriceHistory());
            hist.add(currentPrice);
            while (hist.size() < 15) { hist.add(0, info.getBasePrice()); }
            
            double min = info.getMinPrice();
            double max = info.getMaxPrice();
            double base = info.getBasePrice();
            
            StringBuilder sparkline = new StringBuilder("  ");
            char[] blocks = new char[]{'_', '\u2581', '\u2582', '\u2583', '\u2584', '\u2585', '\u2586', '\u2588'};
            
            for (int i = 0; i < 15; i++) {
                double p = hist.get(i);
                double normalized = 0.5;
                if (p >= base) {
                    double range = max - base;
                    if (range <= 0) range = 1;
                    normalized = 0.5 + 0.5 * (p - base) / range;
                } else {
                    double range = base - min;
                    if (range <= 0) range = 1;
                    normalized = 0.5 * (p - min) / range;
                }
                if (normalized < 0) normalized = 0;
                if (normalized > 1) normalized = 1;
                
                String color = "\u00a7c";
                if (p == base) color = "\u00a7e";
                else if (p > base) color = "\u00a7a";
                
                int idx = (int) Math.round(normalized * 7);
                if (idx < 0) idx = 0;
                if (idx > 7) idx = 7;
                
                sparkline.append(color).append(blocks[idx]);
            }
            lore.add(sparkline.toString());
            
            lore.add("\u00a78\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");'''

text = re.sub(
    r'            lore\.add\("\\u00a7d\\u25bc Bi\\u1ec3u \\u0111\\u1ed3 bi\\u1ebfn \\u0111\\u1ed9ng gi\\u00e1 \\u25bc"\);.*?lore\.add\("\\u00a78\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584\\u2584"\);',
    new_chart,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
