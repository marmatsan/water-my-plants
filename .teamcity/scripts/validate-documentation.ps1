[CmdletBinding()]
param(
    [string]$RepositoryRoot,
    [string]$CoverageManifestPath,
    [string[]]$ChangedPath,
    [switch]$AsJson,
    [switch]$FailOnWarnings,
    [switch]$FailOnCoverageGap
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($RepositoryRoot)) {
    $RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
} else {
    $RepositoryRoot = (Resolve-Path $RepositoryRoot).Path
}

if ([string]::IsNullOrWhiteSpace($CoverageManifestPath)) {
    $CoverageManifestPath = Join-Path $RepositoryRoot ".teamcity/documentation-coverage.json"
}

$errors = [System.Collections.Generic.List[string]]::new()
$warnings = [System.Collections.Generic.List[string]]::new()
$validatedDocuments = [System.Collections.Generic.List[string]]::new()

function Normalize-Path([string]$Path) {
    return $Path.Replace("\", "/").TrimStart("./")
}

function Test-PathMatchesAny([string]$Path, [object[]]$Patterns) {
    $normalizedPath = Normalize-Path $Path
    return @($Patterns | Where-Object { $normalizedPath -like (Normalize-Path $_) }).Count -gt 0
}

function Get-RepositoryChangedPaths {
    & git -C $RepositoryRoot rev-parse --verify origin/main 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot resolve origin/main. Documentation coverage fails closed because the change scope is unknown."
    }

    $head = (& git -C $RepositoryRoot rev-parse HEAD).Trim()
    $main = (& git -C $RepositoryRoot rev-parse origin/main).Trim()
    $base = if ($head -eq $main) {
        (& git -C $RepositoryRoot rev-parse "$head^").Trim()
    } else {
        (& git -C $RepositoryRoot merge-base HEAD origin/main).Trim()
    }

    return @(& git -C $RepositoryRoot diff --name-only --diff-filter=ACMR "$base..$head" |
        ForEach-Object { Normalize-Path $_ })
}

function Get-RelativePath([string]$Path) {
    $root = [System.IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('\', '/')
    $rootWithSeparator = $root + [System.IO.Path]::DirectorySeparatorChar
    $fullPath = [System.IO.Path]::GetFullPath($Path)
    if ($fullPath -eq $root) {
        return ""
    }
    if (-not $fullPath.StartsWith($rootWithSeparator, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Documentation path is outside the repository: $fullPath"
    }
    return Normalize-Path $fullPath.Substring($rootWithSeparator.Length)
}

function Get-ExpectedType([string]$RelativePath) {
    if ($RelativePath -eq "docs/documentation.md") {
        return "standard"
    }

    if ($RelativePath -match '^docs/decisions/adr-[0-9]{4}-[a-z0-9]+(?:-[a-z0-9]+)*\.md$') {
        return "adr"
    }

    if ($RelativePath -match '(?:^|/)docs/(?<kind>standards|guides|reference|runbooks)/.+\.md$' -and
        -not $RelativePath.EndsWith("/README.md")) {
        $type = switch ($Matches.kind) {
            "standards" { "standard" }
            "guides" { "guide" }
            "reference" { "reference" }
            "runbooks" { "runbook" }
        }
        return $type
    }

    return $null
}

function Read-Frontmatter([string]$Content) {
    $match = [regex]::Match($Content, '\A---\r?\n(?<yaml>.*?)\r?\n---(?:\r?\n|\z)', 'Singleline')
    if (-not $match.Success) {
        return $null
    }

    $metadata = @{}
    $sources = [System.Collections.Generic.List[string]]::new()
    $currentKey = $null
    foreach ($line in ($match.Groups["yaml"].Value -split '\r?\n')) {
        if ($line -match '^(?<key>[a-z][a-z0-9-]*):(?:\s*(?<value>.*))?$') {
            $currentKey = $Matches.key
            $metadata[$currentKey] = $Matches.value.Trim().Trim('"', "'")
            continue
        }

        if ($currentKey -eq "sources" -and $line -match '^\s+-\s+(?<value>.+?)\s*$') {
            $sources.Add($Matches.value.Trim().Trim('"', "'"))
        }
    }
    $metadata["sources"] = @($sources)

    return [PSCustomObject]@{
        metadata = $metadata
        body = $Content.Substring($match.Length)
    }
}

function Add-Error([string]$Path, [string]$Message) {
    $errors.Add("[$Path] $Message")
}

function Test-RequiredMetadata([string]$Path, [hashtable]$Metadata, [string]$ExpectedType) {
    $requiredFields = @("title", "type", "scope", "owner", "status", "last-reviewed", "review-cycle-days")
    foreach ($field in $requiredFields) {
        if (-not $Metadata.ContainsKey($field) -or [string]::IsNullOrWhiteSpace($Metadata[$field])) {
            Add-Error $Path "Missing frontmatter field '$field'."
        }
    }

    if ($Metadata["type"] -ne $ExpectedType) {
        Add-Error $Path "Frontmatter type '$($Metadata['type'])' does not match path type '$ExpectedType'."
    }

    if ($Metadata["status"] -notin @("draft", "active", "accepted", "deprecated", "superseded")) {
        Add-Error $Path "Unsupported status '$($Metadata['status'])'."
    }

    foreach ($field in @("title", "scope", "owner")) {
        if ($Metadata[$field] -match '(?i)replace|repository-or|stable-area|placeholder') {
            Add-Error $Path "Frontmatter field '$field' still contains template text."
        }
    }

    $reviewDate = [DateTime]::MinValue
    $validReviewDate = [DateTime]::TryParseExact(
        $Metadata["last-reviewed"],
        "yyyy-MM-dd",
        [Globalization.CultureInfo]::InvariantCulture,
        [Globalization.DateTimeStyles]::None,
        [ref]$reviewDate
    )
    if (-not $validReviewDate) {
        Add-Error $Path "last-reviewed must use YYYY-MM-DD."
    }

    $reviewCycleDays = 0
    if (-not [int]::TryParse($Metadata["review-cycle-days"], [ref]$reviewCycleDays) -or $reviewCycleDays -le 0) {
        Add-Error $Path "review-cycle-days must be a positive integer."
    } elseif ($validReviewDate -and $reviewDate.AddDays($reviewCycleDays) -lt [DateTime]::Today) {
        $warnings.Add("[$Path] Documentation review expired on $($reviewDate.AddDays($reviewCycleDays).ToString('yyyy-MM-dd')).")
    }

    $sources = @($Metadata["sources"])
    if ($sources.Count -eq 0) {
        Add-Error $Path "At least one canonical source is required."
    }
    foreach ($source in $sources) {
        if ($Metadata["status"] -eq "superseded") {
            continue
        }
        if ($source -match '^(https?:|generated:)') {
            continue
        }
        $sourcePath = Join-Path $RepositoryRoot $source
        $exists = if ($source -match '[*?]') {
            @(Get-ChildItem -Path $sourcePath -ErrorAction SilentlyContinue).Count -gt 0
        } else {
            Test-Path -LiteralPath $sourcePath
        }
        if (-not $exists) {
            Add-Error $Path "Canonical source does not exist: $source"
        }
    }
}

function Test-Headings([string]$Path, [string]$Body, [string]$ExpectedType) {
    if ($Body -notmatch '(?m)^#\s+\S') {
        Add-Error $Path "A level-one title is required after frontmatter."
    }

    $headings = @([regex]::Matches($Body, '(?m)^##\s+(?<name>.+?)\s*$') | ForEach-Object {
        $_.Groups["name"].Value.Trim().ToLowerInvariant()
    })

    if ($ExpectedType -eq "runbook") {
        $requirements = @(
            @{ name = "Purpose"; aliases = @("purpose") },
            @{ name = "Prerequisites"; aliases = @("prerequisites") },
            @{ name = "Verification"; aliases = @("verification", "verify") },
            @{ name = "Recovery"; aliases = @("recovery", "failure recovery") },
            @{ name = "Prohibited Actions"; aliases = @("prohibited actions") },
            @{ name = "Sources"; aliases = @("sources") }
        )
        foreach ($requirement in $requirements) {
            if (@($headings | Where-Object { $_ -in $requirement.aliases }).Count -eq 0) {
                Add-Error $Path "Runbook section '$($requirement.name)' is required."
            }
        }
    }

    if ($ExpectedType -eq "adr") {
        foreach ($heading in @("context", "decision", "consequences", "alternatives", "supersession")) {
            if ($heading -notin $headings) {
                Add-Error $Path "ADR section '$heading' is required."
            }
        }
    }
}

$markdownFiles = @(Get-ChildItem -LiteralPath $RepositoryRoot -Filter "*.md" -File -Recurse | Where-Object {
    $relative = Get-RelativePath $_.FullName
    $relative -notmatch '(^|/)(\.git|build|node_modules|tmp)/' -and
    $relative -notmatch '(^|/)docs/templates/'
})

foreach ($file in $markdownFiles) {
    $path = Get-RelativePath $file.FullName
    $content = Get-Content -LiteralPath $file.FullName -Raw
    $expectedType = Get-ExpectedType $path
    $frontmatter = Read-Frontmatter $content

    if ($null -eq $expectedType) {
        if ($null -ne $frontmatter -and $frontmatter.metadata["type"] -in @("standard", "guide", "runbook", "reference", "adr")) {
            Add-Error $path "Typed document is outside its canonical directory."
        }
    } else {
        $validatedDocuments.Add($path)
        if ($null -eq $frontmatter) {
            Add-Error $path "Typed document must start with YAML frontmatter."
        } else {
            Test-RequiredMetadata $path $frontmatter.metadata $expectedType
            Test-Headings $path $frontmatter.body $expectedType
        }
    }

    $sourceDirectory = Split-Path -Parent $file.FullName
    $links = [regex]::Matches($content, '(?<!\!)\[[^\]]*\]\((?<target>[^ )]+)')
    foreach ($link in $links) {
        $target = $link.Groups["target"].Value.Trim("<>")
        if ($target -match '^(https?:|mailto:|#)') {
            continue
        }
        $targetPath = ($target -split '[#?]', 2)[0]
        if ([string]::IsNullOrWhiteSpace($targetPath)) {
            continue
        }
        $resolvedTarget = if ($targetPath.StartsWith("/")) {
            Join-Path $RepositoryRoot $targetPath.TrimStart("/")
        } else {
            Join-Path $sourceDirectory $targetPath
        }
        if (-not (Test-Path -LiteralPath $resolvedTarget)) {
            Add-Error $path "Broken local Markdown link: $target"
        }
    }
}

$coverageViolations = [System.Collections.Generic.List[object]]::new()
if ($FailOnCoverageGap -or $PSBoundParameters.ContainsKey("ChangedPath")) {
    if (-not (Test-Path -LiteralPath $CoverageManifestPath)) {
        throw "Documentation coverage manifest was not found: $CoverageManifestPath"
    }

    $manifest = Get-Content -LiteralPath $CoverageManifestPath -Raw | ConvertFrom-Json
    $changedPaths = if ($PSBoundParameters.ContainsKey("ChangedPath")) {
        @($ChangedPath | ForEach-Object { Normalize-Path $_ } | Where-Object { $_ })
    } else {
        @(Get-RepositoryChangedPaths)
    }
    foreach ($rule in @($manifest.rules)) {
        $changedSources = @($changedPaths | Where-Object { Test-PathMatchesAny $_ @($rule.sourcePaths) })
        if ($changedSources.Count -eq 0) {
            continue
        }

        $changedDocumentation = @(
            $changedPaths | Where-Object { Test-PathMatchesAny $_ @($rule.documentationPaths) }
        )
        if ($changedDocumentation.Count -eq 0) {
            $coverageViolations.Add(
                [PSCustomObject]@{
                    rule = $rule.id
                    changedSources = $changedSources
                    requiredDocumentation = @($rule.documentationPaths)
                }
            )
        }
    }
}

$result = [PSCustomObject]@{
    validatedDocuments = @($validatedDocuments)
    errors = @($errors)
    warnings = @($warnings)
    coverageViolations = @($coverageViolations)
}

if ($AsJson) {
    $result | ConvertTo-Json -Depth 4
} else {
    foreach ($warning in $warnings) {
        Write-Warning $warning
    }
}

if ($errors.Count -gt 0) {
    throw "Documentation validation failed:`n$($errors -join "`n")"
}
if ($FailOnWarnings -and $warnings.Count -gt 0) {
    throw "Documentation validation warnings are configured as failures:`n$($warnings -join "`n")"
}
if ($FailOnCoverageGap -and $coverageViolations.Count -gt 0) {
    $details = $coverageViolations | ForEach-Object {
        "[$($_.rule)] changed: $($_.changedSources -join ', '); update one of: " +
            "$($_.requiredDocumentation -join ', ')"
    }
    throw "Documentation coverage is incomplete.`n$($details -join "`n")"
}

if (-not $AsJson) {
    Write-Host "Documentation validation passed for $($validatedDocuments.Count) typed documents."
}
