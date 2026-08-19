import os
import re

def fix_vietnamese(filepath):
    if not os.path.exists(filepath): return
    
    with open('menu_translations.txt', 'r', encoding='utf-8') as f:
        translations = [line.strip().split('\t') for line in f if '\t' in line]
    
    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()

    # Create a mapping of regex to correct text
    # We only care about the right side (Vietnamese)
    # Some Vietnamese strings might be too short to safely regex, so we only do it for strings with enough characters
    
    changes = 0
    for en, vi in translations:
        if len(vi) < 3: continue
        
        # Create regex: replace any non-ASCII char in the Vietnamese string with '.'
        # e.g., 'Nâng Cấp Đảo' -> 'N[^a-zA-Z0-9]ng C[^a-zA-Z0-9]p [^a-zA-Z0-9][^a-zA-Z0-9]o'
        
        pattern = ""
        for char in vi:
            if ord(char) > 127:
                pattern += r'.'
            else:
                pattern += re.escape(char)
                
        # If the pattern contains at least one wildcard and is not just wildcards
        if '.' in pattern and not all(c == '.' or c == '\\' for c in pattern):
            # We want to replace occurrences of the pattern in the content
            # To avoid messing up YAML, we only replace it if it's inside quotes or after a colon
            # Actually, the file has it as: name: '&3&lNng C?p...'
            # We can just do a direct regex replace, but be careful of overlapping patterns
            
            # Find all matches
            matches = list(re.finditer(pattern, content))
            if matches:
                # Replace with the exact vi string
                content = re.sub(pattern, vi.replace('\\', r'\\'), content)
                changes += 1

    if changes > 0:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed {changes} strings in {filepath}")
    else:
        print(f"No changes for {filepath}")

fix_vietnamese('src/main/resources/menus/upgrades.yml')
fix_vietnamese('src/main/resources/menus/upgrades1_12.yml')
fix_vietnamese('src/main/resources/menus/upgrades1_13.yml')
fix_vietnamese('src/main/resources/menus/upgrades1_20.yml')
