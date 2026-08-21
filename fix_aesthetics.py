import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

# 1. Update Chart Math
chart_math = '''            int CHART_HEIGHT = 5;
            int[] heights = new int[15];
            double min = info.getMinPrice();
            double max = info.getMaxPrice();
            double base = info.getBasePrice();
            
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
                
                int h = (int) Math.round(normalized * (CHART_HEIGHT - 1)) + 1;
                if (h > CHART_HEIGHT) h = CHART_HEIGHT;
                if (h < 1) h = 1;
                heights[i] = h;
            }'''
text = re.sub(
    r'            int CHART_HEIGHT = 5;\s*int\[\] heights = new int\[15\];.*?heights\[i\] = h;\s*\}',
    chart_math,
    text,
    flags=re.DOTALL
)

# 2. Update Borders
border_logic = '''    private ItemStack getBorder() {
        ItemStack border;
        try { border = new ItemStack(Material.valueOf("GRAY_STAINED_GLASS_PANE")); } 
        catch (Exception ex) { border = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 7); }
        ItemMeta meta = border.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); border.setItemMeta(meta); }
        return border;
    }
    
    private ItemStack getAccentBorder() {
        ItemStack border;
        try { border = new ItemStack(Material.valueOf("CYAN_STAINED_GLASS_PANE")); } 
        catch (Exception ex) { border = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 9); }
        ItemMeta meta = border.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); border.setItemMeta(meta); }
        return border;
    }'''
text = re.sub(
    r'    private ItemStack getBorder\(\) \{.*?return border;\s*\}',
    border_logic,
    text,
    flags=re.DOTALL
)

# 3. Apply Accent Borders to Shop Main
shop_layout = '''    private void openShopMain(Player player, String category) {
        currentState = State.SHOP_MAIN;
        currentCategory = category;
        inventory = Bukkit.createInventory(null, 54, "\u00a78\u00a7lC\u1eeda H\u00e0ng: " + category);
        
        ItemStack border = getBorder();
        ItemStack accent = getAccentBorder();
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                if (i < 9 || i > 44) inventory.setItem(i, accent);
                else inventory.setItem(i, border);
            }
        }

        Material bMat = Material.BRICK;
        try { bMat = Material.valueOf("BRICKS"); } catch(Exception e) {}
        
        inventory.setItem(2, getShopCatBtn("BUILDING", bMat, "Kh\u1ed1i X\u00e2y D\u1ef1ng", category));
        inventory.setItem(3, getShopCatBtn("DECORATION", Material.PAINTING, "\u0110\u1ed3 Trang Tr\u00ed", category));
        
        Material tMat = Material.DIAMOND_PICKAXE;
        inventory.setItem(5, getShopCatBtn("TOOLS", tMat, "C\u00f4ng C\u1ee5", category));
        
        Material rMat = Material.NETHER_STAR;
        inventory.setItem(6, getShopCatBtn("RARES", rMat, "\u0110\u1ed3 Hi\u1ebfm", category));

        inventory.setItem(49, getBackButton());
        inventory.setItem(53, getTabButton(true));'''
text = re.sub(
    r'    private void openShopMain\(Player player, String category\) \{.*?inventory\.setItem\(53, getTabButton\(true\)\);',
    shop_layout,
    text,
    flags=re.DOTALL
)

# 4. Apply Accent Borders to Market Category
market_layout = '''    private void openMarketCategory(Player player, String category) {
        currentState = State.MARKET_CATEGORY;
        currentCategory = category;
        String title = category.equals("MINERAL") ? "\u00a78\u00a7lS\u00e0n Kho\u00e1ng S\u1ea3n" : "\u00a78\u00a7lS\u00e0n N\u00f4ng S\u1ea3n";
        inventory = Bukkit.createInventory(null, 54, title);
        
        ItemStack border = getBorder();
        ItemStack accent = getAccentBorder();
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                if (i < 9 || i > 44) inventory.setItem(i, accent);
                else inventory.setItem(i, border);
            }
        }

        inventory.setItem(49, getBackButton());
        
        inventory.setItem(45, getTabButton(false)); 
        inventory.setItem(53, getTabButton(true));'''
text = re.sub(
    r'    private void openMarketCategory\(Player player, String category\) \{.*?inventory\.setItem\(53, getTabButton\(true\)\);',
    market_layout,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
