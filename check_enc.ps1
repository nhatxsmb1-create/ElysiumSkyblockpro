$bytes = [System.IO.File]::ReadAllBytes('src\main\resources\menus\upgrades.yml')
$utf8Str = [System.Text.Encoding]::UTF8.GetString($bytes)
$defaultStr = [System.Text.Encoding]::Default.GetString($bytes)
Write-Output "UTF8 contains Nâng Cấp: $($utf8Str.Contains('Nâng Cấp'))"
Write-Output "Default contains Nâng Cấp: $($defaultStr.Contains('Nâng Cấp'))"
