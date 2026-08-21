import os, re

path = 'src/main/resources/modules/market/config.yml'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

new_shop_items = '''  # === BUILDING BLOCKS ===
  STONE:
    buy-price: 15.0
    category: BUILDING
  GRANITE:
    buy-price: 15.0
    category: BUILDING
  DIORITE:
    buy-price: 15.0
    category: BUILDING
  ANDESITE:
    buy-price: 15.0
    category: BUILDING
  DEEPSLATE:
    buy-price: 20.0
    category: BUILDING
  TUFF:
    buy-price: 20.0
    category: BUILDING
  SAND:
    buy-price: 10.0
    category: BUILDING
  RED_SAND:
    buy-price: 15.0
    category: BUILDING
  GRAVEL:
    buy-price: 10.0
    category: BUILDING
  SPRUCE_LOG:
    buy-price: 25.0
    category: BUILDING
  BIRCH_LOG:
    buy-price: 25.0
    category: BUILDING
  JUNGLE_LOG:
    buy-price: 25.0
    category: BUILDING
  ACACIA_LOG:
    buy-price: 25.0
    category: BUILDING
  DARK_OAK_LOG:
    buy-price: 25.0
    category: BUILDING
  CHERRY_LOG:
    buy-price: 30.0
    category: BUILDING
  MANGROVE_LOG:
    buy-price: 30.0
    category: BUILDING
  BAMBOO_BLOCK:
    buy-price: 20.0
    category: BUILDING
  OBSIDIAN:
    buy-price: 150.0
    category: BUILDING
  CRYING_OBSIDIAN:
    buy-price: 250.0
    category: BUILDING
  # === DECORATION ===
  LANTERN:
    buy-price: 45.0
    category: DECORATION
  SOUL_LANTERN:
    buy-price: 65.0
    category: DECORATION
  CAMPFIRE:
    buy-price: 50.0
    category: DECORATION
  SOUL_CAMPFIRE:
    buy-price: 70.0
    category: DECORATION
  BOOKSHELF:
    buy-price: 80.0
    category: DECORATION
  CHISELED_BOOKSHELF:
    buy-price: 120.0
    category: DECORATION
  PAINTING:
    buy-price: 40.0
    category: DECORATION
  ITEM_FRAME:
    buy-price: 30.0
    category: DECORATION
  GLOW_ITEM_FRAME:
    buy-price: 60.0
    category: DECORATION
  FLOWER_POT:
    buy-price: 20.0
    category: DECORATION
  BELL:
    buy-price: 500.0
    category: DECORATION
  # === TOOLS ===
  IRON_PICKAXE:
    buy-price: 150.0
    category: TOOLS
  IRON_AXE:
    buy-price: 150.0
    category: TOOLS
  IRON_SHOVEL:
    buy-price: 50.0
    category: TOOLS
  IRON_HOE:
    buy-price: 100.0
    category: TOOLS
  DIAMOND_AXE:
    buy-price: 1500.0
    category: TOOLS
  DIAMOND_SHOVEL:
    buy-price: 500.0
    category: TOOLS
  DIAMOND_HOE:
    buy-price: 1000.0
    category: TOOLS
  NETHERITE_PICKAXE:
    buy-price: 7500.0
    category: TOOLS
  BUCKET:
    buy-price: 50.0
    category: TOOLS
  WATER_BUCKET:
    buy-price: 100.0
    category: TOOLS
  LAVA_BUCKET:
    buy-price: 250.0
    category: TOOLS
  FISHING_ROD:
    buy-price: 80.0
    category: TOOLS
  FLINT_AND_STEEL:
    buy-price: 60.0
    category: TOOLS
  SHEARS:
    buy-price: 40.0
    category: TOOLS
  NAME_TAG:
    buy-price: 500.0
    category: TOOLS
  LEAD:
    buy-price: 150.0
    category: TOOLS
  # === RARES ===
  BEACON:
    buy-price: 15000.0
    category: RARES
  CONDUIT:
    buy-price: 8500.0
    category: RARES
  SHULKER_BOX:
    buy-price: 5000.0
    category: RARES
  DRAGON_EGG:
    buy-price: 50000.0
    category: RARES
  DRAGON_HEAD:
    buy-price: 25000.0
    category: RARES
  WITHER_SKELETON_SKULL:
    buy-price: 2500.0
    category: RARES
  ENCHANTED_GOLDEN_APPLE:
    buy-price: 7500.0
    category: RARES
  HEART_OF_THE_SEA:
    buy-price: 6000.0
    category: RARES
  ECHO_SHARD:
    buy-price: 2000.0
    category: RARES
'''

text = re.sub(
    r'(buy-shop:)',
    r'\1\n' + new_shop_items,
    text,
    count=1
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
