import os

path = 'src/main/resources/modules/spirits/config.yml'
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

# Replace miner texture
text = text.replace(
    "'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDRiNWI0NGM4YzVlMThkNTJkYTMwNjY2MzgzMGE1MzNlYWZhNTFiMzg5NjFkZTNmOWI2MmY0NzNkMmE3NzU1NiJ9fX0='",
    "'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjIwNzgzZTI3NDNiODk3NmIwMGY4MjdlZTI4MzY3NWQ3OTExMTQwYzk4Mjg0MjM5ZjZiYzFjOGRkMWQyN2M4MSJ9fX0='"
)

# Replace farmer texture
text = text.replace(
    "'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY3N2I4NjI4ZmRlZjY2ZTQzNjE1NGJlM2IzODFkMWRkYWRkZjlmMTllY2FlNThiZjgzYWRmNjYzMWZjYjE3In19fQ=='",
    "'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGIwMWVkZTFkYTNlOWQzMGI5ZWQ0MWM3YjFjYWZhM2Q5MTBhMjQwODZhYTllYmJhM2M2YjBhMDFhZmFhNDliZiJ9fX0='"
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
