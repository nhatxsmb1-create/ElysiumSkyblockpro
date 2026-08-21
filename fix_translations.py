import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketModule.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

translations = '''            case "STONE": return "\u0110\u00e1";
            case "GRANITE": return "\u0110\u00e1 Granite";
            case "DIORITE": return "\u0110\u00e1 Diorite";
            case "ANDESITE": return "\u0110\u00e1 Andesite";
            case "DEEPSLATE": return "\u0110\u00e1 \u0110en";
            case "TUFF": return "\u0110\u00e1 Ng\u01b0ng Th\u1ea1ch";
            case "SAND": return "C\u00e1t";
            case "RED_SAND": return "C\u00e1t \u0110\u1ecf";
            case "GRAVEL": return "S\u1ecfi";
            case "SPRUCE_LOG": return "G\u1ed7 V\u00e2n Sam";
            case "BIRCH_LOG": return "G\u1ed7 B\u1ea1ch D\u01b0\u01a1ng";
            case "JUNGLE_LOG": return "G\u1ed7 R\u1eebng";
            case "ACACIA_LOG": return "G\u1ed7 Xi\u00eam Gai";
            case "DARK_OAK_LOG": return "G\u1ed7 S\u1ed3i S\u1eabm";
            case "CHERRY_LOG": return "G\u1ed7 Anh \u0110\u00e0o";
            case "MANGROVE_LOG": return "G\u1ed7 \u0110\u01b0\u1edbc";
            case "BAMBOO_BLOCK": return "Kh\u1ed1i Tre";
            case "OBSIDIAN": return "H\u1eafc \u00d3c Th\u1ea1ch";
            case "CRYING_OBSIDIAN": return "H\u1eafc \u00d3c Th\u1ea1ch Kh\u00f3c";
            case "LANTERN": return "\u0110\u00e8n L\u1ed3ng";
            case "SOUL_LANTERN": return "\u0110\u00e8n L\u1ed3ng Linh H\u1ed3n";
            case "CAMPFIRE": return "L\u1eeda Tr\u1ea1i";
            case "SOUL_CAMPFIRE": return "L\u1eeda Tr\u1ea1i Linh H\u1ed3n";
            case "BOOKSHELF": return "K\u1ec7 S\u00e1ch";
            case "CHISELED_BOOKSHELF": return "K\u1ec7 S\u00e1ch \u0110i\u00eau Kh\u1eafc";
            case "PAINTING": return "Tranh V\u1ebd";
            case "ITEM_FRAME": return "Khung V\u1eadt Ph\u1ea9m";
            case "GLOW_ITEM_FRAME": return "Khung Ph\u00e1t S\u00e1ng";
            case "FLOWER_POT": return "Ch\u1eadu Hoa";
            case "BELL": return "Chu\u00f4ng";
            case "IRON_PICKAXE": return "C\u00fap S\u1eaft";
            case "IRON_AXE": return "R\u00ecu S\u1eaft";
            case "IRON_SHOVEL": return "X\u1ebbng S\u1eaft";
            case "IRON_HOE": return "Cu\u1ed1c S\u1eaft";
            case "DIAMOND_AXE": return "R\u00ecu Kim C\u01b0\u01a1ng";
            case "DIAMOND_SHOVEL": return "X\u1ebbng Kim C\u01b0\u01a1ng";
            case "DIAMOND_HOE": return "Cu\u1ed1c Kim C\u01b0\u01a1ng";
            case "NETHERITE_PICKAXE": return "C\u00fap Netherite";
            case "BUCKET": return "X\u00f4";
            case "WATER_BUCKET": return "X\u00f4 N\u01b0\u1edbc";
            case "LAVA_BUCKET": return "X\u00f4 Dung Nham";
            case "FISHING_ROD": return "C\u1ea7n C\u00e2u";
            case "FLINT_AND_STEEL": return "B\u1eadt L\u1eeda";
            case "SHEARS": return "K\u00e9o";
            case "NAME_TAG": return "Th\u1ebb T\u00ean";
            case "LEAD": return "D\u00e2y D\u1eabn";
            case "BEACON": return "\u0110\u00e8n T\u00edn Hi\u1ec7u";
            case "CONDUIT": return "\u1ed0ng D\u1eabn N\u01b0\u1edbc";
            case "SHULKER_BOX": return "H\u1ed9p Shulker";
            case "DRAGON_EGG": return "Tr\u1ee9ng R\u1ed3ng";
            case "DRAGON_HEAD": return "\u0110\u1ea7u R\u1ed3ng";
            case "WITHER_SKELETON_SKULL": return "\u0110\u1ea7u Wither";
            case "ENCHANTED_GOLDEN_APPLE": return "T\u00e1o V\u00e0ng Ph\u00f9 Ph\u00e9p";
            case "HEART_OF_THE_SEA": return "Tr\u00e1i Tim C\u1ee7a Bi\u1ec3n";
            case "ECHO_SHARD": return "M\u1ea3nh V\u1ee1 Ti\u1ebfng Vang";'''

text = re.sub(
    r'(public static String getVietnameseName\(Material mat\) \{\s*switch \(mat\.name\(\)\) \{)',
    r'\1\n' + translations,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
