$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path
$adapterScript = Join-Path $repositoryRoot ".teamcity/scripts/invoke-figma-sync-rerun.ps1"
$fixtureRoot = Join-Path ([System.IO.Path]::GetTempPath()) "water-my-plants-figma-sync-rerun-tests-$PID"
$argumentsFile = Join-Path $fixtureRoot "gradle-arguments.txt"
$environmentFile = Join-Path $fixtureRoot "gradle-environment.txt"

function Assert-Equal {
    param(
        [Parameter(Mandatory)]
        [AllowNull()]
        [object] $Actual,

        [Parameter(Mandatory)]
        [AllowNull()]
        [object] $Expected,

        [Parameter(Mandatory)]
        [string] $Message
    )

    if ($Actual -ne $Expected) {
        throw "$Message. Expected '$Expected', got '$Actual'."
    }
}

function Assert-Match {
    param(
        [Parameter(Mandatory)]
        [string] $Actual,

        [Parameter(Mandatory)]
        [string] $Pattern,

        [Parameter(Mandatory)]
        [string] $Message
    )

    if ($Actual -notmatch $Pattern) {
        throw "$Message. '$Actual' does not match '$Pattern'."
    }
}

$testCredential = [PSCredential]::new(
    "test-client-id",
    (ConvertTo-SecureString "test-client-secret" -AsPlainText -Force)
)

function Get-Secret {
    [CmdletBinding()]
    param(
        [string] $Vault,
        [string] $Name,
        [switch] $AsPlainText
    )

    Assert-Equal -Actual $Vault -Expected "TestVault" -Message "The adapter must use the selected vault"
    switch ($Name) {
        "TestTeamCityToken" {
            Assert-Equal -Actual $AsPlainText.IsPresent -Expected $true -Message "The TeamCity token must be requested as plain text"
            return "test-teamcity-token"
        }
        "TestCloudflareCredential" {
            Assert-Equal -Actual $AsPlainText.IsPresent -Expected $false -Message "The Cloudflare credential must retain its PSCredential type"
            return $testCredential
        }
        default {
            throw "Unexpected test secret '$Name'."
        }
    }
}

function Set-ProcessEnvironment {
    param(
        [Parameter(Mandatory)]
        [string] $Name,

        [AllowNull()]
        [string] $Value
    )

    [Environment]::SetEnvironmentVariable($Name, $Value, "Process")
}

$environmentVariableNames = @(
    "TEAMCITY_TOKEN",
    "TEAMCITY_HEADER_CF_ACCESS_TOKEN",
    "TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID",
    "TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET",
    "FIGMA_SYNC_WRAPPER_ARGUMENTS_FILE",
    "FIGMA_SYNC_WRAPPER_ENVIRONMENT_FILE",
    "FIGMA_SYNC_WRAPPER_EXIT_CODE"
)
$originalEnvironment = @{}
foreach ($name in $environmentVariableNames) {
    $originalEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, "Process")
}

if (Test-Path -LiteralPath $fixtureRoot) {
    Remove-Item -LiteralPath $fixtureRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $fixtureRoot -Force | Out-Null

$fakeGradleWrapper = @"
@echo off
echo %* > "%FIGMA_SYNC_WRAPPER_ARGUMENTS_FILE%"
echo TEAMCITY_TOKEN=%TEAMCITY_TOKEN%> "%FIGMA_SYNC_WRAPPER_ENVIRONMENT_FILE%"
echo TEAMCITY_HEADER_CF_ACCESS_TOKEN=%TEAMCITY_HEADER_CF_ACCESS_TOKEN%>> "%FIGMA_SYNC_WRAPPER_ENVIRONMENT_FILE%"
echo TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID=%TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID%>> "%FIGMA_SYNC_WRAPPER_ENVIRONMENT_FILE%"
echo TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET=%TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET%>> "%FIGMA_SYNC_WRAPPER_ENVIRONMENT_FILE%"
if not "%FIGMA_SYNC_WRAPPER_EXIT_CODE%"=="" exit /b %FIGMA_SYNC_WRAPPER_EXIT_CODE%
exit /b 0
"@
Set-Content -LiteralPath (Join-Path $fixtureRoot "gradlew.bat") -Value $fakeGradleWrapper -Encoding Ascii

try {
    Set-ProcessEnvironment -Name "TEAMCITY_TOKEN" -Value "previous-teamcity-token"
    Set-ProcessEnvironment -Name "TEAMCITY_HEADER_CF_ACCESS_TOKEN" -Value "stale-direct-access-token"
    Set-ProcessEnvironment -Name "TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID" -Value "previous-client-id"
    Set-ProcessEnvironment -Name "TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET" -Value "previous-client-secret"
    Set-ProcessEnvironment -Name "FIGMA_SYNC_WRAPPER_ARGUMENTS_FILE" -Value $argumentsFile
    Set-ProcessEnvironment -Name "FIGMA_SYNC_WRAPPER_ENVIRONMENT_FILE" -Value $environmentFile

    & $adapterScript `
        -RepositoryRoot $fixtureRoot `
        -VaultName "TestVault" `
        -TeamCityTokenSecretName "TestTeamCityToken" `
        -CloudflareCredentialSecretName "TestCloudflareCredential" `
        -ValidateOnly

    $arguments = Get-Content -LiteralPath $argumentsFile -Raw
    $environment = Get-Content -LiteralPath $environmentFile -Raw
    Assert-Match -Actual $arguments -Pattern "rerunTeamCityFigmaSync" -Message "The adapter must invoke the Kotlin task"
    Assert-Match -Actual $arguments -Pattern "-PfigmaTeamCityValidateOnly=true" -Message "Validation mode must not request a rerun"
    Assert-Match -Actual $environment -Pattern "TEAMCITY_TOKEN=test-teamcity-token" -Message "The adapter must expose the stored TeamCity token"
    Assert-Match -Actual $environment -Pattern "TEAMCITY_HEADER_CF_ACCESS_TOKEN=\r?\n" -Message "A stale direct Cloudflare token must not override the service credential"
    Assert-Match -Actual $environment -Pattern "TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID=test-client-id" -Message "The adapter must expose the stored Cloudflare client ID"
    Assert-Match -Actual $environment -Pattern "TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET=test-client-secret" -Message "The adapter must expose the stored Cloudflare client secret"

    Assert-Equal -Actual $env:TEAMCITY_TOKEN -Expected "previous-teamcity-token" -Message "The adapter must restore TEAMCITY_TOKEN"
    Assert-Equal -Actual $env:TEAMCITY_HEADER_CF_ACCESS_TOKEN -Expected "stale-direct-access-token" -Message "The adapter must restore the direct Cloudflare token"
    Assert-Equal -Actual $env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID -Expected "previous-client-id" -Message "The adapter must restore the Cloudflare client ID"
    Assert-Equal -Actual $env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET -Expected "previous-client-secret" -Message "The adapter must restore the Cloudflare client secret"

    & $adapterScript `
        -RepositoryRoot $fixtureRoot `
        -VaultName "TestVault" `
        -TeamCityTokenSecretName "TestTeamCityToken" `
        -CloudflareCredentialSecretName "TestCloudflareCredential"

    $arguments = Get-Content -LiteralPath $argumentsFile -Raw
    Assert-Match -Actual $arguments -Pattern "-PfigmaTeamCityWait=true" -Message "Rerun mode must wait for the Kotlin task result"
    if ($arguments -match "figmaTeamCityValidateOnly") {
        throw "Rerun mode must not enable validation-only behavior."
    }

    Set-ProcessEnvironment -Name "FIGMA_SYNC_WRAPPER_EXIT_CODE" -Value "7"
    $gradleFailurePropagated = $false
    try {
        & $adapterScript `
            -RepositoryRoot $fixtureRoot `
            -VaultName "TestVault" `
            -TeamCityTokenSecretName "TestTeamCityToken" `
            -CloudflareCredentialSecretName "TestCloudflareCredential" `
            -ValidateOnly
    }
    catch {
        $gradleFailurePropagated = $_.Exception.Message -eq "rerunTeamCityFigmaSync failed with exit code 7."
    }
    Assert-Equal -Actual $gradleFailurePropagated -Expected $true -Message "The adapter must propagate a Gradle failure"
    Assert-Equal -Actual $env:TEAMCITY_TOKEN -Expected "previous-teamcity-token" -Message "A Gradle failure must restore TEAMCITY_TOKEN"
    Assert-Equal -Actual $env:TEAMCITY_HEADER_CF_ACCESS_TOKEN -Expected "stale-direct-access-token" -Message "A Gradle failure must restore the direct Cloudflare token"
    Assert-Equal -Actual $env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID -Expected "previous-client-id" -Message "A Gradle failure must restore the Cloudflare client ID"
    Assert-Equal -Actual $env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET -Expected "previous-client-secret" -Message "A Gradle failure must restore the Cloudflare client secret"

    Write-Host "Figma Sync rerun adapter tests passed."
}
finally {
    foreach ($name in $environmentVariableNames) {
        Set-ProcessEnvironment -Name $name -Value $originalEnvironment[$name]
    }
    if (Test-Path -LiteralPath $fixtureRoot) {
        Remove-Item -LiteralPath $fixtureRoot -Recurse -Force
    }
}
