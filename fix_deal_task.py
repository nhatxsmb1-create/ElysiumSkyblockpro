import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/DealManager.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

task = """
        Bukkit.getScheduler().runTaskTimer((org.bukkit.plugin.Plugin) module.getPlugin(), () -> {
            if (active) {
                Bukkit.getServer().broadcastMessage("\\u00a7e\\u00a7l\\u272a TH\\u01af\\u01a0NG V\\u1ee4 B\\u1ea0C T\\u1ef6 \\u0111ang g\\u1ecdi v\\u1ed1n \\u272a");
                Bukkit.getServer().broadcastMessage("\\u00a7fTi\\u1ebfn \\u0111\\u1ed9 thu mua: \\u00a7a" + currentAmount + " \\u00a78/ \\u00a7a" + targetAmount + " " + MarketModule.getVietnameseName(targetMaterial));
                Bukkit.getServer().broadcastMessage("\\u00a77\\u27A4 G\\u00f5 \\u00a7a/is thuongvu \\u00a77\\u0111\\u1ec3 b\\u00e1n \\u0111\\u1ed3 cho T\\u1eadp \\u0111o\\u00e0n v\\u00e0 nh\\u1eadn th\\u01b0\\u1edfng li\\u1ec1n tay!");
            }
        }, 20 * 60 * 15L, 20 * 60 * 15L);
"""

text = text.replace('this.module = module;', 'this.module = module;\n' + task)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
