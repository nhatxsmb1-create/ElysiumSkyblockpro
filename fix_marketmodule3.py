import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketModule.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

vn_names = '''            case "DIRT": return "\u0110\u1ea5t";
            case "OAK_LOG": return "G\u1ed7 S\u1ed3i";
            case "GLASS": return "K\u00ednh";
            case "STONE_BRICKS": return "G\u1ea1ch \u0110\u00e1";
            case "SEA_LANTERN": return "\u0110\u00e8n Bi\u1ec3n";
            case "GLOWSTONE": return "\u0110\u00e1 Ph\u00e1t S\u00e1ng";
            case "QUARTZ_BLOCK": return "Kh\u1ed1i Th\u1ea1ch Anh";
            case "OAK_LEAVES": return "L\u00e1 C\u00e2y";
            case "ELYTRA": return "C\u00e1nh Elytra";
            case "TOTEM_OF_UNDYING": return "Totem B\u1ea5t T\u1eed";
            case "NETHER_STAR": return "Sao Nether";
            case "SPONGE": return "M\u00fat X\u1ed1p";'''

text = re.sub(
    r'(public static String getVietnameseName\(Material mat\) \{\s*switch \(mat\.name\(\)\) \{)',
    r'\1\n' + vn_names,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
