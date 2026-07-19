[CmdletBinding(SupportsShouldProcess)]
param(
    [string] $RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path,
    [string] $TaskName = "Water My Plants - Infrastructure Health at startup",
    [switch] $RunNow
)

$ErrorActionPreference = "Stop"
$startupScript = Join-Path $RepositoryRoot ".teamcity/scripts/invoke-infrastructure-health-at-startup.ps1"
if (-not (Test-Path -LiteralPath $startupScript -PathType Leaf)) {
    throw "Infrastructure Health startup adapter was not found at '$startupScript'."
}

$pwsh = (Get-Command pwsh.exe -ErrorAction Stop).Source
$userId = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
$arguments = '-NoProfile -NonInteractive -ExecutionPolicy Bypass -File "{0}" -RepositoryRoot "{1}"' -f `
    $startupScript,
    $RepositoryRoot
$action = New-ScheduledTaskAction -Execute $pwsh -Argument $arguments
$trigger = New-ScheduledTaskTrigger -AtStartup
$principal = New-ScheduledTaskPrincipal `
    -UserId $userId `
    -LogonType Interactive `
    -RunLevel Limited
$settings = New-ScheduledTaskSettingsSet `
    -StartWhenAvailable `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -MultipleInstances IgnoreNew `
    -ExecutionTimeLimit (New-TimeSpan -Minutes 15)

if ($PSCmdlet.ShouldProcess($TaskName, "Register startup Infrastructure Health trigger")) {
    Register-ScheduledTask `
        -TaskName $TaskName `
        -Description "Queues the non-gating Water My Plants Infrastructure Health pipeline after TeamCity is ready." `
        -Action $action `
        -Trigger $trigger `
        -Principal $principal `
        -Settings $settings `
        -Force | Out-Null

    if ($RunNow) {
        Start-ScheduledTask -TaskName $TaskName
    }
}

$task = Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue
if ($null -eq $task) {
    [PSCustomObject]@{
        TaskName = $TaskName
        State = "NotRegistered"
        Author = $userId
        Description = "Startup Infrastructure Health trigger is not registered."
    }
}
else {
    $task | Select-Object TaskName, State, Author, Description
}
