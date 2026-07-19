[CmdletBinding()]
param(
    [string] $RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path,
    [string] $TeamCityServerUrl = "http://127.0.0.1:8111",
    [ValidateRange(1, 1800)]
    [int] $ReadyTimeoutSeconds = 300,
    [ValidateRange(1, 60)]
    [int] $ReadyPollSeconds = 5
)

$ErrorActionPreference = "Stop"
$serverUri = [Uri] $TeamCityServerUrl
$loopbackHosts = @("localhost", "127.0.0.1", "::1")
if ($serverUri.Scheme -ne "http" -or $serverUri.Host -notin $loopbackHosts) {
    throw "The startup adapter accepts only the local HTTP TeamCity origin."
}

$gradleWrapper = Join-Path $RepositoryRoot "gradlew.bat"
if (-not (Test-Path -LiteralPath $gradleWrapper -PathType Leaf)) {
    throw "Gradle wrapper was not found at '$gradleWrapper'."
}

$readyUri = [Uri]::new($serverUri, "/healthCheck/ready")
$deadline = [DateTimeOffset]::UtcNow.AddSeconds($ReadyTimeoutSeconds)
$ready = $false
do {
    try {
        $response = Invoke-WebRequest -Uri $readyUri -Method Get -TimeoutSec 5 -UseBasicParsing
        $ready = $response.StatusCode -eq 200
    }
    catch {
        $ready = $false
    }
    if (-not $ready -and [DateTimeOffset]::UtcNow -lt $deadline) {
        Start-Sleep -Seconds $ReadyPollSeconds
    }
} while (-not $ready -and [DateTimeOffset]::UtcNow -lt $deadline)

if (-not $ready) {
    throw "TeamCity did not become ready at '$readyUri' within $ReadyTimeoutSeconds seconds."
}

$previousToken = $env:TEAMCITY_TOKEN
try {
    $env:TEAMCITY_TOKEN = Get-Secret `
        -Vault TeamCitySecrets `
        -Name TeamCityAutomationToken `
        -AsPlainText
    if ([string]::IsNullOrWhiteSpace($env:TEAMCITY_TOKEN)) {
        throw "TeamCityAutomationToken is missing from the TeamCitySecrets vault."
    }

    Push-Location $RepositoryRoot
    try {
        & $gradleWrapper `
            runTeamCityInfrastructureHealth `
            "-PteamCityInfrastructureHealthServerUrl=$TeamCityServerUrl"
        if ($LASTEXITCODE -ne 0) {
            throw "runTeamCityInfrastructureHealth failed with exit code $LASTEXITCODE."
        }
    }
    finally {
        Pop-Location
    }
}
finally {
    if ($null -eq $previousToken) {
        Remove-Item Env:TEAMCITY_TOKEN -ErrorAction SilentlyContinue
    }
    else {
        $env:TEAMCITY_TOKEN = $previousToken
    }
    Remove-Variable previousToken -ErrorAction SilentlyContinue
}
