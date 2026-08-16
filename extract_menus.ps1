$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$files = Get-ChildItem (Join-Path $root 'src\main\resources\menus\*.yml') | Where-Object { $_.Name -match '^(control-panel|island-bank|top-islands|island-rate|island-ratings|border-color|member-role|member-manage|members|missions|missions-category|warp-manage|warp-categories|warp-category-manage|warp-category-icon-edit|warp-icon-edit|warps|global-warps|bank-logs|visitors|unique-visitors|confirm-transfer|confirm-disband|confirm-leave|confirm-kick|confirm-ban|banned-players|coops|counts|island-chest)' }
$out = @()
foreach ($f in $files) {
  Get-Content $f.FullName | ForEach-Object {
    if ($_ -match "^\s*(title|name|item-name):\s*'(.*)'") { $out += $Matches[2] }
    elseif ($_ -match "^\s*-\s+'(.+)'") { $out += $Matches[1] }
  }
}
$out | Sort-Object -Unique
