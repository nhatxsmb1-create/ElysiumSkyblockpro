import os

path = 'src/main/java/com/bgsoftware/superiorskyblock/module/spirits/SpiritsModule.java'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

helper_method = """
    public static String getVietnameseName(Material mat) {
        switch (mat.name()) {
            case "DIAMOND_BLOCK": return "Kh\u1ed1i Kim C\u01b0\u01a1ng";
            case "IRON_BLOCK": return "Kh\u1ed1i S\u1eaft";
            case "GOLD_BLOCK": return "Kh\u1ed1i V\u00e0ng";
            case "EMERALD_BLOCK": return "Kh\u1ed1i Ng\u1ecdc L\u1ee5c B\u1ea3o";
            case "COAL_BLOCK": return "Kh\u1ed1i Than";
            case "REDSTONE_BLOCK": return "Kh\u1ed1i \u0110\u00e1 \u0110\u1ecf";
            case "LAPIS_BLOCK": return "Kh\u1ed1i L\u01b0u Ly";
            case "NETHERITE_BLOCK": return "Kh\u1ed1i Netherite";
            case "HAY_BLOCK": return "Kh\u1ed1i R\u01a1m";
            case "MELON": return "D\u01b0a H\u1ea5u";
            case "PUMPKIN": return "B\u00ed Ng\u00f4";
            case "WHEAT": return "L\u00faa M\u00ec";
            case "CARROT": return "C\u00e0 R\u1ed1t";
            case "POTATO": return "Khoai T\u00e2y";
            case "SUGAR_CANE": return "M\u00eda";
            case "COBBLESTONE": return "\u0110\u00e1 Cu\u1ed9i";
            case "STONE": return "\u0110\u00e1";
            default:
                String[] parts = mat.name().toLowerCase().split("_");
                StringBuilder sb = new StringBuilder();
                for (String p : parts) {
                    if (p.length() > 0) {
                        sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
                    }
                }
                return sb.toString().trim();
        }
    }
"""

text = text.replace('public static SpiritsModule get() {\n        return instance;\n    }', 'public static SpiritsModule get() {\n        return instance;\n    }\n' + helper_method)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
