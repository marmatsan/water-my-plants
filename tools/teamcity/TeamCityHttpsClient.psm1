Set-StrictMode -Version Latest

$script:FigmaSyncBuildTypeId = "WaterMyPlants_WaterMyPlantsFigmaSync"
$script:FigmaSyncBranch = "main"

function Assert-TeamCityMainBranch {
    param(
        [Parameter(Mandatory)]
        [string] $Branch
    )

    if ($Branch -ne $script:FigmaSyncBranch) {
        throw "Figma Sync automation only supports branch '$($script:FigmaSyncBranch)'. Received '$Branch'."
    }
}

function ConvertFrom-TeamCitySecret {
    param(
        [Parameter(Mandatory)]
        [object] $Secret,

        [Parameter(Mandatory)]
        [string] $SecretName
    )

    $secureValue = switch ($Secret) {
        { $_ -is [System.Management.Automation.PSCredential] } {
            $_.Password
            break
        }
        { $_ -is [System.Security.SecureString] } {
            $_
            break
        }
        { $_ -is [string] } {
            return $_
        }
        default {
            throw "Secret '$SecretName' must be a SecureString or PSCredential."
        }
    }

    $bstr = [IntPtr]::Zero
    try {
        $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureValue)
        return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    }
    finally {
        if ($bstr -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
        }
    }
}

function Get-TeamCitySecret {
    param(
        [Parameter(Mandatory)]
        [string] $Name,

        [Parameter(Mandatory)]
        [string] $Vault
    )

    if (-not (Get-Command Get-Secret -ErrorAction SilentlyContinue)) {
        throw "PowerShell SecretManagement is required. Install and configure it before running TeamCity automation."
    }

    $secret = Get-Secret -Name $Name -Vault $Vault -ErrorAction Stop
    if ($null -eq $secret) {
        throw "Secret '$Name' was not found in vault '$Vault'."
    }

    return $secret
}

function New-TeamCityRequestHeaders {
    param(
        [Parameter(Mandatory)]
        [System.Management.Automation.PSCredential] $CloudflareCredential,

        [Parameter(Mandatory)]
        [string] $TeamCityToken
    )

    return @{
        Accept                            = "application/json"
        Authorization                     = "Bearer $TeamCityToken"
        "CF-Access-Client-Id"             = $CloudflareCredential.UserName
        "CF-Access-Client-Secret"         = $CloudflareCredential.GetNetworkCredential().Password
    }
}

function New-TeamCityMutationHeaders {
    param(
        [Parameter(Mandatory)]
        [hashtable] $ReadHeaders,

        [Parameter(Mandatory)]
        [Microsoft.PowerShell.Commands.WebRequestSession] $ReadSession,

        [Parameter(Mandatory)]
        [string] $ServerUrl
    )

    $serverUri = [Uri] $ServerUrl
    $accessCookie = $ReadSession.Cookies.GetCookies($serverUri) |
        Where-Object { $_.Name -eq "CF_Authorization" } |
        Select-Object -First 1
    if ($null -eq $accessCookie -or [string]::IsNullOrWhiteSpace($accessCookie.Value)) {
        throw "Cloudflare Access did not return a CF_Authorization token for the TeamCity HTTPS session."
    }

    $headers = @{} + $ReadHeaders
    $headers.Remove("CF-Access-Client-Id")
    $headers.Remove("CF-Access-Client-Secret")
    $headers["cf-access-token"] = $accessCookie.Value
    return $headers
}

function New-TeamCityQueueBody {
    param(
        [string] $BuildTypeId = $script:FigmaSyncBuildTypeId,
        [string] $Branch = $script:FigmaSyncBranch
    )

    Assert-TeamCityMainBranch -Branch $Branch

    return @{
        buildType = @{
            id = $BuildTypeId
        }
        branchName = $Branch
    } | ConvertTo-Json -Depth 4 -Compress
}

function New-TeamCityUri {
    param(
        [Parameter(Mandatory)]
        [string] $ServerUrl,

        [Parameter(Mandatory)]
        [string] $Path
    )

    return "$($ServerUrl.TrimEnd('/'))/$($Path.TrimStart('/'))"
}

function ConvertTo-TeamCityBuildArray {
    param(
        [AllowNull()]
        [object] $Response
    )

    if ($null -eq $Response) {
        return @()
    }

    if ($Response.PSObject.Properties.Name -contains "build") {
        return @($Response.build) | Where-Object { $null -ne $_ }
    }

    if ($Response.PSObject.Properties.Name -contains "id") {
        return @($Response)
    }

    return @()
}

function Test-TeamCityMainBranchName {
    param(
        [AllowNull()]
        [string] $BranchName
    )

    return $BranchName -in @("main", "<default>", "refs/heads/main")
}

function Test-TeamCityBuildRecent {
    param(
        [Parameter(Mandatory)]
        [object] $Build,

        [Parameter(Mandatory)]
        [DateTimeOffset] $Now,

        [ValidateRange(1, 168)]
        [int] $MaxAgeHours = 6
    )

    $serializedDate = if (
        $Build.PSObject.Properties.Name -contains "startDate" -and
        -not [string]::IsNullOrWhiteSpace([string] $Build.startDate)
    ) {
        [string] $Build.startDate
    }
    elseif (
        $Build.PSObject.Properties.Name -contains "queuedDate" -and
        -not [string]::IsNullOrWhiteSpace([string] $Build.queuedDate)
    ) {
        [string] $Build.queuedDate
    }
    else {
        return $true
    }

    $normalizedDate = $serializedDate -replace "([+-]\d{2})(\d{2})$", '$1:$2'
    $parsedDate = [DateTimeOffset]::MinValue
    $formats = @(
        "yyyyMMdd'T'HHmmsszzz",
        "yyyy-MM-dd'T'HH:mm:ssK",
        "o"
    )
    $parsed = $false
    foreach ($format in $formats) {
        try {
            $parsedDate = [DateTimeOffset]::ParseExact(
                $normalizedDate,
                $format,
                [Globalization.CultureInfo]::InvariantCulture
            )
            $parsed = $true
            break
        }
        catch [FormatException] {
            continue
        }
    }

    if (-not $parsed) {
        return $true
    }

    return ($Now - $parsedDate.ToUniversalTime()).TotalHours -le $MaxAgeHours
}

function Select-TeamCityActiveRun {
    param(
        [AllowNull()]
        [object] $QueuedResponse,

        [AllowNull()]
        [object] $RunningResponse,

        [DateTimeOffset] $Now = [DateTimeOffset]::UtcNow,

        [ValidateRange(1, 168)]
        [int] $MaxAgeHours = 6
    )

    $running = ConvertTo-TeamCityBuildArray -Response $RunningResponse |
        Where-Object { Test-TeamCityMainBranchName -BranchName $_.branchName } |
        Where-Object { Test-TeamCityBuildRecent -Build $_ -Now $Now -MaxAgeHours $MaxAgeHours } |
        Select-Object -First 1

    if ($null -ne $running) {
        return $running
    }

    return ConvertTo-TeamCityBuildArray -Response $QueuedResponse |
        Where-Object { Test-TeamCityMainBranchName -BranchName $_.branchName } |
        Where-Object { Test-TeamCityBuildRecent -Build $_ -Now $Now -MaxAgeHours $MaxAgeHours } |
        Select-Object -First 1
}

function Invoke-TeamCityDefaultTransport {
    param(
        [Parameter(Mandatory)]
        [hashtable] $Request
    )

    $parameters = @{
        Method      = $Request.Method
        Uri         = $Request.Uri
        Headers     = $Request.Headers
        WebSession  = $Request.WebSession
        ErrorAction = "Stop"
    }

    if ($Request.ContainsKey("ContentType")) {
        $parameters.ContentType = $Request.ContentType
    }
    if ($Request.ContainsKey("Body")) {
        $parameters.Body = $Request.Body
    }

    try {
        return Invoke-RestMethod @parameters
    }
    catch {
        $caughtError = $_
        $statusCode = $null
        $responseDetail = [string] $caughtError.ErrorDetails.Message
        $responseProperty = $caughtError.Exception.PSObject.Properties["Response"]
        if ($null -ne $responseProperty -and $null -ne $responseProperty.Value) {
            $statusCode = [int] $responseProperty.Value.StatusCode
            $contentProperty = $responseProperty.Value.PSObject.Properties["Content"]
            if (
                [string]::IsNullOrWhiteSpace($responseDetail) -and
                $null -ne $contentProperty -and
                $null -ne $contentProperty.Value
            ) {
                try {
                    $responseDetail = $contentProperty.Value.ReadAsStringAsync().GetAwaiter().GetResult()
                    $responseDetail = ($responseDetail -replace "\s+", " ").Trim()
                    if ($responseDetail.Length -gt 500) {
                        $responseDetail = $responseDetail.Substring(0, 500)
                    }
                }
                catch {
                    $responseDetail = $null
                }
            }
        }

        if (-not [string]::IsNullOrWhiteSpace($responseDetail)) {
            $responseDetail = ($responseDetail -replace "\s+", " ").Trim()
            if ($responseDetail.Length -gt 500) {
                $responseDetail = $responseDetail.Substring(0, 500)
            }
        }

        $guidance = switch ($statusCode) {
            401 { "Verify the TeamCity access token and its expiry." }
            403 { "Verify Cloudflare Service Auth, the raw Access JWT, TeamCity permissions, and cookie-free Bearer authentication." }
            default { "Inspect TeamCity and Cloudflare logs for the request." }
        }

        $status = if ($null -eq $statusCode) { "unknown status" } else { "HTTP $statusCode" }
        $detail = if ([string]::IsNullOrWhiteSpace($responseDetail)) {
            ""
        }
        else {
            " Response: $responseDetail"
        }
        throw "TeamCity request '$($Request.Method) $($Request.Uri)' failed with $status. $guidance$detail Cause: $($caughtError.Exception.Message)"
    }
}

function Invoke-TeamCityRequest {
    param(
        [Parameter(Mandatory)]
        [string] $Method,

        [Parameter(Mandatory)]
        [string] $ServerUrl,

        [Parameter(Mandatory)]
        [string] $Path,

        [Parameter(Mandatory)]
        [hashtable] $Headers,

        [Parameter(Mandatory)]
        [Microsoft.PowerShell.Commands.WebRequestSession] $WebSession,

        [AllowNull()]
        [string] $Body,

        [AllowNull()]
        [string] $ContentType,

        [Parameter(Mandatory)]
        [scriptblock] $Transport
    )

    $request = @{
        Method     = $Method
        Uri        = New-TeamCityUri -ServerUrl $ServerUrl -Path $Path
        Headers    = $Headers
        WebSession = $WebSession
    }

    if (-not [string]::IsNullOrWhiteSpace($Body)) {
        $request.Body = $Body
    }
    if (-not [string]::IsNullOrWhiteSpace($ContentType)) {
        $request.ContentType = $ContentType
    }

    return & $Transport $request
}

function Get-TeamCityActiveFigmaSyncRun {
    param(
        [Parameter(Mandatory)]
        [string] $ServerUrl,

        [Parameter(Mandatory)]
        [hashtable] $Headers,

        [Parameter(Mandatory)]
        [Microsoft.PowerShell.Commands.WebRequestSession] $WebSession,

        [Parameter(Mandatory)]
        [scriptblock] $Transport
    )

    $queueLocator = [Uri]::EscapeDataString("buildType:(id:$($script:FigmaSyncBuildTypeId))")
    $runningLocator = [Uri]::EscapeDataString(
        "buildType:(id:$($script:FigmaSyncBuildTypeId)),branch:$($script:FigmaSyncBranch),running:true"
    )
    $fields = [Uri]::EscapeDataString(
        "build(id,state,status,branchName,queuedDate,startDate,webUrl)"
    )

    $queued = Invoke-TeamCityRequest `
        -Method Get `
        -ServerUrl $ServerUrl `
        -Path "/app/rest/buildQueue?locator=$queueLocator&fields=$fields" `
        -Headers $Headers `
        -WebSession $WebSession `
        -Transport $Transport

    $running = Invoke-TeamCityRequest `
        -Method Get `
        -ServerUrl $ServerUrl `
        -Path "/app/rest/builds?locator=$runningLocator&fields=$fields" `
        -Headers $Headers `
        -WebSession $WebSession `
        -Transport $Transport

    return Select-TeamCityActiveRun -QueuedResponse $queued -RunningResponse $running
}

function Wait-TeamCityBuild {
    param(
        [Parameter(Mandatory)]
        [string] $BuildId,

        [Parameter(Mandatory)]
        [string] $ServerUrl,

        [Parameter(Mandatory)]
        [hashtable] $Headers,

        [Parameter(Mandatory)]
        [Microsoft.PowerShell.Commands.WebRequestSession] $WebSession,

        [Parameter(Mandatory)]
        [scriptblock] $Transport,

        [ValidateRange(1, 300)]
        [int] $PollIntervalSeconds = 10,

        [ValidateRange(1, 1440)]
        [int] $TimeoutMinutes = 60
    )

    $deadline = [DateTimeOffset]::UtcNow.AddMinutes($TimeoutMinutes)
    $fields = [Uri]::EscapeDataString("id,state,status,statusText,branchName,webUrl")

    do {
        $build = Invoke-TeamCityRequest `
            -Method Get `
            -ServerUrl $ServerUrl `
            -Path "/app/rest/builds/id:${BuildId}?fields=$fields" `
            -Headers $Headers `
            -WebSession $WebSession `
            -Transport $Transport

        if ($build.state -eq "finished") {
            if ($build.status -ne "SUCCESS") {
                throw "TeamCity Figma Sync run $BuildId finished with status '$($build.status)': $($build.statusText)"
            }

            return $build
        }

        Start-Sleep -Seconds $PollIntervalSeconds
    }
    while ([DateTimeOffset]::UtcNow -lt $deadline)

    throw "Timed out waiting $TimeoutMinutes minutes for TeamCity Figma Sync run $BuildId."
}

function New-TeamCityRunResult {
    param(
        [Parameter(Mandatory)]
        [object] $Build,

        [Parameter(Mandatory)]
        [bool] $Reused,

        [Parameter(Mandatory)]
        [string] $State
    )

    return [pscustomobject] @{
        RunId   = [string] $Build.id
        WebUrl  = [string] $Build.webUrl
        Branch  = [string] $Build.branchName
        State   = $State
        Reused  = $Reused
    }
}

function Invoke-FigmaSyncRerun {
    [CmdletBinding(SupportsShouldProcess, ConfirmImpact = "Medium")]
    param(
        [string] $ServerUrl = "https://teamcity.marmatsan.dev",
        [string] $Branch = $script:FigmaSyncBranch,
        [string] $Vault = "TeamCitySecrets",
        [string] $CloudflareSecretName = "TeamCityCloudflareAccess",
        [string] $TeamCityTokenSecretName = "TeamCityAutomationToken",
        [switch] $ValidateOnly,
        [switch] $Wait,
        [ValidateRange(1, 300)]
        [int] $PollIntervalSeconds = 10,
        [ValidateRange(1, 1440)]
        [int] $TimeoutMinutes = 60,
        [scriptblock] $SecretResolver = ${function:Get-TeamCitySecret},
        [scriptblock] $Transport = ${function:Invoke-TeamCityDefaultTransport}
    )

    Assert-TeamCityMainBranch -Branch $Branch

    if (-not $ServerUrl.StartsWith("https://", [StringComparison]::OrdinalIgnoreCase)) {
        throw "The public TeamCity automation endpoint must use HTTPS."
    }

    if ($WhatIfPreference) {
        return [pscustomobject] @{
            RunId  = $null
            WebUrl = $null
            Branch = $Branch
            State  = "Planned"
            Reused = $false
        }
    }

    $cloudflareSecret = & $SecretResolver $CloudflareSecretName $Vault
    if ($cloudflareSecret -isnot [System.Management.Automation.PSCredential]) {
        throw "Secret '$CloudflareSecretName' must be stored as a PSCredential."
    }

    $teamCitySecret = & $SecretResolver $TeamCityTokenSecretName $Vault
    $teamCityToken = ConvertFrom-TeamCitySecret `
        -Secret $teamCitySecret `
        -SecretName $TeamCityTokenSecretName

    try {
        $headers = New-TeamCityRequestHeaders `
            -CloudflareCredential $cloudflareSecret `
            -TeamCityToken $teamCityToken
        $readSession = [Microsoft.PowerShell.Commands.WebRequestSession]::new()

        $activeRun = Get-TeamCityActiveFigmaSyncRun `
            -ServerUrl $ServerUrl `
            -Headers $headers `
            -WebSession $readSession `
            -Transport $Transport

        if ($null -ne $activeRun -and -not $ValidateOnly) {
            $result = New-TeamCityRunResult -Build $activeRun -Reused $true -State $activeRun.state
            if ($Wait) {
                $finished = Wait-TeamCityBuild `
                    -BuildId $result.RunId `
                    -ServerUrl $ServerUrl `
                    -Headers $headers `
                    -WebSession $readSession `
                    -Transport $Transport `
                    -PollIntervalSeconds $PollIntervalSeconds `
                    -TimeoutMinutes $TimeoutMinutes
                return New-TeamCityRunResult -Build $finished -Reused $true -State $finished.state
            }

            return $result
        }

        $mutationHeaders = New-TeamCityMutationHeaders `
            -ReadHeaders $headers `
            -ReadSession $readSession `
            -ServerUrl $ServerUrl

        if ($ValidateOnly) {
            return [pscustomobject] @{
                RunId  = $null
                WebUrl = $null
                Branch = $Branch
                State  = "Validated"
                Reused = $false
            }
        }

        if (-not $PSCmdlet.ShouldProcess(
            "$($script:FigmaSyncBuildTypeId) on $Branch",
            "Queue TeamCity Figma Sync"
        )) {
            return [pscustomobject] @{
                RunId  = $null
                WebUrl = $null
                Branch = $Branch
                State  = "Skipped"
                Reused = $false
            }
        }

        $body = New-TeamCityQueueBody -Branch $Branch
        # Forward the Access JWT as a header so TeamCity never receives Cloudflare's cookie.
        $mutationSession = [Microsoft.PowerShell.Commands.WebRequestSession]::new()

        $reusedAfterPost = $false
        try {
            $queuedBuild = Invoke-TeamCityRequest `
                -Method Post `
                -ServerUrl $ServerUrl `
                -Path "/app/rest/buildQueue" `
                -Headers $mutationHeaders `
                -WebSession $mutationSession `
                -Body $body `
                -ContentType "application/json" `
                -Transport $Transport
        }
        catch {
            $activeAfterFailure = Get-TeamCityActiveFigmaSyncRun `
                -ServerUrl $ServerUrl `
                -Headers $headers `
                -WebSession $readSession `
                -Transport $Transport
            if ($null -ne $activeAfterFailure) {
                $queuedBuild = $activeAfterFailure
                $reusedAfterPost = $true
            }
            else {
                throw
            }
        }

        $result = New-TeamCityRunResult `
            -Build $queuedBuild `
            -Reused $reusedAfterPost `
            -State $queuedBuild.state
        if ($Wait) {
            $finished = Wait-TeamCityBuild `
                -BuildId $result.RunId `
                -ServerUrl $ServerUrl `
                -Headers $headers `
                -WebSession $readSession `
                -Transport $Transport `
                -PollIntervalSeconds $PollIntervalSeconds `
                -TimeoutMinutes $TimeoutMinutes
            return New-TeamCityRunResult `
                -Build $finished `
                -Reused $reusedAfterPost `
                -State $finished.state
        }

        return $result
    }
    finally {
        Remove-Variable teamCityToken -ErrorAction SilentlyContinue
        Remove-Variable teamCitySecret -ErrorAction SilentlyContinue
        Remove-Variable cloudflareSecret -ErrorAction SilentlyContinue
    }
}

Export-ModuleMember -Function `
    Assert-TeamCityMainBranch, `
    New-TeamCityRequestHeaders, `
    New-TeamCityMutationHeaders, `
    New-TeamCityQueueBody, `
    Select-TeamCityActiveRun, `
    Invoke-FigmaSyncRerun
