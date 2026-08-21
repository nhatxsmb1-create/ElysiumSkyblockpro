import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketModule.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

vn = '''            case "NETHERITE_INGOT": return "Ph\u00f4i Netherite";
            case "NETHERITE_BLOCK": return "Kh\u1ed1i Netherite";
            case "NETHERITE_SCRAP": return "M\u1ea3nh Netherite";
            case "ANCIENT_DEBRIS": return "M\u1ea3nh V\u1ee1 C\u1ed5 \u0110\u1ea1i";'''

text = re.sub(
    r'(public static String getVietnameseName\(Material mat\) \{\s*switch \(mat\.name\(\)\) \{)',
    r'\1\n' + vn,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
