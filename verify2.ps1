$bytes = [System.IO.File]::ReadAllBytes('src\main\resources\menus\upgrades.yml')
$str = [System.Text.Encoding]::UTF8.GetString($bytes)
if ($str.Contains("Nâng Cấp")) {
    Write-Output "SUCCESS: upgrades.yml is correct."
} else {
    Write-Output "FAILED: upgrades.yml is corrupted too!"
}
