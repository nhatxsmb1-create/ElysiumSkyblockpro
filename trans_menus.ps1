$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$menus = Join-Path $root 'src\main\resources\menus'

# Load UTF-8 mapping: key<TAB>value
$mapFile = Join-Path $root 'menu_translations.txt'
$map = @{}
[System.IO.File]::ReadAllLines($mapFile, [System.Text.Encoding]::UTF8) | ForEach-Object {
    if ($_ -match '^(.*?)\t(.*)$') { $map[$Matches[1]] = $Matches[2] }
}

$prefixes = @('control-panel','island-bank','top-islands','island-rate','island-ratings','border-color','member-role','member-manage','members','missions-category','missions','warp-manage','warp-categories','warp-category-manage','warp-category-icon-edit','warp-icon-edit','warps','global-warps','bank-logs','visitors','unique-visitors','confirm-transfer','confirm-disband','confirm-leave','confirm-kick','confirm-ban','banned-players','coops','counts','island-chest')

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$files = Get-ChildItem (Join-Path $menus '*.yml') | Where-Object {
    $n = $_.Name
    foreach ($p in $prefixes) { if ($n -eq "$p.yml" -or $n -match "^$p\d+_\d+\.yml$") { return $true } }
    return $false
}

$changedCount = 0
foreach ($f in $files) {
    $raw = [System.IO.File]::ReadAllText($f.FullName, [System.Text.Encoding]::UTF8)
    $nl = if ($raw.Contains("`r`n")) { "`r`n" } else { "`n" }
    $lines = $raw -split "`r?`n"
    $section = ''
    $out = New-Object System.Collections.Generic.List[string]
    $changed = $false
    foreach ($line in $lines) {
        if ($line -match '^([A-Za-z_][A-Za-z0-9_-]*):') { $section = $Matches[1] }
        if ($line -match "^(\s*(?:title|item-name|name):\s*)'(.*)'\s*$") {
            $pre = $Matches[1]; $inner = $Matches[2]
            if ($map.ContainsKey($inner)) { $line = $pre + "'" + $map[$inner] + "'"; $changed = $true }
        }
        elseif (($section -eq 'items') -and ($line -match "^(\s*-\s*)'(.*)'\s*$")) {
            $pre = $Matches[1]; $inner = $Matches[2]
            if ($map.ContainsKey($inner)) { $line = $pre + "'" + $map[$inner] + "'"; $changed = $true }
        }
        $out.Add($line)
    }
    if ($changed) {
        [System.IO.File]::WriteAllText($f.FullName, ($out -join $nl), $utf8NoBom)
        $changedCount++
        Write-Output ("TRANSLATED: " + $f.Name)
    } else {
        Write-Output ("NO CHANGES: " + $f.Name)
    }
}
Write-Output ("Total changed: " + $changedCount + " of " + $files.Count)
