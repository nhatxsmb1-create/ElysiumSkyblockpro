import sys
try:
    with open('src/main/resources/menus/upgrades.yml', 'r', encoding='mbcs') as f:
        content = f.read()
    if 'Nâng Cấp' in content:
        print("mbcs works!")
    else:
        print("mbcs doesn't contain Nâng Cấp")
except Exception as e:
    print(e)
