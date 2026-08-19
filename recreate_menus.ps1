$ErrorActionPreference = 'Stop'
$utf8 = New-Object System.Text.UTF8Encoding($false)
$content = [System.IO.File]::ReadAllText('src\main\resources\menus\upgrades.yml', $utf8)

$content = $content -replace 'type: STAINED_GLASS_PANE\r?\n\s*data: 15', 'type: BLACK_STAINED_GLASS_PANE'
$content = $content -replace 'type: SKULL_ITEM\r?\n\s*data: 3', 'type: PLAYER_HEAD'
$content = $content -replace 'type: ORB_PICKUP', 'type: ENTITY_EXPERIENCE_ORB_PICKUP'
$content = $content -replace 'type: ANVIL_LAND', 'type: BLOCK_ANVIL_PLACE'
$content = $content -replace 'type: EXP_BOTTLE', 'type: EXPERIENCE_BOTTLE'

[System.IO.File]::WriteAllText('src\main\resources\menus\upgrades1_20.yml', $content, $utf8)
[System.IO.File]::WriteAllText('src\main\resources\menus\upgrades1_13.yml', $content, $utf8)

[System.IO.File]::WriteAllText('src\main\resources\menus\upgrades1_12.yml', [System.IO.File]::ReadAllText('src\main\resources\menus\upgrades.yml', $utf8), $utf8)
