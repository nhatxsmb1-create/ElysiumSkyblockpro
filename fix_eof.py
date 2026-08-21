import os, re

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/market/MarketModule.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

replacement = '''            default:
                String[] words = mat.name().split("_");
                StringBuilder sb = new StringBuilder();
                for (String word : words) {
                    if (word.length() > 0) {
                        sb.append(Character.toUpperCase(word.charAt(0)));
                        if (word.length() > 1) {
                            sb.append(word.substring(1).toLowerCase());
                        }
                        sb.append(" ");
                    }
                }
                return sb.toString().trim();
        }
    }
}'''

text = re.sub(
    r'            default:\s*String\[\] words = mat\.name\(\)\.split\("_["]\);.*',
    replacement,
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
