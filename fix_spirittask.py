import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritTask.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

simulate_method = '''
    public void simulateOffline(Player player, long ticks) {
        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        Island island = sp.getIsland();
        if (island == null) return;
        
        Map<Location, PlacedSpirit> spirits = module.getSpiritManager().getPlacedSpiritLocations(island);
        if (spirits.isEmpty()) return;

        Map<Material, Integer> totalDrops = new java.util.HashMap<>();

        for (Map.Entry<Location, PlacedSpirit> entry : spirits.entrySet()) {
            String type = entry.getValue().getType();
            int level = entry.getValue().getLevel();
            SpiritConfigInfo info = module.getConfiguration().getSpirits().get(type);
            if (info == null) continue;

            int currentInterval = info.getActionIntervalTicks();
            if (info.getUpgrades().containsKey(level)) {
                currentInterval = info.getUpgrades().get(level).getIntervalTicks();
            }

            long actionsCount = ticks / currentInterval;
            if (actionsCount <= 0) continue;

            if (type.equalsIgnoreCase("miner")) {
                Dimension dim = Dimensions.fromEnvironment(World.Environment.NORMAL);
                Map<String, Integer> amounts = island.getGeneratorAmounts(dim);
                if (amounts != null && !amounts.isEmpty()) {
                    int totalWeight = 0;
                    for (Integer weight : amounts.values()) {
                        totalWeight += weight;
                    }
                    if (totalWeight > 0) {
                        for (long i = 0; i < actionsCount; i++) {
                            int rand = random.nextInt(totalWeight);
                            int current = 0;
                            Material drop = null;
                            for (Map.Entry<String, Integer> amt : amounts.entrySet()) {
                                current += amt.getValue();
                                if (rand < current) {
                                    try { drop = Material.matchMaterial(amt.getKey()); } catch (Exception ignored) {}
                                    break;
                                }
                            }
                            if (drop == null) drop = Material.COBBLESTONE;
                            totalDrops.put(drop, totalDrops.getOrDefault(drop, 0) + 1);
                        }
                    } else {
                        totalDrops.put(Material.COBBLESTONE, totalDrops.getOrDefault(Material.COBBLESTONE, 0) + (int)actionsCount);
                    }
                } else {
                    totalDrops.put(Material.COBBLESTONE, totalDrops.getOrDefault(Material.COBBLESTONE, 0) + (int)actionsCount);
                }
            } else if (type.equalsIgnoreCase("farmer")) {
                for (long i = 0; i < actionsCount; i++) {
                    Material drop = FARMER_DROPS[random.nextInt(FARMER_DROPS.length)];
                    totalDrops.put(drop, totalDrops.getOrDefault(drop, 0) + 1);
                }
            }
        }

        if (totalDrops.isEmpty()) return;

        for (Map.Entry<Material, Integer> drop : totalDrops.entrySet()) {
            BuiltinModules.ORE_STORAGE.getStorageManager().addAmount(island.getUniqueId(), drop.getKey(), BigInteger.valueOf(drop.getValue()));
        }
        
        player.sendMessage("\u00a7b\u2728 \u00a7e\u00a7lB\u00c1O C\u00c1O NGO\u1ea0I TUY\u1ebeN");
        player.sendMessage("\u00a77Tinh linh c\u1ee7a b\u1ea1n \u0111\u00e3 l\u00e0m vi\u1ec7c ch\u0103m ch\u1ec9 v\u00e0 \u0111\u01b0a v\u00e0o /is kho:");
        for (Map.Entry<Material, Integer> drop : totalDrops.entrySet()) {
            player.sendMessage("\u00a7a+ " + drop.getValue() + " \u00a7f" + drop.getKey().name());
        }
        try {
            player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 1f, 1f);
        } catch (Exception ex) {
            try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("LEVEL_UP"), 1f, 1f); } catch (Exception ignored) {}
        }
    }
'''

text = text.replace('public class SpiritTask extends BukkitRunnable {', 'public class SpiritTask extends BukkitRunnable {' + simulate_method)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
