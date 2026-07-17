$ErrorActionPreference = "Stop"

$sourceRoot = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path
$testRoot = Join-Path $env:TEMP ("water-my-plants-ci-scope-" + [guid]::NewGuid())

function Assert-Equal([object]$Actual, [object]$Expected, [string]$Message) {
    if ($Actual -ne $Expected) {
        throw "$Message. Expected '$Expected', got '$Actual'."
    }
}

function Invoke-Git([string[]]$Arguments) {
    & git @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') failed"
    }
}

try {
    New-Item -ItemType Directory -Path "$testRoot/.teamcity/scripts" -Force | Out-Null
    New-Item -ItemType Directory -Path "$testRoot/docs" -Force | Out-Null
    Copy-Item -LiteralPath "$sourceRoot/.teamcity/documentation-coverage.json" -Destination "$testRoot/.teamcity/documentation-coverage.json"
    @("get-change-impact.ps1", "invoke-ci-verification.ps1", "prepare-figma-sync.ps1", "verify-figma-trunk-sync.ps1") |
        ForEach-Object {
            Copy-Item -LiteralPath "$sourceRoot/.teamcity/scripts/$_" -Destination "$testRoot/.teamcity/scripts/$_"
        }

    Push-Location $testRoot
    try {
        Invoke-Git @("init")
        Invoke-Git @("config", "user.name", "CI test")
        Invoke-Git @("config", "user.email", "ci@example.invalid")
        Set-Content -LiteralPath "docs/guide.md" -Value "# Guide"
        Invoke-Git @("add", ".")
        Invoke-Git @("commit", "-m", "test: base")
        Invoke-Git @("branch", "-M", "main")
        Invoke-Git @("remote", "add", "origin", $testRoot)
        Invoke-Git @("fetch", "origin", "main:refs/remotes/origin/main")

        Set-Content -LiteralPath "docs/guide.md" -Value "# Guide`n`nDocumented change."
        Invoke-Git @("add", "docs/guide.md")
        Invoke-Git @("commit", "-m", "docs: update guide")

        & "$testRoot/.teamcity/scripts/invoke-ci-verification.ps1"
        Assert-Equal $LASTEXITCODE 0 "Documentation-only CI verification must succeed without Gradle"

        & "$testRoot/.teamcity/scripts/prepare-figma-sync.ps1"
        Assert-Equal $LASTEXITCODE 0 "Documentation-only Figma preparation must succeed"
        $scope = Get-Content -LiteralPath "build/reports/figma-sync/sync-scope.json" -Raw | ConvertFrom-Json
        Assert-Equal $scope.scope "documentation-only" "Figma scope artifact must preserve documentation-only classification"
        Assert-Equal (Test-Path -LiteralPath "build/reports/figma-sync/design-model.json") $false "Documentation-only Figma preparation must not generate a model"

        & "$testRoot/.teamcity/scripts/verify-figma-trunk-sync.ps1"
        Assert-Equal $LASTEXITCODE 0 "Documentation-only Figma verification must be a successful no-op"
    } finally {
        Pop-Location
    }
} finally {
    if (Test-Path -LiteralPath $testRoot) {
        Remove-Item -LiteralPath $testRoot -Recurse -Force
    }
}

Write-Host "documentation-only pipeline tests passed"
