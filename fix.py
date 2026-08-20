import re
import os

def to_unicode(s):
    return "".join(c if ord(c) < 128 else "\\u%04x" % ord(c) for c in s)

def fix(path):
    with open(path, "r", encoding="utf-8") as f:
        txt = f.read()
    txt = re.sub(r'"([^"\\]*(?:\\.[^"\\]*)*)"', lambda m: '"' + to_unicode(m.group(1)) + '"', txt)
    with open(path, "w", encoding="utf-8") as f:
        f.write(txt)

for f in [
    "src/main/java/com/bgsoftware/superiorskyblock/module/trophies/TrophiesMainMenu.java",
    "src/main/java/com/bgsoftware/superiorskyblock/module/trophies/TrophiesPlacedMenu.java",
    "src/main/java/com/bgsoftware/superiorskyblock/module/trophies/TrophiesCollectionMenu.java",
    "src/main/java/com/bgsoftware/superiorskyblock/module/trophies/CmdTrophies.java",
    "src/main/java/com/bgsoftware/superiorskyblock/module/trophies/listeners/TrophyListener.java"
]:
    fix(f)
