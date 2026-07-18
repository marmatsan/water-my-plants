$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
Import-Module (Join-Path $PSScriptRoot "../FigmaArtifactHandoff.psm1") -Force

$fixtureRoot = Join-Path ([System.IO.Path]::GetTempPath()) "water-my-plants-figma-handoff-tests-$([Guid]::NewGuid())"
$visualDirectory = Join-Path $fixtureRoot "mcp-runners/visual"
$metadataDirectory = Join-Path $fixtureRoot "mcp-runners/metadata"
New-Item -ItemType Directory -Path $visualDirectory -Force | Out-Null
New-Item -ItemType Directory -Path $metadataDirectory -Force | Out-Null

function Write-TestJson {
    param([Parameter(Mandatory)][string] $Path, [Parameter(Mandatory)][object] $Value)
    $Value | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $Path -Encoding utf8
}

try {
    Write-TestJson (Join-Path $fixtureRoot "design-model.json") ([pscustomobject]@{
        branch = "main"; gitSha = "abc123"; modelHash = "model-hash"; content = [pscustomobject]@{}
    })
    Write-TestJson (Join-Path $fixtureRoot "sync-scope.json") ([pscustomobject]@{
        scope = "full-verification"; gitSha = "abc123"; modelHash = "model-hash"
        writerHash = "writer-hash"; transportHash = "transport-hash"
        visualRunnerManifestHash = "visual-hash"; metadataRunnerManifestHash = "metadata-hash"
        visualSyncDecision = "partial"
    })
    Write-TestJson (Join-Path $fixtureRoot "visual-sync-plan.json") ([pscustomobject]@{
        decision = "partial"; manifestHash = "visual-hash"; executionScopes = @("preflight")
        identity = [pscustomobject]@{ modelHash = "model-hash"; writerHash = "writer-hash"; transportHash = "transport-hash" }
    })
    Write-TestJson (Join-Path $visualDirectory "manifest.json") ([pscustomobject]@{
        mode = "official"; gitSha = "abc123"; modelHash = "model-hash"; manifestHash = "visual-hash"
        writerHash = "writer-hash"; transportHash = "transport-hash"
        fullVisualSync = $true; writeMetadata = $false
    })
    Write-TestJson (Join-Path $metadataDirectory "manifest.json") ([pscustomobject]@{
        mode = "official"; gitSha = "abc123"; modelHash = "model-hash"; manifestHash = "metadata-hash"
        writerHash = "writer-hash"; transportHash = "transport-hash"
        fullVisualSync = $false; writeMetadata = $true
    })

    $result = Test-OfficialFigmaArtifactSet -ArtifactDirectory $fixtureRoot -ExpectedGitSha "abc123"
    if ($result.Decision -ne "partial" -or $result.ModelHash -ne "model-hash") {
        throw "Valid official artifact fixture did not produce the expected handoff."
    }

    $invalidModel = Get-Content -LiteralPath (Join-Path $fixtureRoot "design-model.json") -Raw | ConvertFrom-Json
    $invalidModel.branch = "feature/not-main"
    Write-TestJson (Join-Path $fixtureRoot "design-model.json") $invalidModel
    $rejected = $false
    try {
        Test-OfficialFigmaArtifactSet -ArtifactDirectory $fixtureRoot | Out-Null
    }
    catch {
        $rejected = $_.Exception.Message -like "*require model branch 'main'*"
    }
    if (-not $rejected) {
        throw "A branch-local design model must be rejected."
    }

    Write-Host "Figma artifact handoff tests passed."
}
finally {
    if (Test-Path -LiteralPath $fixtureRoot) {
        Remove-Item -LiteralPath $fixtureRoot -Recurse -Force
    }
}
