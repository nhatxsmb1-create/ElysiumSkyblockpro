import os, re

path = 'src/main/resources/modules/market/config.yml'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

massive_items = '''
  # --- EXTRA BUILDING BLOCKS ---
  TERRACOTTA:
    buy-price: 20.0
    category: BUILDING
  BRICKS:
    buy-price: 25.0
    category: BUILDING
  END_STONE:
    buy-price: 30.0
    category: BUILDING
  PURPUR_BLOCK:
    buy-price: 35.0
    category: BUILDING
  PRISMARINE:
    buy-price: 40.0
    category: BUILDING
  PRISMARINE_BRICKS:
    buy-price: 45.0
    category: BUILDING
  DARK_PRISMARINE:
    buy-price: 50.0
    category: BUILDING
  NETHER_BRICKS:
    buy-price: 25.0
    category: BUILDING
  RED_NETHER_BRICKS:
    buy-price: 30.0
    category: BUILDING
  QUARTZ_BLOCK:
    buy-price: 40.0
    category: BUILDING
  SMOOTH_QUARTZ:
    buy-price: 45.0
    category: BUILDING
  BONE_BLOCK:
    buy-price: 20.0
    category: BUILDING
  COPPER_BLOCK:
    buy-price: 50.0
    category: BUILDING
  AMETHYST_BLOCK:
    buy-price: 150.0
    category: BUILDING
  CALCITE:
    buy-price: 35.0
    category: BUILDING
  DRIPSTONE_BLOCK:
    buy-price: 30.0
    category: BUILDING
    
  # --- EXTRA DECORATION ---
  TORCH:
    buy-price: 5.0
    category: DECORATION
  REDSTONE_TORCH:
    buy-price: 10.0
    category: DECORATION
  CHAIN:
    buy-price: 25.0
    category: DECORATION
  END_ROD:
    buy-price: 150.0
    category: DECORATION
  IRON_BARS:
    buy-price: 15.0
    category: DECORATION
  OAK_LEAVES:
    buy-price: 5.0
    category: DECORATION
  SPRUCE_LEAVES:
    buy-price: 5.0
    category: DECORATION
  BIRCH_LEAVES:
    buy-price: 5.0
    category: DECORATION
  JUNGLE_LEAVES:
    buy-price: 5.0
    category: DECORATION
  ACACIA_LEAVES:
    buy-price: 5.0
    category: DECORATION
  DARK_OAK_LEAVES:
    buy-price: 5.0
    category: DECORATION
  OAK_SAPLING:
    buy-price: 25.0
    category: DECORATION
  SPRUCE_SAPLING:
    buy-price: 25.0
    category: DECORATION
  BIRCH_SAPLING:
    buy-price: 25.0
    category: DECORATION
  JUNGLE_SAPLING:
    buy-price: 25.0
    category: DECORATION
  ACACIA_SAPLING:
    buy-price: 25.0
    category: DECORATION
  DARK_OAK_SAPLING:
    buy-price: 25.0
    category: DECORATION
  SUNFLOWER:
    buy-price: 50.0
    category: DECORATION
  LILAC:
    buy-price: 50.0
    category: DECORATION
  ROSE_BUSH:
    buy-price: 50.0
    category: DECORATION
  PEONY:
    buy-price: 50.0
    category: DECORATION
  WHITE_BED:
    buy-price: 100.0
    category: DECORATION
  RED_BED:
    buy-price: 150.0
    category: DECORATION
  BLACK_BED:
    buy-price: 150.0
    category: DECORATION
  WHITE_CARPET:
    buy-price: 10.0
    category: DECORATION
  RED_CARPET:
    buy-price: 15.0
    category: DECORATION
  BLACK_CARPET:
    buy-price: 15.0
    category: DECORATION

  # --- EXTRA TOOLS / EQUIPMENT ---
  DIAMOND_SWORD:
    buy-price: 1500.0
    category: TOOLS
  NETHERITE_SWORD:
    buy-price: 7500.0
    category: TOOLS
  BOW:
    buy-price: 250.0
    category: TOOLS
  CROSSBOW:
    buy-price: 500.0
    category: TOOLS
  ARROW:
    buy-price: 10.0
    category: TOOLS
  SPECTRAL_ARROW:
    buy-price: 25.0
    category: TOOLS
  SHIELD:
    buy-price: 300.0
    category: TOOLS
  DIAMOND_HELMET:
    buy-price: 2500.0
    category: TOOLS
  DIAMOND_CHESTPLATE:
    buy-price: 4000.0
    category: TOOLS
  DIAMOND_LEGGINGS:
    buy-price: 3500.0
    category: TOOLS
  DIAMOND_BOOTS:
    buy-price: 2000.0
    category: TOOLS
  NETHERITE_HELMET:
    buy-price: 12500.0
    category: TOOLS
  NETHERITE_CHESTPLATE:
    buy-price: 20000.0
    category: TOOLS
  NETHERITE_LEGGINGS:
    buy-price: 17500.0
    category: TOOLS
  NETHERITE_BOOTS:
    buy-price: 10000.0
    category: TOOLS

  # --- EXTRA RARES ---
  TRIDENT:
    buy-price: 15000.0
    category: RARES
  SPONGE:
    buy-price: 1000.0
    category: RARES
  NETHER_STAR:
    buy-price: 25000.0
    category: RARES
  EXPERIENCE_BOTTLE:
    buy-price: 150.0
    category: RARES
  GOLDEN_APPLE:
    buy-price: 1000.0
    category: RARES
  NAUTILUS_SHELL:
    buy-price: 3500.0
    category: RARES
  SCULK_SENSOR:
    buy-price: 7500.0
    category: RARES
  SCULK_SHRIEKER:
    buy-price: 10000.0
    category: RARES
'''

text = re.sub(
    r'(buy-shop:)',
    r'\1\n' + massive_items.strip('\n') + '\n',
    text,
    count=1
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
