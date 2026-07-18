$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path
$capabilityScript = Join-Path $repositoryRoot ".teamcity/scripts/test-agent-capabilities.ps1"
$healthScript = Join-Path $repositoryRoot ".teamcity/scripts/test-ci-infrastructure-health.ps1"
$fixtureRoot = Join-Path ([System.IO.Path]::GetTempPath()) "water-my-plants-ci-health-tests"
$sdkRoot = Join-Path $fixtureRoot "android-sdk"

function Assert-True {
    param(
        [Parameter(Mandatory)]
        [bool] $Condition,

        [Parameter(Mandatory)]
        [string] $Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

if (Test-Path -LiteralPath $fixtureRoot) {
    Remove-Item -LiteralPath $fixtureRoot -Recurse -Force
}
New-Item -ItemType Directory -Path (Join-Path $sdkRoot "build-tools") -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $sdkRoot "platforms") -Force | Out-Null

try {
    $capabilityReport = Join-Path $fixtureRoot "agent-capabilities.json"
    & $capabilityScript `
        -AndroidSdkPath $sdkRoot `
        -RequiredCommands @("pwsh") `
        -ReportPath $capabilityReport
    $capabilities = Get-Content -LiteralPath $capabilityReport -Raw | ConvertFrom-Json
    Assert-True -Condition (@($capabilities.checks | Where-Object { -not $_.passed }).Count -eq 0) -Message "Expected all fixture agent capabilities to pass."

    $missingCommandFailed = $false
    try {
        & $capabilityScript `
            -AndroidSdkPath $sdkRoot `
            -RequiredCommands @("water-my-plants-command-that-does-not-exist") `
            -ReportPath (Join-Path $fixtureRoot "missing-command.json")
    }
    catch {
        $missingCommandFailed = $_.Exception.Message -like "*command:water-my-plants-command-that-does-not-exist*"
    }
    Assert-True -Condition $missingCommandFailed -Message "A missing agent command must fail closed."

    $healthReport = Join-Path $fixtureRoot "infrastructure-health.json"
    & $healthScript `
        -MinimumFreeDiskGb 1 `
        -ReportPath $healthReport `
        -SkipNetworkProbes
    $health = Get-Content -LiteralPath $healthReport -Raw | ConvertFrom-Json
    Assert-True -Condition (@($health.checks | Where-Object { $_.status -eq "failed" }).Count -eq 0) -Message "Offline infrastructure health fixture must not fail."
    Assert-True -Condition (@($health.checks | Where-Object { $_.status -eq "skipped" }).Count -eq 3) -Message "Offline infrastructure health fixture must record three skipped network checks."

    Write-Host "CI infrastructure health tests passed."
}
finally {
    if (Test-Path -LiteralPath $fixtureRoot) {
        Remove-Item -LiteralPath $fixtureRoot -Recurse -Force
    }
}
