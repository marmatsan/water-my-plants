$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$modulePath = Join-Path $PSScriptRoot "..\TeamCityHttpsClient.psm1"
Import-Module $modulePath -Force

$failures = [System.Collections.Generic.List[string]]::new()

function Test-Equal {
    param(
        [Parameter(Mandatory)]
        [object] $Actual,

        [Parameter(Mandatory)]
        [object] $Expected,

        [Parameter(Mandatory)]
        [string] $Name
    )

    if ($Actual -ne $Expected) {
        $failures.Add("$Name expected '$Expected' but received '$Actual'.")
    }
}

function Test-True {
    param(
        [Parameter(Mandatory)]
        [bool] $Condition,

        [Parameter(Mandatory)]
        [string] $Name
    )

    if (-not $Condition) {
        $failures.Add("$Name expected true.")
    }
}

try {
    Assert-TeamCityMainBranch -Branch "feature/not-main"
    $failures.Add("Non-main branch validation did not fail.")
}
catch {
    Test-True -Condition $_.Exception.Message.Contains("only supports branch 'main'") -Name "Branch rejection message"
}

$queueBody = New-TeamCityQueueBody | ConvertFrom-Json
Test-Equal -Actual $queueBody.buildType.id -Expected "WaterMyPlants_WaterMyPlantsFigmaSync" -Name "Queue build type"
Test-Equal -Actual $queueBody.branchName -Expected "main" -Name "Queue branch"

$cloudflareCredential = [PSCredential]::new(
    "client-id",
    (ConvertTo-SecureString "client-secret" -AsPlainText -Force)
)
$headers = New-TeamCityRequestHeaders `
    -CloudflareCredential $cloudflareCredential `
    -TeamCityToken "teamcity-token"
Test-Equal -Actual $headers["CF-Access-Client-Id"] -Expected "client-id" -Name "Cloudflare client id header"
Test-Equal -Actual $headers["CF-Access-Client-Secret"] -Expected "client-secret" -Name "Cloudflare client secret header"
Test-Equal -Actual $headers.Authorization -Expected "Bearer teamcity-token" -Name "TeamCity bearer header"

$active = Select-TeamCityActiveRun `
    -QueuedResponse ([pscustomobject] @{
        build = @(
            [pscustomobject] @{ id = "10"; branchName = "feature/example"; state = "queued" },
            [pscustomobject] @{ id = "11"; branchName = "main"; state = "queued" }
        )
    }) `
    -RunningResponse ([pscustomobject] @{
        build = @(
            [pscustomobject] @{ id = "12"; branchName = "main"; state = "running" }
        )
    })
Test-Equal -Actual $active.id -Expected "12" -Name "Running build takes precedence"

$stale = Select-TeamCityActiveRun `
    -QueuedResponse ([pscustomobject] @{ count = 0; build = @() }) `
    -RunningResponse ([pscustomobject] @{
        build = @(
            [pscustomobject] @{
                id = "13"
                branchName = "main"
                state = "running"
                startDate = "20260716T010000+0200"
            }
        )
    }) `
    -Now ([DateTimeOffset] "2026-07-16T12:00:00+02:00")
Test-True -Condition ($null -eq $stale) -Name "Stale active build is ignored"

$requests = [System.Collections.Generic.List[hashtable]]::new()
$readSession = $null
$transport = {
    param([hashtable] $Request)

    $requests.Add($Request)
    if ($Request.Method -eq "Get") {
        Test-True -Condition (-not $Request.ContainsKey("ContentType")) -Name "GET omits content type"
        Test-True -Condition (-not $Request.ContainsKey("Body")) -Name "GET omits body"
    }
    if ($Request.Method -eq "Get" -and $null -eq $script:readSession) {
        $script:readSession = $Request.WebSession
        $cookie = [Net.Cookie]::new(
            "CF_Authorization",
            "cloudflare-access-jwt",
            "/",
            ([Uri] $Request.Uri).Host
        )
        $Request.WebSession.Cookies.Add($cookie)
    }
    elseif ($Request.Method -eq "Get") {
        Test-True `
            -Condition ([object]::ReferenceEquals($script:readSession, $Request.WebSession)) `
            -Name "Read requests share one WebRequestSession"
    }
    elseif ($Request.Method -eq "Post") {
        Test-True `
            -Condition (-not [object]::ReferenceEquals($script:readSession, $Request.WebSession)) `
            -Name "POST uses a fresh WebRequestSession"
        Test-Equal `
            -Actual @($Request.WebSession.Cookies.GetAllCookies()).Count `
            -Expected 0 `
            -Name "POST session cookie count"
    }

    if ($Request.Uri -like "*/app/rest/buildQueue?*") {
        return [pscustomobject] @{ count = 0; build = @() }
    }
    if ($Request.Uri -like "*/app/rest/builds?*") {
        return [pscustomobject] @{ count = 0; build = @() }
    }
    if ($Request.Method -eq "Post" -and $Request.Uri -like "*/app/rest/buildQueue") {
        return [pscustomobject] @{
            id = "42"
            state = "queued"
            branchName = "main"
            webUrl = "https://teamcity.example/build/42"
        }
    }

    throw "Unexpected request: $($Request.Method) $($Request.Uri)"
}

$secrets = @{
    TeamCityCloudflareAccess = $cloudflareCredential
    TeamCityAutomationToken = ConvertTo-SecureString "teamcity-token" -AsPlainText -Force
}
$secretResolver = {
    param([string] $Name, [string] $Vault)
    Test-Equal -Actual $Vault -Expected "TeamCitySecrets" -Name "Secret vault"
    return $secrets[$Name]
}

$result = Invoke-FigmaSyncRerun `
    -ServerUrl "https://teamcity.example" `
    -SecretResolver $secretResolver `
    -Transport $transport `
    -Confirm:$false

Test-Equal -Actual $result.RunId -Expected "42" -Name "Queued run id"
Test-Equal -Actual $result.Branch -Expected "main" -Name "Queued run branch"
Test-Equal -Actual $result.Reused -Expected $false -Name "New run is not reused"

$postRequest = $requests |
    Where-Object { $_.Method -eq "Post" } |
    Select-Object -First 1
Test-True `
    -Condition (-not $postRequest.Headers.ContainsKey("X-TC-CSRF-Token")) `
    -Name "POST omits CSRF header"
Test-Equal -Actual $postRequest.Headers.Authorization -Expected "Bearer teamcity-token" -Name "POST bearer header"
Test-True `
    -Condition (-not $postRequest.Headers.ContainsKey("CF-Access-Client-Id")) `
    -Name "POST omits Cloudflare client id"
Test-True `
    -Condition (-not $postRequest.Headers.ContainsKey("CF-Access-Client-Secret")) `
    -Name "POST omits Cloudflare client secret"
Test-Equal `
    -Actual $postRequest.Headers["cf-access-token"] `
    -Expected "cloudflare-access-jwt" `
    -Name "POST raw Cloudflare access token"

$postBody = $postRequest.Body | ConvertFrom-Json
Test-Equal -Actual $postBody.branchName -Expected "main" -Name "POST body branch"

$validateRequests = [System.Collections.Generic.List[hashtable]]::new()
$validateTransport = {
    param([hashtable] $Request)

    $validateRequests.Add($Request)
    if ($Request.Uri -like "*/app/rest/buildQueue?*" -or $Request.Uri -like "*/app/rest/builds?*") {
        if (@($Request.WebSession.Cookies.GetCookies([Uri] $Request.Uri)).Count -eq 0) {
            $cookie = [Net.Cookie]::new(
                "CF_Authorization",
                "cloudflare-access-jwt",
                "/",
                ([Uri] $Request.Uri).Host
            )
            $Request.WebSession.Cookies.Add($cookie)
        }
        return [pscustomobject] @{ count = 0; build = @() }
    }
    throw "Validate-only mode must not send '$($Request.Method) $($Request.Uri)'."
}

$validation = Invoke-FigmaSyncRerun `
    -ServerUrl "https://teamcity.example" `
    -SecretResolver $secretResolver `
    -Transport $validateTransport `
    -ValidateOnly `
    -Confirm:$false
Test-Equal -Actual $validation.State -Expected "Validated" -Name "Validate-only result"
Test-Equal `
    -Actual @($validateRequests | Where-Object { $_.Method -eq "Post" }).Count `
    -Expected 0 `
    -Name "Validate-only POST count"

$validateWithActiveRequests = [System.Collections.Generic.List[hashtable]]::new()
$validateWithActiveTransport = {
    param([hashtable] $Request)

    $validateWithActiveRequests.Add($Request)
    if ($Request.Uri -like "*/app/rest/buildQueue?*") {
        $cookie = [Net.Cookie]::new(
            "CF_Authorization",
            "cloudflare-access-jwt",
            "/",
            ([Uri] $Request.Uri).Host
        )
        $Request.WebSession.Cookies.Add($cookie)
        return [pscustomobject] @{
            count = 1
            build = @(
                [pscustomobject] @{
                    id = "83"
                    state = "queued"
                    branchName = "main"
                    webUrl = "https://teamcity.example/build/83"
                }
            )
        }
    }
    if ($Request.Uri -like "*/app/rest/builds?*") {
        return [pscustomobject] @{ count = 0; build = @() }
    }
    throw "Validate-only mode must not send '$($Request.Method) $($Request.Uri)'."
}

$validationWithActive = Invoke-FigmaSyncRerun `
    -ServerUrl "https://teamcity.example" `
    -SecretResolver $secretResolver `
    -Transport $validateWithActiveTransport `
    -ValidateOnly `
    -Confirm:$false
Test-Equal -Actual $validationWithActive.State -Expected "Validated" -Name "Validate-only with active run"
Test-Equal `
    -Actual @($validateWithActiveRequests | Where-Object { $_.Method -eq "Post" }).Count `
    -Expected 0 `
    -Name "Validate-only active run POST count"

$reuseRequests = [System.Collections.Generic.List[hashtable]]::new()
$reuseTransport = {
    param([hashtable] $Request)

    $reuseRequests.Add($Request)
    if ($Request.Uri -like "*/app/rest/buildQueue?*") {
        $cookie = [Net.Cookie]::new(
            "CF_Authorization",
            "cloudflare-access-jwt",
            "/",
            ([Uri] $Request.Uri).Host
        )
        $Request.WebSession.Cookies.Add($cookie)
        return [pscustomobject] @{
            count = 1
            build = @(
                [pscustomobject] @{
                    id = "84"
                    state = "queued"
                    branchName = "main"
                    webUrl = "https://teamcity.example/build/84"
                }
            )
        }
    }
    if ($Request.Uri -like "*/app/rest/builds?*") {
        return [pscustomobject] @{ count = 0; build = @() }
    }

    throw "Active-run reuse must not send '$($Request.Method) $($Request.Uri)'."
}

$reused = Invoke-FigmaSyncRerun `
    -ServerUrl "https://teamcity.example" `
    -SecretResolver $secretResolver `
    -Transport $reuseTransport `
    -Confirm:$false
Test-Equal -Actual $reused.RunId -Expected "84" -Name "Reused run id"
Test-Equal -Actual $reused.Reused -Expected $true -Name "Reused run marker"
Test-Equal `
    -Actual @($reuseRequests | Where-Object { $_.Method -eq "Post" }).Count `
    -Expected 0 `
    -Name "Reused run POST count"

$waitRequests = [System.Collections.Generic.List[hashtable]]::new()
$waitTransport = {
    param([hashtable] $Request)

    $waitRequests.Add($Request)
    if ($Request.Uri -like "*/app/rest/buildQueue?*") {
        $cookie = [Net.Cookie]::new(
            "CF_Authorization",
            "cloudflare-access-jwt",
            "/",
            ([Uri] $Request.Uri).Host
        )
        $Request.WebSession.Cookies.Add($cookie)
        return [pscustomobject] @{
            count = 1
            build = @(
                [pscustomobject] @{
                    id = "85"
                    state = "queued"
                    branchName = "main"
                    webUrl = "https://teamcity.example/build/85"
                }
            )
        }
    }
    if ($Request.Uri -like "*/app/rest/builds/id:85?fields=*") {
        return [pscustomobject] @{
            id = "85"
            state = "finished"
            status = "SUCCESS"
            statusText = "Success"
            branchName = "main"
            webUrl = "https://teamcity.example/build/85"
        }
    }
    if ($Request.Uri -like "*/app/rest/builds?*") {
        return [pscustomobject] @{ count = 0; build = @() }
    }

    throw "Unexpected wait request: $($Request.Method) $($Request.Uri)"
}

$waited = Invoke-FigmaSyncRerun `
    -ServerUrl "https://teamcity.example" `
    -SecretResolver $secretResolver `
    -Transport $waitTransport `
    -Wait `
    -PollIntervalSeconds 1 `
    -Confirm:$false
Test-Equal -Actual $waited.RunId -Expected "85" -Name "Waited run id"
Test-Equal -Actual $waited.State -Expected "finished" -Name "Waited run state"
Test-Equal -Actual $waited.Reused -Expected $true -Name "Waited run reuse marker"

$uncertainRequests = [System.Collections.Generic.List[hashtable]]::new()
$uncertainQueueReads = 0
$uncertainPostCount = 0
$uncertainTransport = {
    param([hashtable] $Request)

    $uncertainRequests.Add($Request)
    if ($Request.Uri -like "*/app/rest/buildQueue?*") {
        $script:uncertainQueueReads++
        if ($script:uncertainQueueReads -eq 1) {
            $cookie = [Net.Cookie]::new(
                "CF_Authorization",
                "cloudflare-access-jwt",
                "/",
                ([Uri] $Request.Uri).Host
            )
            $Request.WebSession.Cookies.Add($cookie)
            return [pscustomobject] @{ count = 0; build = @() }
        }

        return [pscustomobject] @{
            count = 1
            build = @(
                [pscustomobject] @{
                    id = "86"
                    state = "queued"
                    branchName = "main"
                    webUrl = "https://teamcity.example/build/86"
                }
            )
        }
    }
    if ($Request.Uri -like "*/app/rest/builds?*") {
        return [pscustomobject] @{ count = 0; build = @() }
    }
    if ($Request.Method -eq "Post" -and $Request.Uri -like "*/app/rest/buildQueue") {
        $script:uncertainPostCount++
        throw "Simulated connection loss after TeamCity accepted the POST."
    }

    throw "Unexpected uncertain request: $($Request.Method) $($Request.Uri)"
}

$uncertain = Invoke-FigmaSyncRerun `
    -ServerUrl "https://teamcity.example" `
    -SecretResolver $secretResolver `
    -Transport $uncertainTransport `
    -Confirm:$false
Test-Equal -Actual $uncertain.RunId -Expected "86" -Name "Uncertain POST recovered run id"
Test-Equal -Actual $uncertain.Reused -Expected $true -Name "Uncertain POST reuse marker"
Test-Equal -Actual $uncertainPostCount -Expected 1 -Name "Uncertain POST attempt count"

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host "TeamCity HTTPS client tests passed."
