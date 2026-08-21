import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritsListener.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

import_str = '''
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
'''

text = text.replace('import org.bukkit.inventory.ItemStack;', 'import org.bukkit.inventory.ItemStack;' + import_str)

offline_logic = '''
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        player.getPersistentDataContainer().put(
            new org.bukkit.NamespacedKey(plugin, "spirit_offline_time"),
            org.bukkit.persistence.PersistentDataType.LONG,
            System.currentTimeMillis()
        );
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "spirit_offline_time");
        if (player.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.LONG)) {
            long quitTime = player.getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.LONG);
            long elapsed = System.currentTimeMillis() - quitTime;
            player.getPersistentDataContainer().remove(key);
            
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
    }
'''

text = text.replace('public class SpiritsListener implements Listener {', 'public class SpiritsListener implements Listener {\n' + offline_logic)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
