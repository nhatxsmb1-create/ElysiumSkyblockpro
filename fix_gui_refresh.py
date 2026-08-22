import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

old_end = """        try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP"), 1f, 1f); } 
        catch (Exception ex) { try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ORB_PICKUP"), 1f, 1f); } catch (Exception ignored) {} }
    }"""

new_end = """        try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP"), 1f, 1f); } 
        catch (Exception ex) { try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ORB_PICKUP"), 1f, 1f); } catch (Exception ignored) {} }
        
        openShopBuy(player, info);
    }"""

text = text.replace(old_end, new_end)
with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
