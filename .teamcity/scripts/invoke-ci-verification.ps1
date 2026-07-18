[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
$impactFile = Join-Path $repositoryRoot "build/reports/figma-sync/change-impact.json"
$gradleWrapper = if ([System.Environment]::OSVersion.Platform -eq [System.PlatformID]::Win32NT) {
    Join-Path $repositoryRoot "gradlew.bat"
} else {
    Join-Path $repositoryRoot "gradlew"
}
Push-Location $repositoryRoot
try {
    & (Join-Path $PSScriptRoot "validate-documentation.ps1") -FailOnCoverageGap

    & $gradleWrapper classifyFigmaChangeImpact --stacktrace
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
    if (-not (Test-Path -LiteralPath $impactFile)) {
        throw "Missing Figma change-impact report: $impactFile"
    }
    $impact = Get-Content -LiteralPath $impactFile -Raw | ConvertFrom-Json

    if ($impact.scope -eq "documentation-only") {
        if ([string]::IsNullOrWhiteSpace($impact.comparisonBase)) {
            throw "Documentation-only verification requires a comparison base."
        }

        & git diff --check "$($impact.comparisonBase)..HEAD"
        if ($LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }

        Write-Host "Documentation-only change verified; the full Gradle check is not required."
        exit 0
    }

    & $gradleWrapper check --stacktrace
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
