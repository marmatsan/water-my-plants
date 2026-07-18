[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
Push-Location $repositoryRoot
try {
    & (Join-Path $PSScriptRoot "validate-documentation.ps1")

    $impact = & (Join-Path $PSScriptRoot "get-change-impact.ps1") -FailOnDocumentationGap -AsJson |
        ConvertFrom-Json

    if ($impact.scope -eq "documentation-only") {
        if ([string]::IsNullOrWhiteSpace($impact.comparisonBase)) {
            throw "Documentation-only verification requires a comparison base."
        }

        & git diff --check "$($impact.comparisonBase)..HEAD"
        if ($LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }

        Write-Host "Documentation-only change verified; Gradle check is not required."
        exit 0
    }

    & .\gradlew.bat check --stacktrace
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
