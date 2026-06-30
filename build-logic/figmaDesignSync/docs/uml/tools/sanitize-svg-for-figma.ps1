param(
    [Parameter(Mandatory = $true)]
    [string] $InputPath,

    [Parameter(Mandatory = $true)]
    [string] $OutputPath
)

$resolvedInput = Resolve-Path -LiteralPath $InputPath
$outputDirectory = Split-Path -Parent $OutputPath

if ($outputDirectory) {
    New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
}

$svg = Get-Content -LiteralPath $resolvedInput -Raw

$svg = $svg -replace '<!--[\s\S]*?-->', ''
$svg = $svg -replace '<title>[^<]*</title>', ''
$svg = $svg -replace '<defs/>', ''
$svg = $svg -replace '\s+xmlns:xlink="[^"]*"', ''
$svg = $svg -replace '\s+contentStyleType="[^"]*"', ''
$svg = $svg -replace '\s+data-diagram-type="[^"]*"', ''
$svg = $svg -replace '\s+zoomAndPan="[^"]*"', ''
$svg = $svg -replace '\s+version="[^"]*"', ''
$svg = $svg -replace '\s+style="width:[^"]+"', ''
$svg = $svg -replace ' style="stroke:([^;]+);stroke-width:([^;]+);"', ' stroke="$1" stroke-width="$2"'

Set-Content -LiteralPath $OutputPath -Value $svg -Encoding UTF8
