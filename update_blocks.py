import os
import re

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

def update_config_yml():
    filepath = 'src/main/resources/modules/upgrades/config.yml'
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    generator_rates = ""
    prices = [50000, 150000, 300000, 600000, 1500000, 2500000, 5000000, 10000000, 15000000, 0]
    
    for i in range(1, 11):
        chances = rates[i-1][1] if i < 10 else max_rate
        price = float(prices[i-1])
        
        generator_rates += f"""    '{i}':
      price: {price}
      price-type: "Money"
      generator-rates:
        normal:
"""
        for _, _, block, chance in chances:
            generator_rates += f"          {block}: {chance}\n"
            
        generator_rates += f"""      commands:
"""
        if i < 10:
            generator_rates += f"""        - 'island admin setupgrade %player% generator-rates {i+1}'
        - 'island admin msgall %player% &e&lUpgrades | &7Your generator was upgraded to level {i}!'\n"""
        else:
            generator_rates += f"""        - 'island admin msg %player% &e&lUpgrades | &7You have reached the maximum upgrade for generator.'\n"""

    new_content = re.sub(r'(?s)  generator-rates:\r?\n.*?  minecarts-limit:', '  generator-rates:\n' + generator_rates + '  minecarts-limit:', content)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print("Updated config.yml")


def build_menu_yaml(is_new_sounds=False):
    generator_rates = b"  generator-rates:\n    item: '+'\n"
    sound_buy = b"ENTITY_EXPERIENCE_ORB_PICKUP" if is_new_sounds else b"ORB_PICKUP"
    sound_fail = b"BLOCK_ANVIL_PLACE" if is_new_sounds else b"ANVIL_LAND"

    for i in range(1, 10):
        mat, chances, price = rates[i-1]
        mat_b = mat.encode('utf-8')
        price_b = price.encode('utf-8')
        
        generator_rates += b"    '" + str(i).encode() + b"':\n"
        generator_rates += b"      has-next-level:\n"
        generator_rates += b"        type: " + mat_b + b"\n"
        generator_rates += b"        name: '&3&lN\xc3\xa2ng C\xc3\xa1p M\xc3\xa1y Qu\xe1\xb7\xb7ng &a(C\xc3\xb3 th\xe1\xbb\x83 mua)'\n" # We'll just encode standard text as utf-8 below
        
        generator_rates += b"        name: '" + "&3&lN\xc3\xa2ng C\xc3\xa1p M\xc3\xa1y Qu\xe1\xb7\xb7ng &a(C\xc3\xb3 th\xe1\xbb\x83 mua)".encode('utf-8') + b"'\n" # Wait, manual bytes is bad.

        # LET'S JUST BUILD A PYTHON STRING AND ENCODE ENTIRE THING AS UTF-8!
        block_content = f"""    '{i}':
      has-next-level:
        type: {mat}
        name: '&3&lNâng Cấp Máy Quặng &a(Có thể mua)'
        lore:
          - '&7'
          - '&3Cấp Tiếp Theo: &e{i+1}'
          - '&7'
          - '&7Nâng cấp máy tạo quặng sẽ'
          - '&7tăng tỉ lệ ra block quặng'
          - '&7từ máy tạo quặng của đảo bạn.'
          - '&7'
          - '&3Tỉ lệ:'
"""
        for chance_str, color, _, _ in chances:
            block_content += f"          - ' &7 - &{color}{chance_str}'\n"
            
        block_content += f"""          - '&7'
          - '&3Giá: &f${price}'
          - '&7'
          - '&aNhấp để nâng cấp.'
        sound:
          type: {sound_buy.decode()}
          volume: 0.2
          pitch: 0.2
      no-next-level:
        type: {mat}
        name: '&3&lNâng Cấp Máy Quặng &c(Không đủ điều kiện)'
        lore:
          - '&7'
          - '&3Cấp Tiếp Theo: &e{i+1}'
          - '&7'
          - '&7Nâng cấp máy tạo quặng sẽ'
          - '&7tăng tỉ lệ ra block quặng'
          - '&7từ máy tạo quặng của đảo bạn.'
          - '&7'
          - '&3Tỉ lệ:'
"""
        for chance_str, color, _, _ in chances:
            block_content += f"          - ' &7 - &{color}{chance_str}'\n"
            
        block_content += f"""          - '&7'
          - '&3Giá: &f${price}'
          - '&7'
          - '&cBạn không đủ tiền.'
        sound:
          type: {sound_fail.decode()}
          volume: 0.2
          pitch: 0.2
"""
        generator_rates += block_content.encode('utf-8')

    # Max level
    block_content = f"""    '10':
      has-next-level:
        type: BEACON
        name: '&c&lCẤP TỐI ĐA'
        lore:
          - '&7Bạn đã đạt cấp độ tối đa'
          - '&7của máy tạo quặng!'
          - '&7'
          - '&3Tỉ lệ hiện tại:'
"""
    for chance_str, color, _, _ in max_rate:
        block_content += f"          - ' &7 - &{color}{chance_str}'\n"
        
    block_content += f"""        sound:
          type: {sound_fail.decode()}
          volume: 0.2
          pitch: 0.2
      no-next-level:
        type: BEACON
        name: '&c&lCẤP TỐI ĐA'
        lore:
          - '&7Bạn đã đạt cấp độ tối đa'
          - '&7của máy tạo quặng!'
          - '&7'
          - '&3Tỉ lệ hiện tại:'
"""
    for chance_str, color, _, _ in max_rate:
        block_content += f"          - ' &7 - &{color}{chance_str}'\n"
        
    block_content += f"""        sound:
          type: {sound_fail.decode()}
          volume: 0.2
          pitch: 0.2
"""
    generator_rates += block_content.encode('utf-8')
    return generator_rates


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


update_config_yml()
patch_menu_file('src/main/resources/menus/upgrades.yml', False)
patch_menu_file('src/main/resources/menus/upgrades1_12.yml', False)
patch_menu_file('src/main/resources/menus/upgrades1_13.yml', True)
patch_menu_file('src/main/resources/menus/upgrades1_20.yml', True)

