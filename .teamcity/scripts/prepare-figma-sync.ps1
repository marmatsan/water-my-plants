[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
$reportDirectory = Join-Path $repositoryRoot "build/reports/figma-sync"
$generatedConfigurationDirectory = Join-Path $repositoryRoot ".teamcity/target/generated-configs"
Push-Location $repositoryRoot
try {
    $impact = & (Join-Path $PSScriptRoot "get-change-impact.ps1") -FailOnDocumentationGap -AsJson |
        ConvertFrom-Json

    New-Item -ItemType Directory -Path $reportDirectory -Force | Out-Null
    New-Item -ItemType Directory -Path $generatedConfigurationDirectory -Force | Out-Null

    if ($impact.scope -eq "full-verification") {
        & .\mvnw.cmd -f .teamcity\pom.xml teamcity-configs:generate
        if ($LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }

        & .\gradlew.bat generateFigmaDesignModel
        if ($LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }
    }

    $scope = [PSCustomObject]@{
        scope = $impact.scope
        comparisonBase = $impact.comparisonBase
        gitSha = (& git rev-parse HEAD).Trim()
    }
    $scope | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $reportDirectory "sync-scope.json") -Encoding utf8
    Write-Host "Prepared Figma Sync scope: $($scope.scope)"
} finally {
    Pop-Location
}
