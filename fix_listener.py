import os
import re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritsListener.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('import org.bukkit.event.player.PlayerQuitEvent;', 'import org.bukkit.event.player.PlayerQuitEvent;\nimport com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;\nimport com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;')

replacement = """    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(e.getPlayer().getUniqueId());
        if (sp != null) {
            sp.getPersistentDataContainer().put("spirit_offline_time", PersistentDataType.LONG, System.currentTimeMillis());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        if (sp != null && sp.getPersistentDataContainer().has("spirit_offline_time")) {
            long quitTime = sp.getPersistentDataContainer().getOrDefault("spirit_offline_time", PersistentDataType.LONG, System.currentTimeMillis());
            long elapsed = System.currentTimeMillis() - quitTime;
            sp.getPersistentDataContainer().remove("spirit_offline_time");
            
            long maxElapsed = 8 * 3600000L; // 8 hours
            if (player.hasPermission("elysium.offline.vip2")) {
                maxElapsed = 24 * 3600000L;
            } else if (player.hasPermission("elysium.offline.vip1")) {
                maxElapsed = 12 * 3600000L;
            }
            
            if (elapsed > maxElapsed) elapsed = maxElapsed;
            if (elapsed > 60000L) { // at least 1 minute offline
                long ticks = (elapsed / 1000L) * 20L;
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                    module.getSpiritTask().simulateOffline(player, ticks);
                }, 60L); // wait 3 seconds before reporting to avoid chat spam on join
            }
        }
    }"""

text = re.sub(
    r'    @EventHandler\(priority = EventPriority\.MONITOR\)\n    public void onPlayerQuit\(PlayerQuitEvent e\).*?// wait 3 seconds before reporting to avoid chat spam on join\n            \}\n        \}\n    \}',
    replacement,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
