import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritTask.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('if (totalDrops.isEmpty()) return;', 'if (totalDrops.isEmpty()) { player.sendMessage("\u00a7cKh\u00f4ng c\u00f3 v\u1eadt ph\u1ea9m n\u00e0o \u0111\u01b0\u1ee3c t\u1ea1o ra!"); return; }')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
