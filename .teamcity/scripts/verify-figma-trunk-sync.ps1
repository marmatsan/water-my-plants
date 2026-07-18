[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
$scopeFile = Join-Path $repositoryRoot "build/reports/figma-sync/sync-scope.json"
$gradleWrapper = if ([System.Environment]::OSVersion.Platform -eq [System.PlatformID]::Win32NT) {
    Join-Path $repositoryRoot "gradlew.bat"
} else {
    Join-Path $repositoryRoot "gradlew"
}
Push-Location $repositoryRoot
try {
    if (-not (Test-Path -LiteralPath $scopeFile)) {
        throw "Missing Figma Sync scope artifact: $scopeFile"
    }

    $scope = Get-Content -LiteralPath $scopeFile -Raw | ConvertFrom-Json
    $currentSha = (& git rev-parse HEAD).Trim()
    if ($scope.gitSha -ne $currentSha) {
        throw "Figma Sync scope artifact belongs to '$($scope.gitSha)', not '$currentSha'."
    }

    if ($scope.scope -ne "full-verification") {
        Write-Host "$($scope.scope) main change; Figma model and metadata are unchanged."
        exit 0
    }

    $modelFile = Join-Path $repositoryRoot "build/reports/figma-sync/design-model.json"
    if (-not (Test-Path -LiteralPath $modelFile)) {
        throw "Missing official Figma design model artifact: $modelFile"
    }

    & $gradleWrapper checkFigmaTrunkSync
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
