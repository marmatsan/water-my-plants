[CmdletBinding()]
param(
    [string] $TeamCityServerUrl = $env:TEAMCITY_SERVER_URL,
    [string] $PublicTeamCityUrl = "https://teamcity.marmatsan.dev",
    [ValidateRange(1, 1024)]
    [int] $MinimumFreeDiskGb = 10,
    [string] $ReportPath,
    [switch] $SkipNetworkProbes
)

$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
if ([string]::IsNullOrWhiteSpace($ReportPath)) {
    $ReportPath = Join-Path $repositoryRoot "build/reports/ci-health/infrastructure-health.json"
}

$checks = [System.Collections.Generic.List[object]]::new()

function Add-HealthCheck {
    param(
        [Parameter(Mandatory)]
        [string] $Name,

        [Parameter(Mandatory)]
        [string] $Status,

        [Parameter(Mandatory)]
        [string] $Detail
    )

    $checks.Add([pscustomobject]@{
        name = $Name
        status = $Status
        detail = $Detail
    })
}

function Test-HttpBoundary {
    param(
        [Parameter(Mandatory)]
        [string] $Name,

        [Parameter(Mandatory)]
        [string] $Url,

        [Parameter(Mandatory)]
        [int[]] $ExpectedStatusCodes
    )

    $curl = Get-Command "curl.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -eq $curl) {
        Add-HealthCheck -Name $Name -Status "failed" -Detail "curl.exe is not available on PATH."
        return
    }

    try {
        $statusCodeText = & $curl.Source `
            --silent `
            --show-error `
            --output NUL `
            --write-out "%{http_code}" `
            --max-time 20 `
            $Url
        if ($LASTEXITCODE -ne 0) {
            Add-HealthCheck -Name $Name -Status "failed" -Detail "curl.exe exited with code $LASTEXITCODE for $Url."
            return
        }

        $statusCode = 0
        if (-not [int]::TryParse(([string] $statusCodeText).Trim(), [ref] $statusCode)) {
            Add-HealthCheck -Name $Name -Status "failed" -Detail "Unexpected HTTP status '$statusCodeText' for $Url."
            return
        }

        $passed = $ExpectedStatusCodes -contains $statusCode
        Add-HealthCheck `
            -Name $Name `
            -Status $(if ($passed) { "passed" } else { "failed" }) `
            -Detail "HTTP $statusCode from $Url; expected $($ExpectedStatusCodes -join ', ')."
    }
    catch {
        Add-HealthCheck -Name $Name -Status "failed" -Detail $_.Exception.Message
    }
}

$repositoryDriveName = ([System.IO.Path]::GetPathRoot($repositoryRoot)).TrimEnd('\').TrimEnd(':')
$repositoryDrive = Get-PSDrive -Name $repositoryDriveName -ErrorAction Stop
$freeDiskGb = [Math]::Round($repositoryDrive.Free / 1GB, 2)
Add-HealthCheck `
    -Name "agent-disk-space" `
    -Status $(if ($freeDiskGb -ge $MinimumFreeDiskGb) { "passed" } else { "failed" }) `
    -Detail "$freeDiskGb GiB free on $repositoryDriveName`:; minimum is $MinimumFreeDiskGb GiB."

if ($SkipNetworkProbes) {
    Add-HealthCheck -Name "teamcity-ready" -Status "skipped" -Detail "Network probes were explicitly skipped."
    Add-HealthCheck -Name "cloudflare-access" -Status "skipped" -Detail "Network probes were explicitly skipped."
    Add-HealthCheck -Name "github-webhook-route" -Status "skipped" -Detail "Network probes were explicitly skipped."
}
else {
    if ([string]::IsNullOrWhiteSpace($TeamCityServerUrl)) {
        Add-HealthCheck -Name "teamcity-ready" -Status "failed" -Detail "TEAMCITY_SERVER_URL is not configured."
    }
    else {
        Test-HttpBoundary `
            -Name "teamcity-ready" `
            -Url "$($TeamCityServerUrl.TrimEnd('/'))/healthCheck/ready" `
            -ExpectedStatusCodes @(200)
    }

    Test-HttpBoundary `
        -Name "cloudflare-access" `
        -Url $PublicTeamCityUrl `
        -ExpectedStatusCodes @(301, 302, 303, 307, 308, 401, 403)
    Test-HttpBoundary `
        -Name "github-webhook-route" `
        -Url "$($PublicTeamCityUrl.TrimEnd('/'))/app/webhooks/githubapp" `
        -ExpectedStatusCodes @(400)
}

$reportDirectory = Split-Path -Parent $ReportPath
New-Item -ItemType Directory -Path $reportDirectory -Force | Out-Null
$report = [pscustomobject]@{
    schemaVersion = 1
    generatedAt = [DateTime]::UtcNow.ToString("o")
    machine = [System.Environment]::MachineName
    teamCityServerUrl = $TeamCityServerUrl
    publicTeamCityUrl = $PublicTeamCityUrl
    checks = @($checks)
}
$report | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $ReportPath -Encoding utf8

$failures = @($checks | Where-Object { $_.status -eq "failed" })
if ($failures.Count -gt 0) {
    $failureSummary = $failures | ForEach-Object { "$($_.name): $($_.detail)" }
    throw "CI infrastructure health validation failed:`n$($failureSummary -join "`n")"
}

Write-Host "CI infrastructure health validation passed. Report: $ReportPath"
