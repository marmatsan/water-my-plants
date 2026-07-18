Set-StrictMode -Version Latest

function Get-RequiredJsonProperty {
    param(
        [Parameter(Mandatory)][object] $Object,
        [Parameter(Mandatory)][string] $Name,
        [Parameter(Mandatory)][string] $Context
    )

    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property -or $null -eq $property.Value) {
        throw "$Context is missing required property '$Name'."
    }
    return $property.Value
}

function Get-SingleArtifactFile {
    param(
        [Parameter(Mandatory)][string] $Root,
        [Parameter(Mandatory)][string] $Filter,
        [Parameter(Mandatory)][string] $Description
    )

    $files = @(Get-ChildItem -LiteralPath $Root -Recurse -File -Filter $Filter)
    if ($files.Count -ne 1) {
        throw "Expected exactly one $Description under '$Root'; found $($files.Count)."
    }
    return $files[0]
}

function Read-ArtifactJson {
    param([Parameter(Mandatory)][System.IO.FileInfo] $File)

    try {
        return Get-Content -LiteralPath $File.FullName -Raw | ConvertFrom-Json
    }
    catch {
        throw "Artifact '$($File.FullName)' is not valid JSON: $($_.Exception.Message)"
    }
}

function Assert-ArtifactValue {
    param(
        [AllowNull()][object] $Actual,
        [AllowNull()][object] $Expected,
        [Parameter(Mandatory)][string] $Description
    )

    if ([string] $Actual -cne [string] $Expected) {
        throw "$Description mismatch: expected '$Expected', found '$Actual'."
    }
}

function Test-OfficialFigmaArtifactSet {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)][string] $ArtifactDirectory,
        [string] $ExpectedGitSha
    )

    $root = (Resolve-Path -LiteralPath $ArtifactDirectory).Path
    $modelFile = Get-SingleArtifactFile -Root $root -Filter "design-model.json" -Description "design model"
    $scopeFile = Get-SingleArtifactFile -Root $root -Filter "sync-scope.json" -Description "sync scope"
    $planFile = Get-SingleArtifactFile -Root $root -Filter "visual-sync-plan.json" -Description "visual sync plan"

    $model = Read-ArtifactJson -File $modelFile
    $scope = Read-ArtifactJson -File $scopeFile
    $plan = Read-ArtifactJson -File $planFile

    $branch = Get-RequiredJsonProperty -Object $model -Name "branch" -Context "design-model.json"
    if ($branch -ne "main") {
        throw "Official Figma artifacts require model branch 'main'; found '$branch'."
    }
    $gitSha = Get-RequiredJsonProperty -Object $model -Name "gitSha" -Context "design-model.json"
    $modelHash = Get-RequiredJsonProperty -Object $model -Name "modelHash" -Context "design-model.json"
    if ([string]::IsNullOrWhiteSpace([string] $gitSha) -or [string]::IsNullOrWhiteSpace([string] $modelHash)) {
        throw "design-model.json requires non-empty gitSha and modelHash values."
    }
    if (-not [string]::IsNullOrWhiteSpace($ExpectedGitSha)) {
        Assert-ArtifactValue -Actual $gitSha -Expected $ExpectedGitSha -Description "TeamCity revision"
    }

    Assert-ArtifactValue `
        -Actual (Get-RequiredJsonProperty -Object $scope -Name "scope" -Context "sync-scope.json") `
        -Expected "full-verification" `
        -Description "Figma change scope"
    Assert-ArtifactValue -Actual $scope.gitSha -Expected $gitSha -Description "Scope gitSha"
    Assert-ArtifactValue -Actual $scope.modelHash -Expected $modelHash -Description "Scope modelHash"

    $manifestFiles = @(Get-ChildItem -LiteralPath $root -Recurse -File -Filter "manifest.json")
    $manifestEntries = @($manifestFiles | ForEach-Object {
        [pscustomobject]@{ File = $_; Data = (Read-ArtifactJson -File $_) }
    })
    $visualEntries = @($manifestEntries | Where-Object { $_.Data.fullVisualSync -eq $true })
    $metadataEntries = @($manifestEntries | Where-Object { $_.Data.writeMetadata -eq $true })
    if ($visualEntries.Count -ne 1 -or $metadataEntries.Count -ne 1) {
        throw "Expected one full visual and one metadata manifest; found $($visualEntries.Count) visual and $($metadataEntries.Count) metadata."
    }

    $visual = $visualEntries[0].Data
    $metadata = $metadataEntries[0].Data
    foreach ($entry in @(
        @{ Name = "visual"; Data = $visual },
        @{ Name = "metadata"; Data = $metadata }
    )) {
        Assert-ArtifactValue -Actual $entry.Data.mode -Expected "official" -Description "$($entry.Name) manifest mode"
        Assert-ArtifactValue -Actual $entry.Data.gitSha -Expected $gitSha -Description "$($entry.Name) manifest gitSha"
        Assert-ArtifactValue -Actual $entry.Data.modelHash -Expected $modelHash -Description "$($entry.Name) manifest modelHash"
        [void] (Get-RequiredJsonProperty -Object $entry.Data -Name "manifestHash" -Context "$($entry.Name) manifest")
    }
    Assert-ArtifactValue -Actual $visual.writeMetadata -Expected $false -Description "Visual manifest metadata flag"
    Assert-ArtifactValue -Actual $metadata.writeMetadata -Expected $true -Description "Metadata manifest metadata flag"
    foreach ($identityName in @("writerHash", "transportHash")) {
        $visualIdentity = Get-RequiredJsonProperty -Object $visual -Name $identityName -Context "visual manifest"
        Assert-ArtifactValue -Actual $metadata.$identityName -Expected $visualIdentity -Description "Metadata manifest $identityName"
        Assert-ArtifactValue -Actual $scope.$identityName -Expected $visualIdentity -Description "Scope $identityName"
        Assert-ArtifactValue -Actual $plan.identity.$identityName -Expected $visualIdentity -Description "Visual plan $identityName"
    }
    Assert-ArtifactValue -Actual $plan.identity.modelHash -Expected $modelHash -Description "Visual plan modelHash"
    Assert-ArtifactValue -Actual $plan.manifestHash -Expected $visual.manifestHash -Description "Visual plan manifestHash"
    Assert-ArtifactValue -Actual $scope.visualRunnerManifestHash -Expected $visual.manifestHash -Description "Scope visual manifestHash"
    Assert-ArtifactValue -Actual $scope.metadataRunnerManifestHash -Expected $metadata.manifestHash -Description "Scope metadata manifestHash"
    Assert-ArtifactValue -Actual $scope.visualSyncDecision -Expected $plan.decision -Description "Scope visual decision"
    if ($plan.decision -notin @("none", "partial", "full")) {
        throw "Unsupported visual sync decision '$($plan.decision)'."
    }

    return [pscustomobject]@{
        ArtifactDirectory = $root
        GitSha = [string] $gitSha
        ModelHash = [string] $modelHash
        Decision = [string] $plan.decision
        ModelPath = $modelFile.FullName
        ScopePath = $scopeFile.FullName
        PlanPath = $planFile.FullName
        VisualManifestPath = $visualEntries[0].File.FullName
        MetadataManifestPath = $metadataEntries[0].File.FullName
        VisualStatePath = Join-Path $visualEntries[0].File.DirectoryName "execution-state.json"
    }
}

Export-ModuleMember -Function Test-OfficialFigmaArtifactSet
