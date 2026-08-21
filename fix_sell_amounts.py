import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketMenu.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

# Replace openMarketItem logic
open_market_item = '''        inventory.setItem(13, center);

        inventory.setItem(10, getSellBtn(info, 1, false));
        inventory.setItem(11, getSellBtn(info, 64, false));
        inventory.setItem(12, getSellBtn(info, -1, false)); // -1 means All

        inventory.setItem(14, getSellBtn(info, 64, true));
        inventory.setItem(15, getSellBtn(info, -1, true));

        player.openInventory(inventory);
    }'''

text = re.sub(
    r'        inventory\.setItem\(13, center\);.*?player\.openInventory\(inventory\);\s*\}',
    open_market_item,
    text,
    flags=re.DOTALL
)

# Replace handleSell logic
handle_sell = '''    private ItemStack getSellBtn(MarketModule.MarketItemInfo info, int amount, boolean fromKho) {
        Material icon = fromKho ? Material.ENDER_CHEST : Material.CHEST;
        String amountStr = amount == -1 ? "T\u1ea5t C\u1ea3" : "x" + amount;
        String fromStr = fromKho ? "Kho \u1ea2o" : "T\u00fai \u0110\u1ed3";
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String color = fromKho ? "\u00a7d\u00a7l" : "\u00a7a\u00a7l";
            meta.setDisplayName(color + "B\u00e1n " + amountStr + " (" + fromStr + ")");
            List<String> lore = new ArrayList<>();
            lore.add("\u00a77B\u00e1n " + amountStr + " " + MarketModule.getVietnameseName(info.getMaterial()));
            lore.add("\u00a77t\u1eeb " + fromStr + " c\u1ee7a b\u1ea1n v\u00e0o S\u00e0n.");
            lore.add("");
            lore.add("\u00a7a[\u25b6] Click \u0111\u1ec3 B\u00e1n");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void handleSell(Player player, MarketModule.MarketItemInfo info, boolean fromKho, int exactAmount) {
        SuperiorPlayer sp = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
        long amountToSell = 0;
        Material mat = info.getMaterial();

        if (!fromKho) {
            if (exactAmount == -1) {
                ItemStack[] contents = player.getInventory().getContents();
                for (int i = 0; i < contents.length; i++) {
                    if (contents[i] != null && contents[i].getType() == mat) {
                        amountToSell += contents[i].getAmount();
                        player.getInventory().setItem(i, null);
                    }
                }
            } else {
                int count = 0;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == mat) count += item.getAmount();
                }
                if (count < exactAmount) {
                    player.sendMessage("\u00a7cB\u1ea1n kh\u00f4ng c\u00f3 \u0111\u1ee7 " + exactAmount + "x " + MarketModule.getVietnameseName(mat) + "!");
                    return;
                }
                int remaining = exactAmount;
                ItemStack[] contents = player.getInventory().getContents();
                for (int i = 0; i < contents.length; i++) {
                    if (contents[i] != null && contents[i].getType() == mat) {
                        if (contents[i].getAmount() <= remaining) {
                            remaining -= contents[i].getAmount();
                            player.getInventory().setItem(i, null);
                        } else {
                            contents[i].setAmount(contents[i].getAmount() - remaining);
                            remaining = 0;
                        }
                        if (remaining <= 0) break;
                    }
                }
                amountToSell = exactAmount;
            }
        } else {
            if (sp.getIsland() == null) { player.sendMessage("\u00a7cB\u1ea1n ch\u01b0a c\u00f3 \u0111\u1ea3o!"); return; }
            BigInteger amountInKho = BuiltinModules.ORE_STORAGE.getStorageManager().getAmount(sp.getIsland().getUniqueId(), mat);
            if (amountInKho.compareTo(BigInteger.ZERO) > 0) {
                if (exactAmount == -1) {
                    amountToSell = amountInKho.longValue();
                    if (amountToSell > 100000L) amountToSell = 100000L;
                } else {
                    if (amountInKho.compareTo(BigInteger.valueOf(exactAmount)) < 0) {
                        player.sendMessage("\u00a7cKho \u1ea2o c\u1ee7a b\u1ea1n kh\u00f4ng \u0111\u1ee7 " + exactAmount + "x " + MarketModule.getVietnameseName(mat) + "!");
                        return;
                    }
                    amountToSell = exactAmount;
                }
                BuiltinModules.ORE_STORAGE.getStorageManager().removeAmount(sp.getIsland().getUniqueId(), mat, BigInteger.valueOf(amountToSell));
            } else {
                player.sendMessage("\u00a7cKho \u1ea2o c\u1ee7a b\u1ea1n kh\u00f4ng c\u00f2n " + MarketModule.getVietnameseName(mat) + "!");
                return;
            }
        }

        if (amountToSell <= 0) { player.sendMessage("\u00a7cB\u1ea1n kh\u00f4ng c\u00f3 " + MarketModule.getVietnameseName(mat) + " \u0111\u1ec3 b\u00e1n!"); return; }

        double currentPrice = info.getCurrentPrice();
        double totalMoney = currentPrice * amountToSell;

        plugin.getProviders().getEconomyProvider().depositMoney(sp, totalMoney);
        info.addPoolSize((int) amountToSell);
        module.saveData();

        player.sendMessage("\u00a7a\u2714 \u0110\u00e3 b\u00e1n " + amountToSell + "x " + MarketModule.getVietnameseName(mat) + " v\u1edbi gi\u00e1 $" + String.format("%.2f", totalMoney));

        if (amountToSell >= 10000 || totalMoney >= 100000) {
            Bukkit.getServer().broadcastMessage("");
            Bukkit.getServer().broadcastMessage("\u00a7b\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
            Bukkit.getServer().broadcastMessage("\u00a7c\u00a7l\u26a0 C\u1ea2NH B\u00c1O C\u00c1 M\u1eacP X\u1ea2 H\u00c0NG \u26a0");
            Bukkit.getServer().broadcastMessage("\u00a7e\u0110\u1ea1i gia \u00a7a" + player.getName() + " \u00a7ev\u1eeba x\u1ea3 \u00a7f" + amountToSell + "x " + MarketModule.getVietnameseName(mat) + " \u00a7ev\u00e0o th\u1ecb tr\u01b0\u1eddng!");
            Bukkit.getServer().broadcastMessage("\u00a77\u27a4 Gi\u00e1 " + MarketModule.getVietnameseName(mat) + " \u0111ang r\u1edbt! Anh em c\u1ea9n th\u1eadn!");
            Bukkit.getServer().broadcastMessage("\u00a7b\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584\u2584");
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                try { p.playSound(p.getLocation(), org.bukkit.Sound.valueOf("ENTITY_ENDER_DRAGON_GROWL"), 0.5f, 1.5f); } 
                catch (Exception ex) { try { p.playSound(p.getLocation(), org.bukkit.Sound.valueOf("ENDERDRAGON_GROWL"), 0.5f, 1.5f); } catch (Exception ignored) {} }
            }
            try {
                Class<?> particleClass = Class.forName("org.bukkit.Particle");
                Object totem = Enum.valueOf((Class<Enum>) particleClass, "TOTEM");
                player.getWorld().getClass().getMethod("spawnParticle", particleClass, org.bukkit.Location.class, int.class, double.class, double.class, double.class, double.class)
                    .invoke(player.getWorld(), totem, player.getLocation().add(0, 1, 0), 100, 0.5, 0.5, 0.5, 0.1);
            } catch (Exception ignored) {}
        } else {
            try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("ENTITY_PLAYER_LEVELUP"), 0.5f, 2f); } 
            catch (Exception ex) { try { player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("LEVEL_UP"), 0.5f, 2f); } catch (Exception ignored) {} }
        }
        openMarketItem(player, info);
    }'''

text = re.sub(
    r'    private void handleSell\(Player player, MarketModule\.MarketItemInfo info, boolean fromKho\) \{.*?\s*openMarketItem\(player, info\);\s*\}',
    handle_sell,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
