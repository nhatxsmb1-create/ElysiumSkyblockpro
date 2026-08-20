import os

def extract_generator_rates():
    with open('src/main/resources/menus/upgrades.yml', 'r', encoding='cp1258') as f:
        lines = f.readlines()
        
    start_idx = -1
    end_idx = -1
    for i, line in enumerate(lines):
        if line.startswith('  generator-rates:'):
            start_idx = i
        elif line.startswith('  minecarts-limit:'):
            end_idx = i
            break
            
    if start_idx == -1 or end_idx == -1:
        raise Exception("Could not find generator-rates or minecarts-limit in upgrades.yml")
        
    gen_rates_lines = lines[start_idx:end_idx]
    
    # Fix the item: REDSTONE to item: '+'
    for i, line in enumerate(gen_rates_lines):
        if line.strip() == "item: REDSTONE":
            gen_rates_lines[i] = line.replace("REDSTONE", "'+'")
            break
            
    return gen_rates_lines

def patch_file(filepath, gen_rates_lines, is_new_sounds=False):
    with open(filepath, 'r', encoding='cp1258') as f:
        lines = f.readlines()
        
    start_idx = -1
    end_idx = -1
    for i, line in enumerate(lines):
        if line.startswith('  generator-rates:'):
            start_idx = i
        elif line.startswith('  minecarts-limit:'):
            end_idx = i
            break
            
    if start_idx == -1 or end_idx == -1:
        print(f"Could not find bounds in {filepath}")
        return
        
    new_gen_rates = list(gen_rates_lines)
    if is_new_sounds:
        for i in range(len(new_gen_rates)):
            new_gen_rates[i] = new_gen_rates[i].replace('type: ORB_PICKUP', 'type: ENTITY_EXPERIENCE_ORB_PICKUP')
            new_gen_rates[i] = new_gen_rates[i].replace('type: ANVIL_LAND', 'type: BLOCK_ANVIL_PLACE')
            
    # Also if upgrades.yml itself is being patched, we should replace it to fix the REDSTONE issue
    
    new_lines = lines[:start_idx] + new_gen_rates + lines[end_idx:]
    
    with open(filepath, 'w', encoding='cp1258') as f:
        f.writelines(new_lines)
        
    print(f"Patched {filepath}")

gen_lines = extract_generator_rates()
patch_file('src/main/resources/menus/upgrades.yml', gen_lines, False)
patch_file('src/main/resources/menus/upgrades1_12.yml', gen_lines, False)
patch_file('src/main/resources/menus/upgrades1_13.yml', gen_lines, True)
patch_file('src/main/resources/menus/upgrades1_20.yml', gen_lines, True)

