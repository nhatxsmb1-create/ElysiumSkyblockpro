import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritTask.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace(
    'player.sendMessage("\u00a7a+ " + drop.getValue() + " \u00a7f" + drop.getKey().name());',
    'player.sendMessage("\u00a7a+ " + drop.getValue() + " \u00a7f" + SpiritsModule.getVietnameseName(drop.getKey()));'
)

# Calculate minutes from ticks
# ticks is passed as parameter to simulateOffline(Player player, long ticks)
# 20 ticks = 1 second
# 1200 ticks = 1 minute
time_logic = """
        long totalMinutes = ticks / 1200L;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        String timeString = (hours > 0 ? hours + " Gi\u1edd " : "") + minutes + " Ph\u00fat";
        
        player.sendMessage("\u00a7b\u2728 \u00a7e\u00a7lB\u00c1O C\u00c1O NGO\u1ea0I TUY\u1ebeN \u00a77(" + timeString + ")");
"""

text = text.replace('player.sendMessage("\u00a7b\u2728 \u00a7e\u00a7lB\u00c1O C\u00c1O NGO\u1ea0I TUY\u1ebeN");', time_logic)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
