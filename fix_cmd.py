import os
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/CmdAdminSpirit.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('return java.util.Arrays.asList("adminspirit", "as");', 'return java.util.Arrays.asList("spirit", "spirits");')
text = text.replace('return "adminspirit <player> <type>";', 'return "spirit <player> <type>";')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
