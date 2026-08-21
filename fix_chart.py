import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

chart_render = '''            for (int row = CHART_HEIGHT; row >= 1; row--) {
                StringBuilder line = new StringBuilder("    ");
                for (int col = 0; col < 15; col++) {
                    if (heights[col] == row) {
                        String color = "\u00a7c";
                        if (heights[col] >= 3) color = "\u00a7e";
                        if (heights[col] == 5) color = "\u00a7a";
                        line.append(color).append("\u2588");
                    } else {
                        line.append("\u00a78\u2591");
                    }
                }
                lore.add(line.toString());
            }'''

text = re.sub(
    r'            for \(int row = CHART_HEIGHT; row >= 1; row--\) \{.*?lore\.add\(line\.toString\(\)\);\s*\}',
    chart_render,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
