import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

start_marker = 'List<Double> hist = new ArrayList<>(info.getPriceHistory());'
end_marker = 'lore.add("    " + sparkline.toString());'

start_idx = text.find(start_marker)
end_idx = text.find(end_marker) + len(end_marker)

if start_idx != -1 and end_idx != -1:
    new_chart = '''List<Double> hist = new ArrayList<>(info.getPriceHistory());
              hist.add(currentPrice); // Add current to end
              
              while (hist.size() < 15) {
                  hist.add(0, info.getBasePrice());
              }
              
              int CHART_HEIGHT = 5;
              int[] heights = new int[15];
              double min = info.getMinPrice();
              double max = info.getMaxPrice();
              double range = max - min;
              if (range <= 0) range = 1;
              
              for (int i = 0; i < 15; i++) {
                  double p = hist.get(i);
                  double normalized = (p - min) / range;
                  int h = (int) Math.round(normalized * CHART_HEIGHT);
                  if (h > CHART_HEIGHT) h = CHART_HEIGHT;
                  if (h < 1) h = 1; // At least 1 block high so it shows
                  heights[i] = h;
              }
              
              for (int row = CHART_HEIGHT; row >= 1; row--) {
                  StringBuilder line = new StringBuilder("    ");
                  for (int col = 0; col < 15; col++) {
                      if (heights[col] >= row) {
                          String color = "\\u00a7c"; // Red
                          if (heights[col] >= 3) color = "\\u00a7e"; // Yellow
                          if (heights[col] == 5) color = "\\u00a7a"; // Green
                          line.append(color).append("\\u2588"); // Full block
                      } else {
                          line.append("\\u00a78\\u2591"); // Dark gray shaded block for background
                      }
                  }
                  lore.add(line.toString());
              }'''

    text = text[:start_idx] + new_chart + text[end_idx:]

    with open(path, 'w', encoding='utf-8') as f:
        f.write(text)
