import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/worldevents/event/IslandWorldEvent.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

new_method = """    protected ItemStack createEventItem(org.bukkit.Material mat, String name, String rarity, String eventName, String description) {
        return createEventItem(mat, name, rarity, eventName, description, 1);
    }
    protected ItemStack createEventItem(org.bukkit.Material mat, String name, String rarity, String eventName, String description, int amount) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add("\\u00a78\\u00a7m                                ");
            lore.add("\\u00a77Ngu\\u1ed3n g\\u1ed1c: \\u00a76" + eventName);
            lore.add("\\u00a77Ph\\u1ea9m ch\\u1ea5t: " + rarity);
            lore.add("");
            String[] words = description.split(" ");
            StringBuilder line = new StringBuilder("\\u00a7f");
            for (String word : words) {
                if (line.length() + word.length() > 30) {
                    lore.add(line.toString());
                    line = new StringBuilder("\\u00a7f");
                }
                line.append(word).append(" ");
            }
            if (line.length() > 2) lore.add(line.toString());
            lore.add("");
            lore.add("\\u00a7a\\u2714 \\u00a77D\\u00f9ng \u0111\\u1ec3 Trao \\u0111\\u1ed5i");
            lore.add("\\u00a77t\\u1ea1i khu v\\u1ef1c: \\u00a7e/warp trade");
            lore.add("\\u00a78\\u00a7m                                ");
            meta.setLore(lore);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            try { meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS); } catch (Exception ignored) {}
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack named(org.bukkit.Material mat, String name) { return named(mat, name, 1); }"""

text = text.replace('    protected ItemStack named(org.bukkit.Material mat, String name) { return named(mat, name, 1); }', new_method)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
