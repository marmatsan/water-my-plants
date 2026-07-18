[CmdletBinding(DefaultParameterSetName = "Download")]
param(
    [Parameter(Mandatory, ParameterSetName = "Download")]
    [ValidateRange(1, [long]::MaxValue)]
    [long] $BuildId,

    [Parameter(Mandatory, ParameterSetName = "Existing")]
    [string] $ArtifactDirectory,

    [string] $DestinationRoot,
    [switch] $SkipExecutorBuild
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
$expectedGitSha = $null

if ($PSCmdlet.ParameterSetName -eq "Download") {
    if ($null -eq (Get-Command teamcity -ErrorAction SilentlyContinue)) {
        throw "TeamCity CLI is required to download an official artifact."
    }

    $buildJson = (& teamcity --no-color --no-input run view $BuildId --json) -join "`n"
    if ($LASTEXITCODE -ne 0) {
        throw "Could not read TeamCity build $BuildId. Verify CLI authentication and Cloudflare access."
    }
    $build = $buildJson | ConvertFrom-Json
    if ($build.state -ne "finished" -or $build.status -ne "SUCCESS") {
        throw "Build $BuildId must be finished and successful; found state '$($build.state)' and status '$($build.status)'."
    }
    if ($build.branchName -notin @("main", "<default>", "refs/heads/main")) {
        throw "Build $BuildId is not from main; found branch '$($build.branchName)'."
    }
    if ([string] $build.buildType.name -notlike "*Generate main design model*") {
        throw "Build $BuildId is '$($build.buildType.name)', not the Generate main design model job."
    }
    if ([string]::IsNullOrWhiteSpace($DestinationRoot)) {
        $DestinationRoot = Join-Path $repositoryRoot "tmp/teamcity"
    }
    $downloadDirectory = Join-Path $DestinationRoot "figma-sync-$BuildId-$([DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss'))"
    New-Item -ItemType Directory -Path $downloadDirectory -Force | Out-Null
    & teamcity --no-color --no-input run download $BuildId --output $downloadDirectory
    if ($LASTEXITCODE -ne 0) {
        throw "Could not download Figma Sync artifacts from TeamCity build $BuildId."
    }
    $sharedArchives = @(Get-ChildItem -LiteralPath $downloadDirectory -Recurse -File -Filter ".shared_files.zip")
    $downloadedModels = @(Get-ChildItem -LiteralPath $downloadDirectory -Recurse -File -Filter "design-model.json")
    if ($downloadedModels.Count -eq 0 -and $sharedArchives.Count -eq 1) {
        $expandedDirectory = Join-Path $downloadDirectory "shared-files"
        Expand-Archive -LiteralPath $sharedArchives[0].FullName -DestinationPath $expandedDirectory
    }
    elseif ($sharedArchives.Count -gt 1) {
        throw "Build $BuildId returned more than one .shared_files.zip artifact."
    }
    $ArtifactDirectory = $downloadDirectory
}

$artifactParent = Split-Path -Parent $ArtifactDirectory
$artifactName = Split-Path -Leaf $ArtifactDirectory
$validationOutput = Join-Path $artifactParent "$artifactName-validation.json"
$gradleWrapper = Join-Path $repositoryRoot $(if ($IsWindows) { "gradlew.bat" } else { "gradlew" })
$validationArguments = @(
    "validateOfficialFigmaArtifactSet",
    "-PfigmaArtifactDirectory=$ArtifactDirectory",
    "-PfigmaArtifactValidationOutput=$validationOutput"
)
if (-not [string]::IsNullOrWhiteSpace($expectedGitSha)) {
    $validationArguments += "-PfigmaExpectedGitSha=$expectedGitSha"
}
& $gradleWrapper @validationArguments
if ($LASTEXITCODE -ne 0) {
    throw "The official Figma artifact set failed Kotlin validation."
}
$handoff = Get-Content -LiteralPath $validationOutput -Raw | ConvertFrom-Json

$toolsDirectory = Join-Path $repositoryRoot "repo/figma-design-sync/tools"
$executor = Join-Path $toolsDirectory "dist/execute-mcp-runner.mjs"
if (-not $SkipExecutorBuild) {
    Push-Location $toolsDirectory
    try {
        & npm.cmd ci
        if ($LASTEXITCODE -ne 0) { throw "npm ci failed while preparing the Figma handoff." }
        & npm.cmd run build
        if ($LASTEXITCODE -ne 0) { throw "Figma sync tool build failed while preparing the handoff." }
    }
    finally {
        Pop-Location
    }
}

$dryRun = $null
$nextUnit = $null
if (Test-Path -LiteralPath $executor -PathType Leaf) {
    $dryRun = (& node $executor `
        "--manifest=$($handoff.VisualManifestPath)" `
        "--plan=$($handoff.PlanPath)" `
        --dry-run) -join "`n"
    if ($LASTEXITCODE -ne 0) { throw "The official visual manifest failed executor dry-run validation." }
    $nextUnit = (& node $executor `
        "--manifest=$($handoff.VisualManifestPath)" `
        "--plan=$($handoff.PlanPath)" `
        --next) -join "`n"
    if ($LASTEXITCODE -ne 0) { throw "The official visual manifest failed next-unit selection." }
}
elseif (-not $SkipExecutorBuild) {
    throw "Missing built Figma executor: $executor"
}

$commandPrefix = "node `"$executor`" --manifest=`"$($handoff.VisualManifestPath)`" --plan=`"$($handoff.PlanPath)`""
$summary = [pscustomobject]@{
    schemaVersion = 1
    preparedAt = [DateTime]::UtcNow.ToString("o")
    teamCityBuildId = $(if ($PSCmdlet.ParameterSetName -eq "Download") { $BuildId } else { $null })
    gitSha = $handoff.GitSha
    modelHash = $handoff.ModelHash
    decision = $handoff.Decision
    nextUnit = $nextUnit
    artifactDirectory = $handoff.ArtifactDirectory
    visualManifest = $handoff.VisualManifestPath
    metadataManifest = $handoff.MetadataManifestPath
    plan = $handoff.PlanPath
    dryRun = $dryRun
    commands = [pscustomobject]@{
        inspect = "$commandPrefix --dry-run"
        next = "$commandPrefix --next"
        recordSuccess = "$commandPrefix --record-success=`"RUNNER_FILE.mcp.js`" --summary=`"SHORT_RESULT`""
        recordFailure = "$commandPrefix --record-failure=`"RUNNER_FILE.mcp.js`" --summary=`"SHORT_ERROR`""
        rerun = "pwsh -File tools/teamcity/invoke-figma-sync-rerun.ps1 -Wait"
    }
}
$summaryPath = Join-Path $handoff.ArtifactDirectory "figma-sync-handoff.json"
$summary | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $summaryPath -Encoding utf8
$summary
Write-Host "Figma Sync handoff prepared: $summaryPath"
