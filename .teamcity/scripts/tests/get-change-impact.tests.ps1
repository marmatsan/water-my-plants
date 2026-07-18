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

$productDocumentationOnly = Invoke-Impact @("core/ui/docs/README.md")
Assert-Equal $productDocumentationOnly.scope "documentation-only" "Nested product-module documentation must be documentation-only"

$agentInstructionsOnly = Invoke-Impact @("repo/figma-design-sync/AGENTS.md")
Assert-Equal $agentInstructionsOnly.scope "documentation-only" "Module agent instructions must be documentation-only"

$transportOnly = Invoke-Impact @(
    "repo/figma-design-sync/tools/scripts/execute-mcp-runner.ts",
    "repo/figma-design-sync/docs/runbooks/mcp-chunk-transport.md"
)
Assert-Equal $transportOnly.scope "transport-only" "MCP transport changes must not request a visual rewrite"
Assert-Equal $transportOnly.figmaImpact "transport-only" "MCP transport changes must preserve their impact kind"

$modelNeutral = Invoke-Impact @(
    ".teamcity/scripts/validate-documentation.ps1",
    "docs/ci/documentation-coverage.md"
)
Assert-Equal $modelNeutral.scope "model-neutral" "CI validation tooling must not regenerate the Figma model"
Assert-Equal $modelNeutral.figmaImpact "model-neutral" "CI validation tooling must expose model-neutral impact"

$targetedWriter = Invoke-Impact @(
    "repo/figma-design-sync/tools/src/figma/figma-version-sync-gateway.ts",
    "repo/figma-design-sync/docs/reference/visual-sync-contract.md"
)
Assert-Equal $targetedWriter.scope "full-verification" "Visual writer changes must keep full verification"
Assert-Equal $targetedWriter.figmaImpact "visual-targets" "Visual writer changes must expose target impact"
Assert-Equal (@($targetedWriter.affectedVisualTargets) -contains "versions") $true "Version writer changes must target versions"

$coveredImplementation = Invoke-Impact @(
    "repo/figma-design-sync/tools/src/figma/figma-node-gateway.ts",
    "repo/figma-design-sync/docs/reference/visual-sync-contract.md"
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

$uncoveredModuleChangeFailed = $false
try {
    & $scriptPath -ChangedPath @("onboarding/ui/build.gradle.kts") -FailOnDocumentationGap | Out-Null
} catch {
    $uncoveredModuleChangeFailed = $_.Exception.Message -like "*product-module-structure*"
}
Assert-Equal $uncoveredModuleChangeFailed $true "Product module graph changes must fail without architecture documentation"

$coveredModuleChange = Invoke-Impact @(
    "onboarding/ui/build.gradle.kts",
    "onboarding/ui/docs/README.md"
)
Assert-Equal @($coveredModuleChange.documentationViolations).Count 0 "Module documentation must cover its build graph change"

Write-Host "get-change-impact tests passed"
