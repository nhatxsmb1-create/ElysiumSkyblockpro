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
              
              // Pad history to 15 columns so it looks like a real chart
              while (hist.size() < 15) {
                  hist.add(0, info.getBasePrice());
              }
              
              StringBuilder sparkline = new StringBuilder();
              String[] blocks = {"\\u2581", "\\u2582", "\\u2583", "\\u2584", "\\u2585", "\\u2586", "\\u2587", "\\u2588", "\\u2588"};
              for (double p : hist) {
                  double range = info.getMaxPrice() - info.getMinPrice();
                  if (range <= 0) range = 1;
                  double normalized = (p - info.getMinPrice()) / range;
                  if (normalized < 0) normalized = 0;
                  if (normalized > 1) normalized = 1;
                  int blockIndex = (int) Math.round(normalized * 8);
                  if (blockIndex > 8) blockIndex = 8;
                  if (blockIndex < 0) blockIndex = 0;
                  
                  String color = "\\u00a7c"; // Red
                  if (normalized > 0.4) color = "\\u00a7e"; // Yellow
                  if (normalized > 0.7) color = "\\u00a7a"; // Green
                  sparkline.append(color).append(blocks[blockIndex]);
              }
              lore.add("    " + sparkline.toString());'''

    text = text[:start_idx] + new_chart + text[end_idx:]

    with open(path, 'w', encoding='utf-8') as f:
        f.write(text)
