$ErrorActionPreference = "Stop"

$validator = (Resolve-Path (Join-Path $PSScriptRoot "../validate-documentation.ps1")).Path
$fixtureRoot = Join-Path ([System.IO.Path]::GetTempPath()) "water-my-plants-doc-validation-$([Guid]::NewGuid())"

function Write-Fixture([string]$RelativePath, [string]$Content) {
    $path = Join-Path $fixtureRoot $RelativePath
    New-Item -ItemType Directory -Path (Split-Path -Parent $path) -Force | Out-Null
    Set-Content -LiteralPath $path -Value $Content -Encoding utf8
}

function Assert-Fails([scriptblock]$Action, [string]$ExpectedText) {
    $failed = $false
    try {
        & $Action | Out-Null
    } catch {
        $failed = $_.Exception.Message -like "*$ExpectedText*"
    }
    if (-not $failed) {
        throw "Expected validation failure containing '$ExpectedText'."
    }
}

$validStandard = @'
---
title: Example standard
type: standard
scope: repository
owner: engineering
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - source.txt
---

# Example Standard

## Rules

Example.
'@

$validRunbook = @'
---
title: Example operation
type: runbook
scope: repository
owner: operations
status: active
last-reviewed: 2026-07-18
review-cycle-days: 90
sources:
  - source.txt
---

# Example Operation

## Purpose
Example.
## Prerequisites
Example.
## Procedure
Example.
## Verification
Example.
## Recovery
Example.
## Prohibited Actions
Example.
## Sources
Example.
'@

try {
    New-Item -ItemType Directory -Path $fixtureRoot -Force | Out-Null
    Set-Content -LiteralPath (Join-Path $fixtureRoot "source.txt") -Value "source" -Encoding utf8
    Write-Fixture "docs/standards/example.md" $validStandard
    Write-Fixture "docs/runbooks/example.md" $validRunbook

    & $validator -RepositoryRoot $fixtureRoot | Out-Null

    Write-Fixture "docs/misplaced.md" ($validStandard -replace 'type: standard', 'type: guide')
    Assert-Fails { & $validator -RepositoryRoot $fixtureRoot } "outside its canonical directory"
    Remove-Item -LiteralPath (Join-Path $fixtureRoot "docs/misplaced.md")

    Write-Fixture "docs/runbooks/incomplete.md" ($validRunbook -replace '(?ms)## Recovery\r?\nExample\.\r?\n', '')
    Assert-Fails { & $validator -RepositoryRoot $fixtureRoot } "Runbook section 'Recovery'"
    Remove-Item -LiteralPath (Join-Path $fixtureRoot "docs/runbooks/incomplete.md")

    Write-Fixture "docs/standards/broken-link.md" ($validStandard + "`n[Missing](missing.md)`n")
    Assert-Fails { & $validator -RepositoryRoot $fixtureRoot } "Broken local Markdown link"

    Write-Host "validate-documentation tests passed"
} finally {
    if (Test-Path -LiteralPath $fixtureRoot) {
        Remove-Item -LiteralPath $fixtureRoot -Recurse -Force
    }
}
