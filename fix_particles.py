import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

reflection = """            try {
                Class<?> particleClass = Class.forName("org.bukkit.Particle");
                Object totem = Enum.valueOf((Class<Enum>) particleClass, "TOTEM");
                player.getWorld().getClass().getMethod("spawnParticle", particleClass, org.bukkit.Location.class, int.class, double.class, double.class, double.class, double.class)
                    .invoke(player.getWorld(), totem, player.getLocation().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.1);
            } catch (Exception ignored) {}"""

text = text.replace('            try {\n                player.getWorld().spawnParticle(org.bukkit.Particle.valueOf("TOTEM"), player.getLocation().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.1);\n            } catch (Exception ignored) {}', reflection)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
