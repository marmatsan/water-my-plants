$ErrorActionPreference = "Stop"

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path
$outputFile = Join-Path $repositoryRoot "build/reports/figma-sync/change-impact.json"
$gradleWrapper = if ([System.Environment]::OSVersion.Platform -eq [System.PlatformID]::Win32NT) {
    Join-Path $repositoryRoot "gradlew.bat"
} else {
    Join-Path $repositoryRoot "gradlew"
}

function Assert-Equal([object]$Actual, [object]$Expected, [string]$Message) {
    if ($Actual -ne $Expected) {
        throw "$Message. Expected '$Expected', got '$Actual'."
    }
}

function Invoke-Impact([string[]]$Paths) {
    & $gradleWrapper `
        classifyFigmaChangeImpact `
        "-PfigmaChangedPaths=$($Paths -join ',')" `
        "-PfigmaComparisonBase=test-base" `
        --quiet
    if ($LASTEXITCODE -ne 0) {
        throw "classifyFigmaChangeImpact failed with exit code $LASTEXITCODE"
    }
    return Get-Content -LiteralPath $outputFile -Raw | ConvertFrom-Json
}

Push-Location $repositoryRoot
try {
    $documentation = Invoke-Impact @("docs/documentation.md")
    Assert-Equal $documentation.scope "documentation-only" "Documentation must be a Figma no-op"

    $transport = Invoke-Impact @("repo/figma-documentation-sync/data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/mcp/McpRunnerExecutor.kt")
    Assert-Equal $transport.scope "transport-only" "MCP transport must not rewrite Figma"

    $modelNeutral = Invoke-Impact @(".teamcity/scripts/validate-documentation.ps1")
    Assert-Equal $modelNeutral.scope "model-neutral" "Validation tooling must not regenerate the model"

    $visual = Invoke-Impact @("repo/figma-documentation-sync/tools/src/figma/figma-version-sync-gateway.ts")
    Assert-Equal $visual.figmaImpact "visual-targets" "Version writer must expose visual target impact"
    Assert-Equal @($visual.affectedVisualTargets)[0] "versions" "Version writer must target versions"

    Write-Host "classifyFigmaChangeImpact Gradle task tests passed"
} finally {
    Pop-Location
}
