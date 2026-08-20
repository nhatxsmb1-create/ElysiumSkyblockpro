import os
path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/CmdAdminSpirit.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('return "superiorskyblock.admin.spirit";', 'return "";')

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
