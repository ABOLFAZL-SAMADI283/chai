#Requires -Version 5
<#
  gen-icons.ps1 — تولید ست تصاویر اپ «چای» با Flux (رایگان، بدون کلید)
  خروجی: app/src/main/res/drawable-nodpi/*.webp  (کم‌حجم، ۴۰-۷۰ کیلوبایت)

  نکته: همهٔ تصاویر پس‌زمینهٔ کرم #FBF7F0 دارند تا با خود اپ یکدست دیده شوند.
  محدودیت: ۱ تصویر هر ۴۵ ثانیه (لایهٔ رایگان) → اسکریپت خودش صبر می‌کند.
#>
[CmdletBinding()]
param(
  [string]$OutDir = '',
  [int]$Size = 512,
  [switch]$Force
)

$ErrorActionPreference = 'Stop'
$inv = [System.Globalization.CultureInfo]::InvariantCulture

if (-not $OutDir) {
  $OutDir = Join-Path $env:USERPROFILE 'Documents\FocusApp\app\src\main\res\drawable-nodpi'
}
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$style = 'flat vector illustration, minimal modern style, warm cream background #FBF7F0, teal green and warm brown color palette, soft flat shadows, thick rounded shapes, centered composition, no text, no letters, high quality, square'

$items = @(
  @{ n = 'ic_chai_goal_study'; p = "a stack of books and a pencil on a wooden desk, study concept" },
  @{ n = 'ic_chai_goal_book';  p = "an open book with a cup of tea beside it, reading concept" },
  @{ n = 'ic_chai_goal_work';  p = "a laptop with charts and a coffee cup, work concept" },
  @{ n = 'ic_chai_goal_gym';   p = "a dumbbell and a water bottle, fitness concept" },
  @{ n = 'ic_chai_goal_sleep'; p = "a crescent moon with stars on a pillow, sleep concept" },
  @{ n = 'ic_chai_cup_empty';  p = "an empty ceramic cup of tea with soft steam, minimal, side view" },
  @{ n = 'ic_chai_cup_full';   p = "a ceramic cup filled with amber tea, warm, minimal, side view" },
  @{ n = 'ic_chai_lock';       p = "a simple closed padlock icon, minimal, teal green" },
  @{ n = 'ic_chai_fire';       p = "a simple flame icon, warm orange and gold, minimal" },
  @{ n = 'ic_chai_medal';      p = "a simple round medal with a ribbon, gold, minimal" },
  @{ n = 'ic_chai_leaf';       p = "a single tea leaf sprig, minimal, teal green" }
)

$idx = 0
foreach ($it in $items) {
  $webp = Join-Path $OutDir ($it.n + '.webp')
  if ((Test-Path $webp) -and -not $Force) { Write-Output ("skip (exists): " + $it.n); continue }

  $prompt = $style + ', ' + $it.p
  $url = 'https://image.pollinations.ai/prompt/' + [uri]::EscapeDataString($prompt) +
         "?width=$Size&height=$Size&model=flux&nologo=true&seed=" + (100 + $idx)

  $tmp = Join-Path $env:TEMP ("chai_" + $it.n + '.png')
  $attempt = 0
  while ($true) {
    $attempt++
    Write-Output ("[" + ($idx + 1) + "/" + $items.Count + "] " + $it.n + " (try " + $attempt + ")")
    $code = curl.exe -s -o $tmp -w "%{http_code}" --max-time 180 $url 2>$null
    if ($code -eq '200' -and (Test-Path $tmp) -and (Get-Item $tmp).Length -gt 4000) { break }
    $body = ''
    try { $body = Get-Content -LiteralPath $tmp -Raw -ErrorAction SilentlyContinue } catch { }
    if ($code -eq '402' -and $body -match 'Retry after (\d+)s') {
      $wait = [int]$Matches[1] + 4
      Write-Output ("  free limit -> wait " + $wait + "s")
      Start-Sleep -Seconds $wait
      continue
    }
    Write-Output ("  FAILED http=" + $code + " " + $body)
    break
  }

  if (Test-Path $tmp) {
    # PNG → WebP فشرده (برای حجم کم APK)
    & ffmpeg -y -hide_banner -loglevel error -i $tmp -vf "scale=${Size}:${Size}" -quality 82 -compression_level 6 $webp
    if (Test-Path $webp) {
      Write-Output ("  saved: " + (Split-Path -Leaf $webp) + " (" + [math]::Round((Get-Item $webp).Length / 1KB, 1) + " KB)")
    }
    Remove-Item $tmp -Force -ErrorAction SilentlyContinue
  }
  $idx++
}

Write-Output 'ICONS DONE'
Get-ChildItem $OutDir -Filter 'ic_chai_*.webp' | Select-Object Name, @{n='KB';e={[math]::Round($_.Length/1KB,1)}}
