$ErrorActionPreference = 'Stop'

function Restore-File {
    param($Path)
    
    # Read the corrupted UTF-8 file
    $brokenText = [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
    
    # The text was read as default ANSI and then saved as UTF-8. 
    # To reverse it, we take the UTF-8 string, get its characters, and cast them back to bytes using Default encoding.
    # Actually, Get-Content reads using Default encoding and creates a string.
    # Then WriteAllText saved that string as UTF-8.
    # So the string in memory was a 1:1 mapping of bytes to chars using Default encoding.
    $defaultEncoding = [System.Text.Encoding]::GetEncoding(1252) # Usually Windows-1252 for English/Western, but might be different on Vietnamese Windows (1258). Let's use [System.Text.Encoding]::Default
    $defaultEncoding = [System.Text.Encoding]::Default
    
    $bytes = $defaultEncoding.GetBytes($brokenText)
    $fixedText = [System.Text.Encoding]::UTF8.GetString($bytes)
    
    [System.IO.File]::WriteAllText($Path, $fixedText, [System.Text.Encoding]::UTF8)
    Write-Output "Restored $Path"
}

Restore-File 'src\main\resources\menus\upgrades1_12.yml'
Restore-File 'src\main\resources\menus\upgrades1_13.yml'
Restore-File 'src\main\resources\menus\upgrades1_20.yml'

# Now safely replace the generator section using proper UTF-8 reading!
$base = [System.IO.File]::ReadAllText('src\main\resources\menus\upgrades.yml', [System.Text.Encoding]::UTF8)
$genRates = [regex]::Match($base, '(?s)  generator-rates:\r?\n.*?  minecarts-limit:').Value
$genRates = $genRates.Substring(0, $genRates.Length - '  minecarts-limit:'.Length)

foreach ($file in @('upgrades1_12.yml', 'upgrades1_13.yml', 'upgrades1_20.yml')) {
    $path = 'src\main\resources\menus\' + $file
    $content = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
    
    $newContent = [regex]::Replace($content, '(?s)  generator-rates:\r?\n.*?  minecarts-limit:', $genRates + '  minecarts-limit:')
    
    if ($file -match '13|20') {
        $newContent = $newContent -replace 'type: ORB_PICKUP', 'type: ENTITY_EXPERIENCE_ORB_PICKUP'
        $newContent = $newContent -replace 'type: ANVIL_LAND', 'type: BLOCK_ANVIL_PLACE'
    }
    
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($path, $newContent, $utf8NoBom)
    Write-Output "Successfully updated $file"
}
