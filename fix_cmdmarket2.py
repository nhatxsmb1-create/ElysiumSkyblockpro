import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/CmdMarket.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

exec_code = '''    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            String cmd = args.length > 0 ? args[0].toLowerCase() : "";
            MarketMenu menu = new MarketMenu(module, plugin);
            if (cmd.equals("market")) {
                menu.openBuyShop((Player) sender);
            } else if (cmd.equals("chungkhoan")) {
                menu.openMarket((Player) sender);
            } else {
                // Mặc định (như /is shop) sẽ mở Menu Ở Giữa (Trade Center)
                menu.open((Player) sender);
            }
        }
    }'''

text = re.sub(
    r'    public void execute\(SuperiorSkyblock plugin, CommandSender sender, String\[\] args\) \{.*?    \}',
    exec_code,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
