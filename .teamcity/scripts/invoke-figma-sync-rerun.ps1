[CmdletBinding()]
param(
    [string] $RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path,
    [string] $VaultName = "TeamCitySecrets",
    [string] $TeamCityTokenSecretName = "TeamCityAutomationToken",
    [string] $CloudflareCredentialSecretName = "TeamCityCloudflareAccess",
    [switch] $ValidateOnly
)

$ErrorActionPreference = "Stop"
$gradleWrapper = Join-Path $RepositoryRoot "gradlew.bat"
if (-not (Test-Path -LiteralPath $gradleWrapper -PathType Leaf)) {
    throw "Gradle wrapper was not found at '$gradleWrapper'."
}

Get-Command Get-Secret -ErrorAction Stop | Out-Null

$environmentVariableNames = @(
    "TEAMCITY_TOKEN",
    "TEAMCITY_HEADER_CF_ACCESS_TOKEN",
    "TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID",
    "TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET"
)
$previousEnvironment = @{}
foreach ($name in $environmentVariableNames) {
    $previousEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, "Process")
}

$cloudflareCredential = $null
$teamCityToken = $null
$cloudflareClientSecret = $null
try {
    $teamCityToken = Get-Secret `
        -Vault $VaultName `
        -Name $TeamCityTokenSecretName `
        -AsPlainText `
        -ErrorAction Stop
    if ([string]::IsNullOrWhiteSpace($teamCityToken)) {
        throw "$TeamCityTokenSecretName is missing or empty in the $VaultName vault."
    }

    $cloudflareCredential = Get-Secret `
        -Vault $VaultName `
        -Name $CloudflareCredentialSecretName `
        -ErrorAction Stop
    if ($cloudflareCredential -isnot [System.Management.Automation.PSCredential]) {
        throw "$CloudflareCredentialSecretName must be stored as a PSCredential in the $VaultName vault."
    }

    $cloudflareClientId = $cloudflareCredential.UserName
    $cloudflareClientSecret = $cloudflareCredential.GetNetworkCredential().Password
    if ([string]::IsNullOrWhiteSpace($cloudflareClientId) -or
        [string]::IsNullOrWhiteSpace($cloudflareClientSecret)) {
        throw "$CloudflareCredentialSecretName must contain a non-empty client ID and client secret."
    }

    [Environment]::SetEnvironmentVariable("TEAMCITY_TOKEN", $teamCityToken, "Process")
    [Environment]::SetEnvironmentVariable("TEAMCITY_HEADER_CF_ACCESS_TOKEN", $null, "Process")
    [Environment]::SetEnvironmentVariable("TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID", $cloudflareClientId, "Process")
    [Environment]::SetEnvironmentVariable("TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET", $cloudflareClientSecret, "Process")

    $gradleArguments = @("rerunTeamCityFigmaSync")
    if ($ValidateOnly) {
        $gradleArguments += "-PfigmaTeamCityValidateOnly=true"
    }
    else {
        $gradleArguments += "-PfigmaTeamCityWait=true"
    }

    Push-Location $RepositoryRoot
    try {
        & $gradleWrapper @gradleArguments
        if ($LASTEXITCODE -ne 0) {
            throw "rerunTeamCityFigmaSync failed with exit code $LASTEXITCODE."
        }
    }
    finally {
        Pop-Location
    }
}
finally {
    foreach ($name in $environmentVariableNames) {
        [Environment]::SetEnvironmentVariable($name, $previousEnvironment[$name], "Process")
    }

    Remove-Variable `
        cloudflareClientId, `
        cloudflareClientSecret, `
        cloudflareCredential, `
        teamCityToken `
        -ErrorAction SilentlyContinue
}
