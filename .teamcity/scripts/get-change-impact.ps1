[CmdletBinding()]
param(
    [string]$ManifestPath,
    [string[]]$ChangedPath,
    [switch]$FailOnDocumentationGap,
    [switch]$AsJson
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($ManifestPath)) {
    $ManifestPath = Join-Path $PSScriptRoot "../documentation-coverage.json"
}

function Normalize-Path([string]$Path) {
    return $Path.Trim().Replace("\\", "/")
}

function Test-PathMatchesAny([string]$Path, [object[]]$Patterns) {
    $normalizedPath = Normalize-Path $Path
    return @($Patterns | Where-Object { $normalizedPath -like (Normalize-Path $_) }).Count -gt 0
}

function Get-RepositoryChangedPaths {
    & git rev-parse --verify origin/main 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot resolve origin/main. Documentation coverage fails closed because the change scope is unknown."
    }

    $head = (& git rev-parse HEAD).Trim()
    $main = (& git rev-parse origin/main).Trim()
    if ($head -eq $main) {
        $base = (& git rev-parse "$head^").Trim()
    } else {
        $base = (& git merge-base HEAD origin/main).Trim()
    }

    return [PSCustomObject]@{
        baseRevision = $base
        paths = @(& git diff --name-only --diff-filter=ACMR "$base..$head" | ForEach-Object { Normalize-Path $_ })
    }
}

if (-not (Test-Path -LiteralPath $ManifestPath)) {
    throw "Documentation coverage manifest was not found: $ManifestPath"
}

$manifest = Get-Content -LiteralPath $ManifestPath -Raw | ConvertFrom-Json
$changeSet = if ($PSBoundParameters.ContainsKey("ChangedPath")) {
    [PSCustomObject]@{
        baseRevision = $null
        paths = @($ChangedPath | ForEach-Object { Normalize-Path $_ } | Where-Object { $_ })
    }
} else {
    Get-RepositoryChangedPaths
}
$changedPaths = @($changeSet.paths)

$violations = @()
$affectedRules = @()
foreach ($rule in @($manifest.rules)) {
    $changedSources = @($changedPaths | Where-Object { Test-PathMatchesAny $_ @($rule.sourcePaths) })
    if ($changedSources.Count -eq 0) {
        continue
    }

    $affectedRules += $rule.id
    $changedDocumentation = @($changedPaths | Where-Object { Test-PathMatchesAny $_ @($rule.documentationPaths) })
    if ($changedDocumentation.Count -eq 0) {
        $violations += [PSCustomObject]@{
            rule = $rule.id
            changedSources = $changedSources
            requiredDocumentation = @($rule.documentationPaths)
        }
    }
}

$documentationOnly = $changedPaths.Count -gt 0 -and @(
    $changedPaths | Where-Object { -not (Test-PathMatchesAny $_ @($manifest.documentationOnlyPaths)) }
).Count -eq 0

$result = [PSCustomObject]@{
    scope = if ($documentationOnly) { "documentation-only" } else { "full-verification" }
    comparisonBase = $changeSet.baseRevision
    changedPaths = $changedPaths
    affectedDocumentationRules = $affectedRules
    documentationViolations = $violations
}

if ($FailOnDocumentationGap -and $violations.Count -gt 0) {
    $details = $violations | ForEach-Object {
        "[$($_.rule)] changed: $($_.changedSources -join ', '); update one of: $($_.requiredDocumentation -join ', ')"
    }
    throw "Documentation coverage is incomplete.`n$($details -join "`n")"
}

if ($AsJson) {
    $result | ConvertTo-Json -Depth 5
} else {
    $result
}
