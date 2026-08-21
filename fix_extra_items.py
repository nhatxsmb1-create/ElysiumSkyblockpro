import os, re

path = 'src/main/resources/modules/market/config.yml'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

extra_items = '''
  # --- GLASS BLOCKS ---
  WHITE_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  ORANGE_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  MAGENTA_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  LIGHT_BLUE_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  YELLOW_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  LIME_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  PINK_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  GRAY_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  LIGHT_GRAY_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  CYAN_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  PURPLE_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  BLUE_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  BROWN_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  GREEN_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  RED_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
  BLACK_STAINED_GLASS:
    buy-price: 25.0
    category: BUILDING
    
  # --- CONCRETE BLOCKS ---
  WHITE_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  ORANGE_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  MAGENTA_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  LIGHT_BLUE_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  YELLOW_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  LIME_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  PINK_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  GRAY_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  LIGHT_GRAY_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  CYAN_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  PURPLE_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  BLUE_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  BROWN_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  GREEN_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  RED_CONCRETE:
    buy-price: 35.0
    category: BUILDING
  BLACK_CONCRETE:
    buy-price: 35.0
    category: BUILDING
'''

text = re.sub(
    r'(buy-shop:)',
    r'\1\n' + extra_items.strip('\n') + '\n',
    text,
    count=1
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
