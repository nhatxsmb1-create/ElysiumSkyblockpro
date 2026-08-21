import os

path = 'src/main/resources/modules/market/config.yml'
with open(path, 'a', encoding='utf-8') as f:
    f.write('\nbuy-shop:\n')
    f.write('  DIRT:\n    category: "BUILDING"\n    buy-price: 5.0\n')
    f.write('  COBBLESTONE:\n    category: "BUILDING"\n    buy-price: 10.0\n')
    f.write('  OAK_LOG:\n    category: "BUILDING"\n    buy-price: 20.0\n')
    f.write('  GLASS:\n    category: "BUILDING"\n    buy-price: 15.0\n')
    f.write('  STONE_BRICKS:\n    category: "BUILDING"\n    buy-price: 15.0\n')
    f.write('  SEA_LANTERN:\n    category: "DECORATION"\n    buy-price: 100.0\n')
    f.write('  GLOWSTONE:\n    category: "DECORATION"\n    buy-price: 80.0\n')
    f.write('  QUARTZ_BLOCK:\n    category: "DECORATION"\n    buy-price: 150.0\n')
    f.write('  OAK_LEAVES:\n    category: "DECORATION"\n    buy-price: 5.0\n')
    f.write('  ELYTRA:\n    category: "RARES"\n    buy-price: 10000.0\n')
    f.write('  TOTEM_OF_UNDYING:\n    category: "RARES"\n    buy-price: 5000.0\n')
    f.write('  NETHER_STAR:\n    category: "RARES"\n    buy-price: 20000.0\n')
    f.write('  SPONGE:\n    category: "RARES"\n    buy-price: 500.0\n')
