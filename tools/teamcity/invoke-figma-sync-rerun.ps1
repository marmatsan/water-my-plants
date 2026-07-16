[CmdletBinding(SupportsShouldProcess, ConfirmImpact = "Medium")]
param(
    [string] $ServerUrl = "https://teamcity.marmatsan.dev",
    [string] $Vault = "TeamCitySecrets",
    [string] $CloudflareSecretName = "TeamCityCloudflareAccess",
    [string] $TeamCityTokenSecretName = "TeamCityAutomationToken",
    [switch] $ValidateOnly,
    [switch] $Wait,
    [ValidateRange(1, 300)]
    [int] $PollIntervalSeconds = 10,
    [ValidateRange(1, 1440)]
    [int] $TimeoutMinutes = 60
)

$ErrorActionPreference = "Stop"

$modulePath = Join-Path $PSScriptRoot "TeamCityHttpsClient.psm1"
Import-Module $modulePath -Force

$parameters = @{
    ServerUrl                  = $ServerUrl
    Branch                     = "main"
    Vault                      = $Vault
    CloudflareSecretName       = $CloudflareSecretName
    TeamCityTokenSecretName    = $TeamCityTokenSecretName
    ValidateOnly               = $ValidateOnly
    Wait                       = $Wait
    PollIntervalSeconds        = $PollIntervalSeconds
    TimeoutMinutes             = $TimeoutMinutes
    Confirm                    = $false
}

if ($WhatIfPreference) {
    $parameters.WhatIf = $true
}

Invoke-FigmaSyncRerun @parameters
