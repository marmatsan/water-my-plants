[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
Push-Location $repositoryRoot
try {
    $impact = & (Join-Path $PSScriptRoot "get-change-impact.ps1") -FailOnDocumentationGap -AsJson |
        ConvertFrom-Json

    if ($impact.scope -eq "documentation-only") {
        if ([string]::IsNullOrWhiteSpace($impact.comparisonBase)) {
            throw "Documentation-only verification requires a comparison base."
        }

        & git diff --check "$($impact.comparisonBase)..HEAD"
        if ($LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }

        foreach ($path in @($impact.changedPaths | Where-Object { $_ -like "*.md" })) {
            $file = Join-Path $repositoryRoot $path
            $sourceDirectory = Split-Path -Parent $file
            $matches = [regex]::Matches((Get-Content -LiteralPath $file -Raw), '(?<!\!)\[[^\]]*\]\((?<target>[^ )]+)')
            foreach ($match in $matches) {
                $target = $match.Groups["target"].Value.Trim("<>")
                if ($target -match "^(https?:|mailto:|#)") {
                    continue
                }

                $targetPath = ($target -split "[#?]", 2)[0]
                if ([string]::IsNullOrWhiteSpace($targetPath)) {
                    continue
                }

                $resolvedTarget = if ($targetPath.StartsWith("/")) {
                    Join-Path $repositoryRoot $targetPath.TrimStart("/")
                } else {
                    Join-Path $sourceDirectory $targetPath
                }
                if (-not (Test-Path -LiteralPath $resolvedTarget)) {
                    throw "Broken local Markdown link in '$path': $target"
                }
            }
        }

        Write-Host "Documentation-only change verified; Gradle check is not required."
        exit 0
    }

    & .\gradlew.bat check --stacktrace
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
