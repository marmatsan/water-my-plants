[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
$reportDirectory = Join-Path $repositoryRoot "build/reports/figma-sync"
$generatedConfigurationDirectory = Join-Path $repositoryRoot ".teamcity/target/generated-configs"
$toolsDirectory = Join-Path $repositoryRoot "repo/figma-design-sync/tools"
$runnerOutputDirectory = Join-Path $reportDirectory "mcp-runners"
$visualRunnerManifest = $null
$metadataRunnerManifest = $null
$visualSyncPlan = $null
$impactFile = Join-Path $reportDirectory "change-impact.json"
$isWindows = [System.Environment]::OSVersion.Platform -eq [System.PlatformID]::Win32NT
$gradleWrapper = Join-Path $repositoryRoot $(if ($isWindows) { "gradlew.bat" } else { "gradlew" })
$mavenWrapper = Join-Path $repositoryRoot $(if ($isWindows) { "mvnw.cmd" } else { "mvnw" })
$npmExecutable = if ($isWindows) { "npm.cmd" } else { "npm" }
$teamCityPom = Join-Path $repositoryRoot ".teamcity/pom.xml"
Push-Location $repositoryRoot
try {
    & $gradleWrapper classifyFigmaChangeImpact --stacktrace
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
    if (-not (Test-Path -LiteralPath $impactFile)) {
        throw "Missing Figma change-impact report: $impactFile"
    }
    $impact = Get-Content -LiteralPath $impactFile -Raw | ConvertFrom-Json

    $normalizedRepositoryRoot = [System.IO.Path]::GetFullPath($repositoryRoot).TrimEnd('\', '/') + [System.IO.Path]::DirectorySeparatorChar
    $normalizedReportDirectory = [System.IO.Path]::GetFullPath($reportDirectory)
    if (-not $normalizedReportDirectory.StartsWith($normalizedRepositoryRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to clean Figma Sync reports outside the repository: $normalizedReportDirectory"
    }
    if (Test-Path -LiteralPath $normalizedReportDirectory) {
        Remove-Item -LiteralPath $normalizedReportDirectory -Recurse -Force
    }

    New-Item -ItemType Directory -Path $reportDirectory -Force | Out-Null
    New-Item -ItemType Directory -Path $generatedConfigurationDirectory -Force | Out-Null

    if ($impact.scope -eq "full-verification") {
        & $mavenWrapper -f $teamCityPom teamcity-configs:generate
        if ($LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }

        & $gradleWrapper generateFigmaDesignModel
        if ($LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }

        Push-Location $toolsDirectory
        try {
            & $npmExecutable ci
            if ($LASTEXITCODE -ne 0) {
                exit $LASTEXITCODE
            }
            & $npmExecutable run build
            if ($LASTEXITCODE -ne 0) {
                exit $LASTEXITCODE
            }

            $modelFile = Join-Path $reportDirectory "design-model.json"
            & node dist/write-mcp-runner.mjs --mode=official --model=$modelFile --out-dir=$runnerOutputDirectory
            if ($LASTEXITCODE -ne 0) {
                exit $LASTEXITCODE
            }
            & node dist/write-mcp-runner.mjs --mode=official --model=$modelFile --target=metadata --out-dir=$runnerOutputDirectory
            if ($LASTEXITCODE -ne 0) {
                exit $LASTEXITCODE
            }
        } finally {
            Pop-Location
        }

        $runnerManifests = @(Get-ChildItem -LiteralPath $runnerOutputDirectory -Recurse -Filter "manifest.json" |
            ForEach-Object {
                [PSCustomObject]@{
                    Path = $_.FullName
                    Data = (Get-Content -LiteralPath $_.FullName -Raw | ConvertFrom-Json)
                }
            })
        $visualRunnerEntry = $runnerManifests | Where-Object { $_.Data.fullVisualSync -eq $true } | Select-Object -First 1
        $metadataRunnerEntry = $runnerManifests | Where-Object { $_.Data.writeMetadata -eq $true } | Select-Object -First 1
        $visualRunnerManifest = $visualRunnerEntry.Data
        $metadataRunnerManifest = $metadataRunnerEntry.Data
        if ($null -eq $visualRunnerManifest -or $null -eq $metadataRunnerManifest) {
            throw "Official visual and metadata MCP runner manifests were not generated."
        }

        $visualSyncPlanFile = Join-Path $reportDirectory "visual-sync-plan.json"
        & node (Join-Path $toolsDirectory "dist/write-visual-sync-plan.mjs") `
            --manifest=$($visualRunnerEntry.Path) `
            --out=$visualSyncPlanFile
        if ($LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }
        $visualSyncPlan = Get-Content -LiteralPath $visualSyncPlanFile -Raw | ConvertFrom-Json
    }

    $scope = [PSCustomObject]@{
        scope = $impact.scope
        figmaImpact = $impact.figmaImpact
        affectedVisualTargets = @($impact.affectedVisualTargets)
        comparisonBase = $impact.comparisonBase
        gitSha = (& git rev-parse HEAD).Trim()
        modelHash = $visualRunnerManifest.modelHash
        writerHash = $visualRunnerManifest.writerHash
        transportHash = $visualRunnerManifest.transportHash
        targetFingerprints = $visualRunnerManifest.targetFingerprints
        writerScopeFingerprints = $visualRunnerManifest.writerScopeFingerprints
        writerScopeFingerprintSchemaVersion = $visualRunnerManifest.writerScopeFingerprintSchemaVersion
        visualRunnerManifestHash = $visualRunnerManifest.manifestHash
        metadataRunnerManifestHash = $metadataRunnerManifest.manifestHash
        visualSyncDecision = $visualSyncPlan.decision
        visualSyncPlanHash = $visualSyncPlan.planHash
    }
    $scope | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $reportDirectory "sync-scope.json") -Encoding utf8
    Write-Host "Prepared Figma Sync scope: $($scope.scope)"
} finally {
    Pop-Location
}
