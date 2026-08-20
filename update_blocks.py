import os
import re

def to_unicode_escape(s):
    # Convert non-ascii characters to \uXXXX
    res = ""
    for c in s:
        if ord(c) > 127:
            res += f"\\u{ord(c):04x}"
        else:
            res += c
    return res

rates = [
    ("IRON_BLOCK", [("60% Block Than", "f", "COAL_BLOCK", 60), ("40% Block Sắt", "f", "IRON_BLOCK", 40)], "50,000"),
    ("LAPIS_BLOCK", [("40% Block Than", "f", "COAL_BLOCK", 40), ("40% Block Sắt", "f", "IRON_BLOCK", 40), ("20% Block Redstone", "f", "REDSTONE_BLOCK", 20)], "150,000"),
    ("GOLD_BLOCK", [("30% Block Than", "f", "COAL_BLOCK", 30), ("30% Block Sắt", "f", "IRON_BLOCK", 30), ("25% Block Redstone", "f", "REDSTONE_BLOCK", 25), ("15% Block Lapis", "f", "LAPIS_BLOCK", 15)], "300,000"),
    ("DIAMOND_BLOCK", [("25% Block Than", "f", "COAL_BLOCK", 25), ("25% Block Sắt", "f", "IRON_BLOCK", 25), ("20% Block Redstone", "f", "REDSTONE_BLOCK", 20), ("15% Block Lapis", "f", "LAPIS_BLOCK", 15), ("15% Block Vàng", "f", "GOLD_BLOCK", 15)], "600,000"),
    ("EMERALD_BLOCK", [("20% Block Than", "f", "COAL_BLOCK", 20), ("20% Block Sắt", "f", "IRON_BLOCK", 20), ("20% Block Redstone", "f", "REDSTONE_BLOCK", 20), ("15% Block Lapis", "f", "LAPIS_BLOCK", 15), ("15% Block Vàng", "f", "GOLD_BLOCK", 15), ("10% Block Kim Cương", "b", "DIAMOND_BLOCK", 10)], "1,500,000"),
    ("NETHERITE_BLOCK", [("15% Block Than", "f", "COAL_BLOCK", 15), ("20% Block Sắt", "f", "IRON_BLOCK", 20), ("15% Block Redstone", "f", "REDSTONE_BLOCK", 15), ("15% Block Lapis", "f", "LAPIS_BLOCK", 15), ("15% Block Vàng", "f", "GOLD_BLOCK", 15), ("15% Block Kim Cương", "b", "DIAMOND_BLOCK", 15), ("5% Block Ngọc Lục Bảo", "a", "EMERALD_BLOCK", 5)], "2,500,000"),
    ("BEACON", [("10% Block Than", "f", "COAL_BLOCK", 10), ("15% Block Sắt", "f", "IRON_BLOCK", 15), ("15% Block Redstone", "f", "REDSTONE_BLOCK", 15), ("15% Block Lapis", "f", "LAPIS_BLOCK", 15), ("15% Block Vàng", "f", "GOLD_BLOCK", 15), ("15% Block Kim Cương", "b", "DIAMOND_BLOCK", 15), ("10% Block Ngọc Lục Bảo", "a", "EMERALD_BLOCK", 10), ("5% Block Netherite", "4", "NETHERITE_BLOCK", 5)], "5,000,000"),
    ("BEACON", [("5% Block Than", "f", "COAL_BLOCK", 5), ("10% Block Sắt", "f", "IRON_BLOCK", 10), ("15% Block Redstone", "f", "REDSTONE_BLOCK", 15), ("15% Block Lapis", "f", "LAPIS_BLOCK", 15), ("15% Block Vàng", "f", "GOLD_BLOCK", 15), ("20% Block Kim Cương", "b", "DIAMOND_BLOCK", 20), ("10% Block Ngọc Lục Bảo", "a", "EMERALD_BLOCK", 10), ("10% Block Netherite", "4", "NETHERITE_BLOCK", 10)], "10,000,000"),
    ("BEACON", [("5% Block Than", "f", "COAL_BLOCK", 5), ("10% Block Sắt", "f", "IRON_BLOCK", 10), ("10% Block Redstone", "f", "REDSTONE_BLOCK", 10), ("15% Block Lapis", "f", "LAPIS_BLOCK", 15), ("15% Block Vàng", "f", "GOLD_BLOCK", 15), ("20% Block Kim Cương", "b", "DIAMOND_BLOCK", 20), ("15% Block Ngọc Lục Bảo", "a", "EMERALD_BLOCK", 15), ("10% Block Netherite", "4", "NETHERITE_BLOCK", 10)], "15,000,000")
]
max_rate = [("5% Block Than", "f", "COAL_BLOCK", 5), ("5% Block Sắt", "f", "IRON_BLOCK", 5), ("10% Block Redstone", "f", "REDSTONE_BLOCK", 10), ("10% Block Lapis", "f", "LAPIS_BLOCK", 10), ("15% Block Vàng", "f", "GOLD_BLOCK", 15), ("25% Block Kim Cương", "b", "DIAMOND_BLOCK", 25), ("15% Block Ngọc Lục Bảo", "a", "EMERALD_BLOCK", 15), ("15% Block Netherite", "4", "NETHERITE_BLOCK", 15)]

def build_menu_yaml(is_new_sounds=False):
    generator_rates = "  generator-rates:\n    item: '+'\n"
    sound_buy = "ENTITY_EXPERIENCE_ORB_PICKUP" if is_new_sounds else "ORB_PICKUP"
    sound_fail = "BLOCK_ANVIL_PLACE" if is_new_sounds else "ANVIL_LAND"

    for i in range(1, 10):
        mat, chances, price = rates[i-1]
        
        block_content = f"""    '{i}':
      has-next-level:
        type: {mat}
        name: "{to_unicode_escape('&3&lNâng Cấp Máy Quặng &a(Có thể mua)')}"
        lore:
          - "&7"
          - "{to_unicode_escape('&3Cấp Tiếp Theo: &e' + str(i+1))}"
          - "&7"
          - "{to_unicode_escape('&7Nâng cấp máy tạo quặng sẽ')}"
          - "{to_unicode_escape('&7tăng tỉ lệ ra block quặng')}"
          - "{to_unicode_escape('&7từ máy tạo quặng của đảo bạn.')}"
          - "&7"
          - "{to_unicode_escape('&3Tỉ lệ:')}"
"""
        for chance_str, color, _, _ in chances:
            block_content += f"""          - "{to_unicode_escape(' &7 - &' + color + chance_str)}"
"""
            
        block_content += f"""          - "&7"
          - "&3Gi\\u00e1: &f${price}"
          - "&7"
          - "{to_unicode_escape('&aNhấp để nâng cấp.')}"
        sound:
          type: {sound_buy}
          volume: 0.2
          pitch: 0.2
      no-next-level:
        type: {mat}
        name: "{to_unicode_escape('&3&lNâng Cấp Máy Quặng &c(Không đủ điều kiện)')}"
        lore:
          - "&7"
          - "{to_unicode_escape('&3Cấp Tiếp Theo: &e' + str(i+1))}"
          - "&7"
          - "{to_unicode_escape('&7Nâng cấp máy tạo quặng sẽ')}"
          - "{to_unicode_escape('&7tăng tỉ lệ ra block quặng')}"
          - "{to_unicode_escape('&7từ máy tạo quặng của đảo bạn.')}"
          - "&7"
          - "{to_unicode_escape('&3Tỉ lệ:')}"
"""
        for chance_str, color, _, _ in chances:
            block_content += f"""          - "{to_unicode_escape(' &7 - &' + color + chance_str)}"
"""
            
        block_content += f"""          - "&7"
          - "&3Gi\\u00e1: &f${price}"
          - "&7"
          - "{to_unicode_escape('&cBạn không đủ tiền.')}"
        sound:
          type: {sound_fail}
          volume: 0.2
          pitch: 0.2
"""
        generator_rates += block_content

    # Max level
    block_content = f"""    '10':
      has-next-level:
        type: BEACON
        name: "{to_unicode_escape('&c&lCẤP TỐI ĐA')}"
        lore:
          - "{to_unicode_escape('&7Bạn đã đạt cấp độ tối đa')}"
          - "{to_unicode_escape('&7của máy tạo quặng!')}"
          - "&7"
          - "{to_unicode_escape('&3Tỉ lệ hiện tại:')}"
"""
    for chance_str, color, _, _ in max_rate:
        block_content += f"""          - "{to_unicode_escape(' &7 - &' + color + chance_str)}"
"""
        
    block_content += f"""        sound:
          type: {sound_fail}
          volume: 0.2
          pitch: 0.2
      no-next-level:
        type: BEACON
        name: "{to_unicode_escape('&c&lCẤP TỐI ĐA')}"
        lore:
          - "{to_unicode_escape('&7Bạn đã đạt cấp độ tối đa')}"
          - "{to_unicode_escape('&7của máy tạo quặng!')}"
          - "&7"
          - "{to_unicode_escape('&3Tỉ lệ hiện tại:')}"
"""
    for chance_str, color, _, _ in max_rate:
        block_content += f"""          - "{to_unicode_escape(' &7 - &' + color + chance_str)}"
"""
        
    block_content += f"""        sound:
          type: {sound_fail}
          volume: 0.2
          pitch: 0.2
"""
    generator_rates += block_content
    return generator_rates.encode('ascii')


def patch_menu_file(filepath, is_new_sounds=False):
    with open(filepath, 'rb') as f:
        content = f.read()
        
    start_idx = content.find(b'  generator-rates:\r\n')
    if start_idx == -1:
        start_idx = content.find(b'  generator-rates:\n')
        
    end_idx = content.find(b'  minecarts-limit:\r\n')
    if end_idx == -1:
        end_idx = content.find(b'  minecarts-limit:\n')
        
    if start_idx == -1 or end_idx == -1:
        print(f"Could not patch {filepath}")
        return
        
    new_content = content[:start_idx] + build_menu_yaml(is_new_sounds) + content[end_idx:]
    with open(filepath, 'wb') as f:
        f.write(new_content)
    print(f"Updated {filepath}")


patch_menu_file('src/main/resources/menus/upgrades.yml', False)
patch_menu_file('src/main/resources/menus/upgrades1_12.yml', False)
patch_menu_file('src/main/resources/menus/upgrades1_13.yml', True)
patch_menu_file('src/main/resources/menus/upgrades1_20.yml', True)

