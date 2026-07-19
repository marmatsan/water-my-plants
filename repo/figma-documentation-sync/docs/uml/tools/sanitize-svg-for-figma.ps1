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
$svg = [regex]::Replace($svg, '\s+style="([^"]*)"', {
    param($match)

    $attributes = @()

    foreach ($declaration in $match.Groups[1].Value.Split(';')) {
        if ([string]::IsNullOrWhiteSpace($declaration)) {
            continue
        }

        $parts = $declaration.Split(':', 2)
        if ($parts.Count -ne 2) {
            continue
        }

        $name = $parts[0].Trim()
        $value = $parts[1].Trim()

        if ($name -match '^[a-zA-Z-]+$' -and $value -notmatch '"') {
            $attributes += " $name=`"$value`""
        }
    }

    return $attributes -join ''
})

Set-Content -LiteralPath $OutputPath -Value $svg -Encoding UTF8
