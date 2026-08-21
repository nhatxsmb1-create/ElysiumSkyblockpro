import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

vfx_logic = '''        player.sendMessage("\u00a7a\u2714 \u0110\u00e3 b\u00e1n " + amountToSell + "x " + MarketModule.getVietnameseName(mat) + " v\u1edbi gi\u00e1 " + String.format("$%.2f", totalMoney));

        if (amountToSell >= 10000 || totalMoney >= 100000) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("\u00a7b\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
            Bukkit.broadcastMessage("\u00a7c\u00a7l\u26a0 C\u1ea2NH B\u00c1O C\u00c1 M\u1eacP X\u1ea2 H\u00c0NG \u26a0");
            Bukkit.broadcastMessage("\u00a7e\u0110\u1ea1i gia \u00a7a" + player.getName() + " \u00a7ev\u1eeba x\u1ea3 \u00a7f" + amountToSell + "x " + MarketModule.getVietnameseName(mat) + " \u00a7ev\u00e0o th\u1ecb tr\u01b0\u1eddng!");
            Bukkit.broadcastMessage("\u00a77\u27a4 Gi\u00e1 c\u1ee7a " + MarketModule.getVietnameseName(mat) + " \u0111ang r\u1edbt th\u00ea th\u1ea3m! Anh em c\u1ea9n th\u1eadn!");
            Bukkit.broadcastMessage("\u00a7b\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                try {
                    p.playSound(p.getLocation(), org.bukkit.Sound.valueOf("ENTITY_ENDER_DRAGON_GROWL"), 0.5f, 1.5f);
                } catch (Exception ex) {
                    try { p.playSound(p.getLocation(), org.bukkit.Sound.valueOf("ENDERDRAGON_GROWL"), 0.5f, 1.5f); } catch (Exception ignored) {}
                }
            }
            try {
                player.getWorld().spawnParticle(org.bukkit.Particle.valueOf("TOTEM"), player.getLocation().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.1);
            } catch (Exception ignored) {}
        } else {
            try {
                player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 0.5f, 2f);
            } catch (Exception ex) {
                try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("LEVEL_UP"), 0.5f, 2f); } catch (Exception ignored) {}
            }
        }'''

# Find the end of handleSell and replace the sound logic
text = re.sub(
    r'        player\.sendMessage\(\"\\u00a7a\\u2714 \\u0110\\u00e3 b\\u00e1n \".*?\} catch \(Exception ignored\) \{\}\s*\}',
    vfx_logic,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
