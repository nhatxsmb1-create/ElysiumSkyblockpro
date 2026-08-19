$bytes = [System.IO.File]::ReadAllBytes('src\main\resources\menus\upgrades1_20.yml')
$str = [System.Text.Encoding]::UTF8.GetString($bytes)
if ($str.Contains("Nâng Cấp Máy Quặng")) {
    Write-Output "SUCCESS: File contains correct Vietnamese text."
} else {
    Write-Output "FAILED: Text is still corrupted."
}
