$ErrorActionPreference = 'Stop'
$base = Get-Content 'src\main\resources\menus\upgrades.yml' -Raw
$genRates = [regex]::Match($base, '(?s)  generator-rates:\r?\n.*?  minecarts-limit:').Value
$genRates = $genRates.Substring(0, $genRates.Length - '  minecarts-limit:'.Length)

foreach ($file in @('upgrades1_12.yml', 'upgrades1_13.yml', 'upgrades1_20.yml')) {
    $path = 'src\main\resources\menus\' + $file
    $content = Get-Content $path -Raw
    $newContent = [regex]::Replace($content, '(?s)  generator-rates:\r?\n.*?  minecarts-limit:', $genRates + '  minecarts-limit:')
    
    if ($file -match '13|20') {
        $newContent = $newContent -replace 'type: ORB_PICKUP', 'type: ENTITY_EXPERIENCE_ORB_PICKUP'
        $newContent = $newContent -replace 'type: ANVIL_LAND', 'type: BLOCK_ANVIL_PLACE'
    }
    
    # Save with UTF-8 No BOM
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText((Join-Path (Get-Location) $path), $newContent, $utf8NoBom)
    Write-Output "Updated $file"
}
