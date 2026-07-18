[CmdletBinding()]
param(
    [string] $AndroidSdkPath,
    [string[]] $RequiredCommands = @("git", "java", "powershell.exe"),
    [switch] $RequireNode,
    [switch] $ExportTeamCityParameters,
    [string] $ReportPath
)

$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
if ([string]::IsNullOrWhiteSpace($ReportPath)) {
    $ReportPath = Join-Path $repositoryRoot "build/reports/ci-health/agent-capabilities.json"
}

$checks = [System.Collections.Generic.List[object]]::new()

function Resolve-AndroidSdkPath {
    param([string] $ExplicitPath)

    $candidates = [System.Collections.Generic.List[string]]::new()
    foreach ($candidate in @($ExplicitPath, $env:ANDROID_HOME, $env:ANDROID_SDK_ROOT)) {
        if (-not [string]::IsNullOrWhiteSpace($candidate) -and -not $candidates.Contains($candidate)) {
            $candidates.Add($candidate)
        }
    }

    foreach ($profileRoot in @($env:LOCALAPPDATA, $env:USERPROFILE)) {
        if (-not [string]::IsNullOrWhiteSpace($profileRoot)) {
            $candidate = if ($profileRoot -eq $env:LOCALAPPDATA) {
                Join-Path $profileRoot "Android/Sdk"
            }
            else {
                Join-Path $profileRoot "AppData/Local/Android/Sdk"
            }
            if (-not $candidates.Contains($candidate)) {
                $candidates.Add($candidate)
            }
        }
    }

    # A Windows service normally uses a service profile rather than the profile
    # that owns Android Studio. Discover installed user SDKs without encoding a
    # workstation-specific username in versioned TeamCity settings.
    if ($runningOnWindows -and (Test-Path -LiteralPath "C:/Users" -PathType Container)) {
        foreach ($userDirectory in Get-ChildItem -LiteralPath "C:/Users" -Directory -ErrorAction SilentlyContinue) {
            $candidate = Join-Path $userDirectory.FullName "AppData/Local/Android/Sdk"
            if (-not $candidates.Contains($candidate)) {
                $candidates.Add($candidate)
            }
        }
    }

    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate -PathType Container) {
            return [System.IO.Path]::GetFullPath($candidate)
        }
    }

    return $null
}

function ConvertTo-TeamCityServiceMessageValue {
    param([Parameter(Mandatory)][string] $Value)

    return $Value.Replace("|", "||").Replace("'", "|'").Replace("`n", "|n").Replace("`r", "|r").Replace("[", "|[").Replace("]", "|]")
}

function Add-CapabilityCheck {
    param(
        [Parameter(Mandatory)]
        [string] $Name,

        [Parameter(Mandatory)]
        [bool] $Passed,

        [Parameter(Mandatory)]
        [string] $Detail
    )

    $checks.Add([pscustomobject]@{
        name = $Name
        passed = $Passed
        detail = $Detail
    })
}

$runningOnWindows = [System.Environment]::OSVersion.Platform -eq [System.PlatformID]::Win32NT
Add-CapabilityCheck -Name "windows" -Passed $runningOnWindows -Detail ([System.Environment]::OSVersion.VersionString)

$AndroidSdkPath = Resolve-AndroidSdkPath -ExplicitPath $AndroidSdkPath

$commands = [System.Collections.Generic.List[string]]::new()
foreach ($command in $RequiredCommands) {
    if (-not [string]::IsNullOrWhiteSpace($command) -and -not $commands.Contains($command)) {
        $commands.Add($command)
    }
}
if ($RequireNode) {
    foreach ($command in @("node", "npm")) {
        if (-not $commands.Contains($command)) {
            $commands.Add($command)
        }
    }
}

foreach ($command in $commands) {
    $resolvedCommand = Get-Command $command -ErrorAction SilentlyContinue | Select-Object -First 1
    Add-CapabilityCheck `
        -Name "command:$command" `
        -Passed ($null -ne $resolvedCommand) `
        -Detail $(if ($null -eq $resolvedCommand) { "Command not found on PATH." } else { $resolvedCommand.Source })
}

$sdkExists = -not [string]::IsNullOrWhiteSpace($AndroidSdkPath) -and (Test-Path -LiteralPath $AndroidSdkPath -PathType Container)
Add-CapabilityCheck `
    -Name "android-sdk" `
    -Passed $sdkExists `
    -Detail $(if ($sdkExists) { [System.IO.Path]::GetFullPath($AndroidSdkPath) } else { "ANDROID_HOME does not reference an existing directory." })

if ($sdkExists) {
    foreach ($directoryName in @("build-tools", "platforms")) {
        $directory = Join-Path $AndroidSdkPath $directoryName
        Add-CapabilityCheck `
            -Name "android-sdk:$directoryName" `
            -Passed (Test-Path -LiteralPath $directory -PathType Container) `
            -Detail $directory
    }
}

$reportDirectory = Split-Path -Parent $ReportPath
New-Item -ItemType Directory -Path $reportDirectory -Force | Out-Null
$report = [pscustomobject]@{
    schemaVersion = 1
    generatedAt = [DateTime]::UtcNow.ToString("o")
    machine = [System.Environment]::MachineName
    requireNode = [bool] $RequireNode
    androidSdkPath = $AndroidSdkPath
    checks = @($checks)
}
$report | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $ReportPath -Encoding utf8

$failures = @($checks | Where-Object { -not $_.passed })
if ($failures.Count -gt 0) {
    $failureSummary = $failures | ForEach-Object { "$($_.name): $($_.detail)" }
    throw "Agent capability validation failed:`n$($failureSummary -join "`n")"
}

if ($ExportTeamCityParameters) {
    $escapedSdkPath = ConvertTo-TeamCityServiceMessageValue -Value $AndroidSdkPath
    Write-Host "##teamcity[setParameter name='env.ANDROID_HOME' value='$escapedSdkPath']"
    Write-Host "##teamcity[setParameter name='env.ANDROID_SDK_ROOT' value='$escapedSdkPath']"
}

Write-Host "Agent capability validation passed. Report: $ReportPath"
