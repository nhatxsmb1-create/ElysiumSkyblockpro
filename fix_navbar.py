import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

nav_logic = '''    private void setupNavigationBar(Inventory inv, int size, boolean hasPrev, boolean hasNext) {
        int row = size / 9 - 1;
        int base = row * 9;
        
        ItemStack border = getBorder(true);
        for (int i = 0; i < 9; i++) {
            inv.setItem(base + i, border);
        }
        
        if (hasPrev) inv.setItem(base + 0, getNavBtn("\u00a7e\u25c0 Trang Tr\u01b0\u1edbc", Material.PAPER));
        inv.setItem(base + 2, getNavBtn("\u00a7a\u00a7l\uD83D\uDED2 C\u1eecA H\u00c0NG", Material.CHEST));
        inv.setItem(base + 4, getNavBtn("\u00a7c\u00a7l\u2716 TR\u1ede L\u1ea0I", Material.BARRIER));
        inv.setItem(base + 6, getNavBtn("\u00a76\u00a7l\uD83D\uDCC8 CH\u1ee8NG KHO\u00c1N", Material.ENDER_CHEST));
        if (hasNext) inv.setItem(base + 8, getNavBtn("\u00a7eTrang T\u1edbi \u25b6", Material.PAPER));
    }'''

text = re.sub(
    r'    private void setupNavigationBar\(Inventory inv, int size, boolean hasPrev, boolean hasNext\) \{.*?    \}',
    nav_logic,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
