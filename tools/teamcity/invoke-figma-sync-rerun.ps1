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
Set-StrictMode -Version Latest

$buildTypeId = "WaterMyPlants_WaterMyPlantsFigmaSync"
$branch = "main"

function ConvertFrom-TeamCitySecret {
    param(
        [Parameter(Mandatory)][object] $Secret,
        [Parameter(Mandatory)][string] $SecretName
    )

    if ($Secret -is [string]) {
        return $Secret
    }
    $secureValue = if ($Secret -is [System.Management.Automation.PSCredential]) {
        $Secret.Password
    } elseif ($Secret -is [System.Security.SecureString]) {
        $Secret
    } else {
        throw "Secret '$SecretName' must be a SecureString or PSCredential."
    }

    $pointer = [IntPtr]::Zero
    try {
        $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureValue)
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    } finally {
        if ($pointer -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
        }
    }
}

function Get-CloudflareAccessToken {
    param(
        [Parameter(Mandatory)][System.Management.Automation.PSCredential] $Credential,
        [Parameter(Mandatory)][string] $TeamCityToken
    )

    $session = [Microsoft.PowerShell.Commands.WebRequestSession]::new()
    $headers = @{
        Accept = "application/json"
        Authorization = "Bearer $TeamCityToken"
        "CF-Access-Client-Id" = $Credential.UserName
        "CF-Access-Client-Secret" = $Credential.GetNetworkCredential().Password
    }
    Invoke-WebRequest `
        -Uri "$($ServerUrl.TrimEnd('/'))/app/rest/server" `
        -Headers $headers `
        -WebSession $session `
        -UseBasicParsing | Out-Null

    $accessCookie = $session.Cookies.GetCookies([Uri] $ServerUrl) |
        Where-Object { $_.Name -eq "CF_Authorization" } |
        Select-Object -First 1
    if ($null -eq $accessCookie -or [string]::IsNullOrWhiteSpace($accessCookie.Value)) {
        throw "Cloudflare Access did not return a CF_Authorization token."
    }
    return $accessCookie.Value
}

function Invoke-TeamCityCliJson {
    param(
        [Parameter(Mandatory)][object[]] $Arguments,
        [Parameter(Mandatory)][string] $TeamCityToken,
        [Parameter(Mandatory)][string] $CloudflareAccessToken
    )

    $executable = Get-Command teamcity.exe -ErrorAction Stop
    $environment = @{
        TEAMCITY_URL = $ServerUrl
        TEAMCITY_TOKEN = $TeamCityToken
        TEAMCITY_HEADER_CF_ACCESS_TOKEN = $CloudflareAccessToken
        TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID = $null
        TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET = $null
    }
    $previous = @{}
    foreach ($name in $environment.Keys) {
        $previous[$name] = [Environment]::GetEnvironmentVariable($name, "Process")
    }

    try {
        foreach ($name in $environment.Keys) {
            [Environment]::SetEnvironmentVariable($name, $environment[$name], "Process")
        }
        $commandArguments = @("--no-color", "--no-input") + $Arguments
        $output = (& $executable.Source @commandArguments) -join "`n"
        if ($LASTEXITCODE -ne 0) {
            throw "TeamCity CLI failed with exit code $LASTEXITCODE."
        }
        if ([string]::IsNullOrWhiteSpace($output)) {
            throw "TeamCity CLI returned no JSON output."
        }
        $result = $output | ConvertFrom-Json
        if ($null -ne $result.PSObject.Properties["error"]) {
            throw "TeamCity CLI failed: $($result.error.message)"
        }
        return $result
    } finally {
        foreach ($name in $previous.Keys) {
            [Environment]::SetEnvironmentVariable($name, $previous[$name], "Process")
        }
    }
}

function Get-ActiveFigmaSyncRun {
    foreach ($status in @("running", "queued")) {
        $response = Invoke-TeamCityCliJson `
            -Arguments @(
                "run", "list", "--job", $buildTypeId, "--branch", $branch,
                "--status", $status, "--limit", "1", "--json"
            ) `
            -TeamCityToken $teamCityToken `
            -CloudflareAccessToken $cloudflareAccessToken
        $active = @($response.build) | Where-Object { $null -ne $_ } | Select-Object -First 1
        if ($null -ne $active) {
            return $active
        }
    }
    return $null
}

function New-FigmaSyncRunResult {
    param(
        [AllowNull()][object] $Build,
        [Parameter(Mandatory)][string] $State,
        [Parameter(Mandatory)][bool] $Reused
    )

    return [pscustomobject]@{
        RunId = $(if ($null -eq $Build) { $null } else { [string] $Build.id })
        WebUrl = $(if ($null -eq $Build) { $null } else { [string] $Build.webUrl })
        Branch = $branch
        State = $State
        Reused = $Reused
    }
}

function Wait-FigmaSyncRun {
    param([Parameter(Mandatory)][string] $BuildId)

    try {
        $finished = Invoke-TeamCityCliJson `
            -Arguments @(
                "run", "watch", $BuildId, "--interval", [string] $PollIntervalSeconds,
                "--timeout", "${TimeoutMinutes}m", "--json"
            ) `
            -TeamCityToken $teamCityToken `
            -CloudflareAccessToken $cloudflareAccessToken
    } catch {
        $finished = Invoke-TeamCityCliJson `
            -Arguments @("run", "view", $BuildId, "--json") `
            -TeamCityToken $teamCityToken `
            -CloudflareAccessToken $cloudflareAccessToken
        if ($finished.state -eq "finished" -and $finished.status -ne "SUCCESS") {
            throw "TeamCity Figma Sync run $BuildId finished with status '$($finished.status)': $($finished.statusText)"
        }
        throw
    }
    if ($finished.status -ne "SUCCESS") {
        throw "TeamCity Figma Sync run $BuildId finished with status '$($finished.status)': $($finished.statusText)"
    }
    return $finished
}

if ($WhatIfPreference) {
    New-FigmaSyncRunResult -Build $null -State "Planned" -Reused $false
    return
}
if (-not $ServerUrl.StartsWith("https://", [StringComparison]::OrdinalIgnoreCase)) {
    throw "The public TeamCity automation endpoint must use HTTPS."
}
if ($null -eq (Get-Command Get-Secret -ErrorAction SilentlyContinue)) {
    throw "PowerShell SecretManagement is required."
}

$cloudflareSecret = Get-Secret -Name $CloudflareSecretName -Vault $Vault -ErrorAction Stop
if ($cloudflareSecret -isnot [System.Management.Automation.PSCredential]) {
    throw "Secret '$CloudflareSecretName' must be stored as a PSCredential."
}
$teamCitySecret = Get-Secret -Name $TeamCityTokenSecretName -Vault $Vault -ErrorAction Stop
$teamCityToken = ConvertFrom-TeamCitySecret -Secret $teamCitySecret -SecretName $TeamCityTokenSecretName

try {
    $cloudflareAccessToken = Get-CloudflareAccessToken `
        -Credential $cloudflareSecret `
        -TeamCityToken $teamCityToken
    $activeRun = Get-ActiveFigmaSyncRun

    if ($ValidateOnly) {
        New-FigmaSyncRunResult -Build $null -State "Validated" -Reused $false
        return
    }
    if ($null -ne $activeRun) {
        if ($Wait) {
            $activeRun = Wait-FigmaSyncRun -BuildId ([string] $activeRun.id)
        }
        New-FigmaSyncRunResult -Build $activeRun -State ([string] $activeRun.state) -Reused $true
        return
    }
    if (-not $PSCmdlet.ShouldProcess("$buildTypeId on $branch", "Queue TeamCity Figma Sync")) {
        New-FigmaSyncRunResult -Build $null -State "Skipped" -Reused $false
        return
    }

    try {
        $run = Invoke-TeamCityCliJson `
            -Arguments @("run", "start", $buildTypeId, "--branch", $branch, "--json") `
            -TeamCityToken $teamCityToken `
            -CloudflareAccessToken $cloudflareAccessToken
    } catch {
        $run = Get-ActiveFigmaSyncRun
        if ($null -eq $run) {
            throw
        }
    }
    if ($Wait) {
        $run = Wait-FigmaSyncRun -BuildId ([string] $run.id)
    }
    New-FigmaSyncRunResult -Build $run -State ([string] $run.state) -Reused $false
} finally {
    Remove-Variable teamCityToken,teamCitySecret,cloudflareSecret,cloudflareAccessToken -ErrorAction SilentlyContinue
}
