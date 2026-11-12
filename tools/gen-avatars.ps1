#Requires -Version 5
<#
  gen-avatars.ps1 — ساخت آواتارهای کارتونی هم‌سبک برای لیگ و پروفایل
  خروجی: app/src/main/res/drawable-nodpi/av{1..4}.webp
#>
[CmdletBinding()]
param(
  [string]$OutDir = '',
  [int]$Size = 512
)

$ErrorActionPreference = 'Stop'
if (-not $OutDir) {
  $OutDir = Join-Path $env:USERPROFILE 'Documents\FocusApp\app\src\main\res\drawable-nodpi'
}
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$style = 'cute cartoon character portrait, flat vector illustration, round face avatar, warm cream background #FBF7F0, teal green and warm brown palette, simple friendly features, centered, no text, square'

$items = @(
  @{ n = 'av1'; p = 'a cheerful young man with short brown hair wearing a green hoodie' },
  @{ n = 'av2'; p = 'a young woman with dark hair wearing a beige sweater' },
  @{ n = 'av3'; p = 'a young man with curly hair and glasses wearing a white shirt' },
  @{ n = 'av4'; p = 'a young woman with a bun hairstyle wearing a brown scarf' }
)

foreach ($it in $items) {
  $webp = Join-Path $OutDir ($it.n + '.webp')
  if (Test-Path $webp) { Write-Output ("skip: " + $it.n); continue }

  $prompt = $style + ', ' + $it.p
  $url = 'https://image.pollinations.ai/prompt/' + [uri]::EscapeDataString($prompt) +
         "?width=$Size&height=$Size&model=flux&nologo=true&seed=" + (300 + [int]$it.n.Substring(2))

  $tmp = Join-Path $env:TEMP ("chai_" + $it.n + '.png')
  $attempt = 0
  while ($true) {
    $attempt++
    Write-Output ($it.n + " (try " + $attempt + ")")
    $code = curl.exe -s -o $tmp -w "%{http_code}" --max-time 180 $url 2>$null
    if ($code -eq '200' -and (Test-Path $tmp) -and (Get-Item $tmp).Length -gt 4000) { break }
    $body = ''
    try { $body = Get-Content -LiteralPath $tmp -Raw -ErrorAction SilentlyContinue } catch { }
    if ($code -eq '402' -and $body -match 'Retry after (\d+)s') {
      Start-Sleep -Seconds ([int]$Matches[1] + 4); continue
    }
    Write-Output ("  FAILED " + $code); break
  }
  if (Test-Path $tmp) {
    & ffmpeg -y -hide_banner -loglevel error -i $tmp -vf "scale=${Size}:${Size}" -quality 82 $webp
    if (Test-Path $webp) { Write-Output ("  saved " + $it.n + " (" + [math]::Round((Get-Item $webp).Length / 1KB, 1) + " KB)") }
    Remove-Item $tmp -Force -ErrorAction SilentlyContinue
  }
}
Write-Output 'AVATARS DONE'
