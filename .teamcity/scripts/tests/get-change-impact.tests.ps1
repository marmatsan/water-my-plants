$ErrorActionPreference = "Stop"

$scriptPath = Join-Path $PSScriptRoot "../get-change-impact.ps1"

function Assert-Equal([object]$Actual, [object]$Expected, [string]$Message) {
    if ($Actual -ne $Expected) {
        throw "$Message. Expected '$Expected', got '$Actual'."
    }
}

function Invoke-Impact([string[]]$Paths) {
    return (& $scriptPath -ChangedPath $Paths -AsJson | ConvertFrom-Json)
}

$documentationOnly = Invoke-Impact @("repo/figma-design-sync/docs/runbooks/troubleshooting.md")
Assert-Equal $documentationOnly.scope "documentation-only" "Markdown-only changes must be classified as documentation-only"
Assert-Equal @($documentationOnly.documentationViolations).Count 0 "Markdown-only changes must not require extra coverage"
Assert-Equal $documentationOnly.comparisonBase $null "Explicit test paths must not invent a Git comparison base"

$coveredImplementation = Invoke-Impact @(
    "repo/figma-design-sync/tools/src/figma/figma-node-gateway.ts",
    "repo/figma-design-sync/docs/runbooks/visual-sync-contract.md"
)
Assert-Equal $coveredImplementation.scope "full-verification" "Implementation changes must retain full verification"
Assert-Equal @($coveredImplementation.documentationViolations).Count 0 "Mapped implementation documentation must satisfy coverage"

$uncoveredTeamCityChangeFailed = $false
try {
    & $scriptPath -ChangedPath @(".teamcity/settings.kts") -FailOnDocumentationGap | Out-Null
} catch {
    $uncoveredTeamCityChangeFailed = $_.Exception.Message -like "*teamcity-pipelines*"
}
Assert-Equal $uncoveredTeamCityChangeFailed $true "TeamCity changes must fail without mapped documentation"

Write-Host "get-change-impact tests passed"
