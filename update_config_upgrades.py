import os

path = 'src/main/resources/modules/spirits/config.yml'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

# Add upgrades to miner
miner_upgrades = """    upgrades:
      2:
        interval-ticks: 50
        cost:
          'DIAMOND': 64
          'IRON_INGOT': 256
      3:
        interval-ticks: 40
        cost:
          'DIAMOND_BLOCK': 16
          'EMERALD': 128"""

# Add upgrades to farmer
farmer_upgrades = """    upgrades:
      2:
        interval-ticks: 80
        cost:
          'DIAMOND': 32
          'WHEAT': 500
      3:
        interval-ticks: 60
        cost:
          'DIAMOND_BLOCK': 16
          'GOLDEN_CARROT': 128"""

text = text.replace(
    "      - '&f▪ &7cấp độ Máy Tạo Quặng của Đảo.'",
    "      - '&f▪ &7cấp độ Máy Tạo Quặng của Đảo.'\n" + miner_upgrades
)

text = text.replace(
    "      - '&f▪ &7phát triển của Đảo.'",
    "      - '&f▪ &7phát triển của Đảo.'\n" + farmer_upgrades
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
