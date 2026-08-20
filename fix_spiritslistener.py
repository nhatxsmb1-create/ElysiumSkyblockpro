import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritsListener.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace(
    'import org.bukkit.inventory.ItemStack;',
    'import org.bukkit.inventory.ItemStack;\nimport org.bukkit.event.inventory.InventoryClickEvent;\nimport org.bukkit.inventory.InventoryHolder;'
)

append_text = """

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof AdminSpiritsMenu) {
            e.setCancelled(true);
            if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof AdminSpiritsMenu)) return;
            
            ItemStack clicked = e.getCurrentItem();
            String type = module.getSpiritManager().getSpiritType(clicked);
            if (type != null) {
                ItemStack item = module.getSpiritManager().createSpiritItem(type);
                if (item != null) {
                    Player player = (Player) e.getWhoClicked();
                    player.getInventory().addItem(item);
                    player.sendMessage("\\u00a7b\\u2728 \\u00a7eB\\u1ea1n \\u0111\\u00e3 l\\u1ea5y 1 " + item.getItemMeta().getDisplayName());
                }
            }
        } else if (holder instanceof PlayerSpiritsMenu) {
            e.setCancelled(true);
        }
    }
"""
text = text.replace('    private boolean isSpiritBlock(Location location) {', append_text + '\n    private boolean isSpiritBlock(Location location) {')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
