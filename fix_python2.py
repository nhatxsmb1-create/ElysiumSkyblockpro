import os
import re

def fix_file(filepath, is_1_20=False, is_1_13=False):
    if not os.path.exists(filepath): return
    
    with open(filepath, 'r', encoding='mbcs') as f:
        content = f.read()

    # We will just rewrite the generator-rates section entirely!
    generator_rates = """  generator-rates:
    item: '+'
"""
    
    rates = [
        ("STONE", [("80% Đá", "f"), ("12% Than", "f"), ("8% Sắt", "f")], "50,000"),
        ("IRON_ORE", [("65% Đá", "f"), ("15% Than", "f"), ("14% Sắt", "f"), ("6% Redstone", "f")], "150,000"),
        ("LAPIS_ORE", [("50% Đá", "f"), ("15% Than", "f"), ("16% Sắt", "f"), ("11% Redstone", "f"), ("8% Lapis", "f")], "300,000"),
        ("GOLD_ORE", [("40% Đá", "f"), ("13% Than", "f"), ("17% Sắt", "f"), ("12% Redstone", "f"), ("10% Lapis", "f"), ("8% Vàng", "f")], "600,000"),
        ("DIAMOND_ORE", [("30% Đá", "f"), ("12% Than", "f"), ("18% Sắt", "f"), ("13% Redstone", "f"), ("12% Lapis", "f"), ("10% Vàng", "f"), ("5% Kim Cương", "b")], "1,500,000"),
        ("EMERALD_ORE", [("20% Đá", "f"), ("10% Than", "f"), ("18% Sắt", "f"), ("14% Redstone", "f"), ("14% Lapis", "f"), ("12% Vàng", "f"), ("10% Kim Cương", "b"), ("2% Ngọc Lục Bảo", "a")], "2,500,000"),
        ("ANCIENT_DEBRIS", [("15% Đá", "f"), ("8% Than", "f"), ("16% Sắt", "f"), ("15% Redstone", "f"), ("15% Lapis", "f"), ("14% Vàng", "f"), ("12% Kim Cương", "b"), ("3% Ngọc Lục Bảo", "a"), ("2% Mảnh Netherite", "4")], "5,000,000"),
        ("NETHERITE_BLOCK", [("10% Đá", "f"), ("5% Than", "f"), ("15% Sắt", "f"), ("16% Redstone", "f"), ("16% Lapis", "f"), ("15% Vàng", "f"), ("14% Kim Cương", "b"), ("5% Ngọc Lục Bảo", "a"), ("4% Mảnh Netherite", "4")], "10,000,000"),
        ("BEACON", [("5% Đá", "f"), ("4% Than", "f"), ("15% Sắt", "f"), ("17% Redstone", "f"), ("17% Lapis", "f"), ("16% Vàng", "f"), ("15% Kim Cương", "b"), ("6% Ngọc Lục Bảo", "a"), ("5% Mảnh Netherite", "4")], "25,000,000")
    ]
    
    max_rate = [("5% Đá", "f"), ("3% Than", "f"), ("14% Sắt", "f"), ("18% Redstone", "f"), ("17% Lapis", "f"), ("16% Vàng", "f"), ("15% Kim Cương", "b"), ("7% Ngọc Lục Bảo", "a"), ("5% Mảnh Netherite", "4")]

    sound_buy = "ENTITY_EXPERIENCE_ORB_PICKUP" if (is_1_20 or is_1_13) else "ORB_PICKUP"
    sound_fail = "BLOCK_ANVIL_PLACE" if (is_1_20 or is_1_13) else "ANVIL_LAND"

    for i in range(1, 10):
        mat, chances, price = rates[i-1]
        
        generator_rates += f"""    '{i}':
      has-next-level:
        type: {mat}
        name: '&3&lNâng Cấp Máy Quặng &a(Có thể mua)'
        lore:
          - '&7'
          - '&3Cấp Tiếp Theo: &e{i+1}'
          - '&7'
          - '&7Nâng cấp máy tạo quặng sẽ'
          - '&7tăng tỉ lệ ra quặng quý'
          - '&7từ máy tạo quặng của đảo bạn.'
          - '&7'
          - '&3Tỉ lệ:'
"""
        for chance, color in chances:
            generator_rates += f"          - ' &7 - &{color}{chance}'\n"
        
        generator_rates += f"""          - '&7'
          - '&3Giá: &f${price}'
          - '&7'
          - '&aNhấp để nâng cấp.'
        sound:
          type: {sound_buy}
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
          - '&7tăng tỉ lệ ra quặng quý'
          - '&7từ máy tạo quặng của đảo bạn.'
          - '&7'
          - '&3Tỉ lệ:'
"""
        for chance, color in chances:
            generator_rates += f"          - ' &7 - &{color}{chance}'\n"
            
        generator_rates += f"""          - '&7'
          - '&3Giá: &f${price}'
          - '&7'
          - '&cBạn không đủ tiền.'
        sound:
          type: {sound_fail}
          volume: 0.2
          pitch: 0.2
"""

    # Level 10 (Max)
    generator_rates += f"""    '10':
      has-next-level:
        type: DIAMOND_BLOCK
        name: '&c&lCẤP TỐI ĐA'
        lore:
          - '&7Bạn đã đạt cấp độ tối đa'
          - '&7của máy tạo quặng!'
          - '&7'
          - '&3Tỉ lệ hiện tại:'
"""
    for chance, color in max_rate:
        generator_rates += f"          - ' &7 - &{color}{chance}'\n"
        
    generator_rates += f"""        sound:
          type: {sound_fail}
          volume: 0.2
          pitch: 0.2
      no-next-level:
        type: DIAMOND_BLOCK
        name: '&c&lCẤP TỐI ĐA'
        lore:
          - '&7Bạn đã đạt cấp độ tối đa'
          - '&7của máy tạo quặng!'
          - '&7'
          - '&3Tỉ lệ hiện tại:'
"""
    for chance, color in max_rate:
        generator_rates += f"          - ' &7 - &{color}{chance}'\n"
        
    generator_rates += f"""        sound:
          type: {sound_fail}
          volume: 0.2
          pitch: 0.2
"""

    # Replace generator-rates section
    new_content = re.sub(r'(?s)  generator-rates:\r?\n.*?  minecarts-limit:', generator_rates + '  minecarts-limit:', content)

    # Fix border-size level 4 barrier issue
    border_fix = f"""    '4':
      has-next-level:
        type: BARRIER
        name: '&c&lCẤP TỐI ĐA'
        lore:
          - '&7Bạn đã đạt cấp độ tối đa'
          - '&7của kích thước ranh giới!'
        sound:
          type: {sound_fail}
          volume: 0.2
          pitch: 0.2
      no-next-level:
        type: BARRIER
        name: '&c&lCẤP TỐI ĐA'
        lore:
          - '&7Bạn đã đạt cấp độ tối đa'
          - '&7của kích thước ranh giới!'
        sound:
          type: {sound_fail}
          volume: 0.2
          pitch: 0.2"""
          
    new_content = re.sub(r'(?s)    \'4\':\r?\n      has-next-level:\r?\n.*?  generator-rates:', border_fix + '\n  generator-rates:', new_content)

    with open(filepath, 'w', encoding='mbcs') as f:
        f.write(new_content)
    # Use ASCII output to avoid charmap errors in powershell
    print(filepath + " fixed")

fix_file('src/main/resources/menus/upgrades.yml', False, False)
fix_file('src/main/resources/menus/upgrades1_12.yml', False, False)
fix_file('src/main/resources/menus/upgrades1_13.yml', False, True)
fix_file('src/main/resources/menus/upgrades1_20.yml', True, False)
