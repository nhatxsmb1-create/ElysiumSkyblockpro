import os
import codecs

def convert_to_utf8(filepath):
    # Read as cp1258, write as utf-8
    with codecs.open(filepath, 'r', encoding='cp1258') as f:
        content = f.read()
    with codecs.open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Converted {filepath} to UTF-8")

convert_to_utf8('src/main/resources/menus/upgrades.yml')
convert_to_utf8('src/main/resources/menus/upgrades1_12.yml')
convert_to_utf8('src/main/resources/menus/upgrades1_13.yml')
convert_to_utf8('src/main/resources/menus/upgrades1_20.yml')
