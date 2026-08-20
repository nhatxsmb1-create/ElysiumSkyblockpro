import os
import re

path = 'src/main/resources/modules/trophies/config.yml'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

textures = {
    'tornado': 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTM4NjdkMGIyMDBiZjkxMmJlMDBlNzg0MzM3MWRlYjllNGFkZTdjNWFmNmE1MzcwNWEwMmMzMzBjZTMzNmZhYSJ9fX0=',
    'volcano': 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTRhYjAxNTE4ZTM4MTFlYTgyZTI4MTMzYjNhY2QwODcxOGYxOGRmZTQ5NGFhYzVlMDVhNGQ2MWNmZDYyZjA1NSJ9fX0=',
    'ancient-tree': 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTg4ZTMyNWNiYWJmMDE0NzZlN2I2YTI2NjAyOWYyNDQxMzcyZjEyYjhhNzQ1NjZlOWRmNWFjZDAxNzhjNDIwZCJ9fX0=',
    'celestial': 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDcxNWY5ZjBlYTgyYzlhMTJmOWQ0N2M2NjhjYjA5MTU4YTk1YTc2NWZjNzc0ZDM3OWNmNTRkMmZlZWIzZDUwYyJ9fX0=',
    'invasion': 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzc5ODIzNmQ0NTI4ZWU1MzQxMjBiMDBhZDY2MjE5MmU2OTI5NWE4MDE3YjE1ZmJlZThmYjAzNDFhMWIwNzRiNSJ9fX0=',
    'meteor-shower': 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTE3NDFmMzhhZTNmZTU3Y2U3MTY4NjYwZjI1NjA1ZDhmOGQzMzcxZmQwYTljZjA2OTA1NDJiMjVmMGEwNTYzOCJ9fX0=',
    'space-rift': 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTYxMDVhNTU3YWY1YzNiMDE5Y2U2YjE2ODJkOThlOGZmNDgwZDkwM2M5MGE2YTk0N2Q4NzM4ZjI4YTE5NTdjYSJ9fX0='
}

# The config format has `  key:` followed by some lines, then `    texture: ''`.
for key, texture in textures.items():
    # We use regex to target the specific texture line within the trophy key block
    pattern = r'(  ' + key + r':\n(?:.*?\n)*?    texture: \'\')'
    replacement = lambda m: m.group(1).replace("texture: ''", f"texture: '{texture}'")
    text = re.sub(pattern, replacement, text)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
