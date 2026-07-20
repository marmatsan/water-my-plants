---
title: Bootstrap The TeamCity CI Gate
type: runbook
scope: repository
owner: repository-tooling
status: active
last-reviewed: 2026-07-20
review-cycle-days: 90
sources:
  - .teamcity/settings.kts
  - .teamcity/README.md
  - docs/ci/main-branch-protection.md
  - docs/runbooks/teamcity-cloudflare-access.md
  - docs/standards/git-workflow.md
---

# Bootstrap The TeamCity CI Gate

## Purpose

Use this runbook when `main` already requires the `TeamCity CI` GitHub status
but the versioned classic `CI Gate` that publishes it must be introduced,
recreated, or recovered. The operation breaks the circular dependency long
enough to merge the gate configuration, then restores branch protection before
the first manual verification run.

Do not use this runbook for ordinary TeamCity changes. Once `CI Gate` exists,
all changes follow the normal pull request flow and must pass `TeamCity CI`.

## Prerequisites

- The implementation is on a short-lived branch created from current `main`.
- The pull request contains the versioned `CI Gate` contract from
  `.teamcity/settings.kts` and no unrelated ruleset change.
- `.\gradlew.bat checkDocumentation`, the focused TeamCity DSL checks, full
  Gradle `check`, and TeamCity settings validation have succeeded.
- The operator has repository administration permission, `gh` authentication
  that can read and update repository rulesets, and TeamCity permission to read
  projects and run `CI Gate`.
- The bootstrap run uses the default branch without comments or revision
  overrides. `COMMENT_BUILD` and `CUSTOMIZE_BUILD_REVISIONS` are intentionally
  not required TeamCity token permissions.
- No other administrator is changing the `Main` ruleset during the operation.
- A short maintenance window is approved. New merges remain paused until the
  original ruleset is restored and verified.
- PowerShell is the active shell. Set `$PrNumber` below; repository, ruleset,
  build configuration, and status names are executable contracts.

```powershell
$Repository = 'marmatsan/water-my-plants'
$RulesetName = 'Main'
$RequiredContext = 'TeamCity CI'
$GateBuildType = 'WaterMyPlants_WaterMyPlantsCiGate'
$PrNumber = 0 # Replace with the pull request number.
if ($PrNumber -le 0) { throw 'Set a valid pull request number' }
```

## Procedure

1. Confirm the pull request head and capture the current `main` revision.

   ```powershell
   $HeadSha = gh pr view $PrNumber --repo $Repository --json headRefOid --jq .headRefOid
   $PreviousMainSha = gh api "repos/$Repository/commits/main" --jq .sha
   gh pr view $PrNumber --repo $Repository --json state,isDraft,mergeable,url,headRefName,baseRefName
   ```

   Stop unless the pull request is open, ready, mergeable, targets `main`, and
   `$HeadSha` is the reviewed revision.

2. Locate exactly one active repository ruleset named `Main` and save its full
   response outside the repository.

   ```powershell
   $Rulesets = @(gh api "repos/$Repository/rulesets" | ConvertFrom-Json)
   $Matches = @($Rulesets | Where-Object { $_.name -eq $RulesetName -and $_.enforcement -eq 'active' })
   if ($Matches.Count -ne 1) { throw "Expected one active $RulesetName ruleset, found $($Matches.Count)" }

   $RulesetId = $Matches[0].id
   $BackupPath = Join-Path $env:TEMP "water-my-plants-ruleset-$RulesetId.json"
   gh api "repos/$Repository/rulesets/$RulesetId" |
     Set-Content -LiteralPath $BackupPath -Encoding utf8NoBOM
   ```

3. Prove that the saved contract contains exactly the expected required status
   rule. This runbook deliberately stops if the ruleset has evolved; update the
   procedure instead of discarding another protection accidentally.

   ```powershell
   $Original = Get-Content -LiteralPath $BackupPath -Raw | ConvertFrom-Json
   $StatusRules = @($Original.rules | Where-Object { $_.type -eq 'required_status_checks' })
   $RequiredChecks = @($StatusRules.parameters.required_status_checks)

   if ($StatusRules.Count -ne 1) { throw 'Expected exactly one required_status_checks rule' }
   if ($RequiredChecks.Count -ne 1 -or $RequiredChecks[0].context -ne $RequiredContext) {
     throw "Expected $RequiredContext to be the only required status"
   }
   if (-not $StatusRules[0].parameters.strict_required_status_checks_policy) {
     throw "$RequiredContext must be strict before bootstrap"
   }
   ```

4. Build a temporary update that removes only the
   `required_status_checks` rule. Keep enforcement and every other rule active.

   ```powershell
   $TemporaryPath = Join-Path $env:TEMP "water-my-plants-ruleset-$RulesetId-bootstrap.json"
   $TemporaryBody = [ordered]@{
     name = $Original.name
     target = $Original.target
     enforcement = $Original.enforcement
     bypass_actors = @($Original.bypass_actors)
     conditions = $Original.conditions
     rules = @($Original.rules | Where-Object { $_.type -ne 'required_status_checks' })
   }

   $TemporaryBody | ConvertTo-Json -Depth 100 |
     Set-Content -LiteralPath $TemporaryPath -Encoding utf8NoBOM
   gh api --method PUT "repos/$Repository/rulesets/$RulesetId" --input $TemporaryPath
   ```

5. Read the ruleset back. Confirm it is still active and retains deletion,
   linear-history, pull-request, and non-fast-forward protection. Do not merge
   while any of those rules is missing.

   ```powershell
   $Temporary = gh api "repos/$Repository/rulesets/$RulesetId" | ConvertFrom-Json
   $ExpectedTemporaryRules = @('deletion', 'required_linear_history', 'pull_request', 'non_fast_forward')
   $ActualTemporaryRules = @($Temporary.rules.type)

   if ($Temporary.enforcement -ne 'active') { throw 'Main ruleset enforcement is not active' }
   foreach ($Rule in $ExpectedTemporaryRules) {
     if ($Rule -notin $ActualTemporaryRules) { throw "Temporary ruleset lost $Rule" }
   }
   if ('required_status_checks' -in $ActualTemporaryRules) {
     throw 'Temporary ruleset still contains required_status_checks'
   }
   ```

6. Squash-merge only the reviewed pull request head. If the merge fails, go
   immediately to Recovery and restore the ruleset.

   ```powershell
   gh pr merge $PrNumber --repo $Repository --squash --admin --match-head-commit $HeadSha
   $MainSha = gh api "repos/$Repository/commits/main" --jq .sha
   ```

7. Wait until TeamCity imports the versioned settings and exposes
   `$GateBuildType`.

   ```powershell
   teamcity job list --project WaterMyPlants --all --json
   ```

   The stable TeamCity project configuration is loaded from `main`; a branch
   build cannot install the gate in advance. If polling or the commit hook does
   not reload settings, use **Load project settings from VCS** and inspect
   `teamcity-versioned-settings.log`.

8. Restore the exact original ruleset as soon as `CI Gate` exists. Do not wait
   for the bootstrap build to finish while branch protection is relaxed.

   ```powershell
   $Original = Get-Content -LiteralPath $BackupPath -Raw | ConvertFrom-Json
   $RestorePath = Join-Path $env:TEMP "water-my-plants-ruleset-$RulesetId-restore.json"
   $RestoreBody = [ordered]@{
     name = $Original.name
     target = $Original.target
     enforcement = $Original.enforcement
     bypass_actors = @($Original.bypass_actors)
     conditions = $Original.conditions
     rules = @($Original.rules)
   }

   $RestoreBody | ConvertTo-Json -Depth 100 |
     Set-Content -LiteralPath $RestorePath -Encoding utf8NoBOM
   gh api --method PUT "repos/$Repository/rulesets/$RulesetId" --input $RestorePath
   ```

9. Verify restoration before starting any manual build.

   ```powershell
   $Restored = gh api "repos/$Repository/rulesets/$RulesetId" | ConvertFrom-Json
   $RestoredStatusRule = @($Restored.rules | Where-Object { $_.type -eq 'required_status_checks' })
   $RestoredContexts = @($RestoredStatusRule.parameters.required_status_checks.context)

   if ($Restored.enforcement -ne 'active') { throw 'Main ruleset was not restored as active' }
   if ($RequiredContext -notin $RestoredContexts) { throw "$RequiredContext was not restored" }
   if (-not $RestoredStatusRule[0].parameters.strict_required_status_checks_policy) {
     throw "$RequiredContext was not restored as strict"
   }
   ```

10. Check whether the VCS trigger already created a gate build for `$MainSha`.
    Triggers are not retroactive: if TeamCity processed the commit before the
    new build configuration existed, no gate build will be backfilled.

    ```powershell
    teamcity build list --revision $MainSha --limit 20 --json
    ```

    If no `$GateBuildType` build exists, open `CI Gate` in the authenticated
    TeamCity UI and run it once on the default branch without a custom comment,
    revision, or parameter. Reconfirm that `main` still equals `$MainSha`
    immediately before clicking **Run**.

    If a CLI start fails with the documented Cloudflare CSRF error, use the UI.
    Do not copy cookies, disable CSRF, expose a local TeamCity port, or add
    permissions merely to attach an optional comment or custom revision.

11. Observe the GitHub status transition for the `main` commit.

    ```powershell
    gh api "repos/$Repository/commits/$MainSha/status" `
      --jq '{state: .state, statuses: [.statuses[] | {context, state, target_url, description}]}'
    ```

    The same `TeamCity CI` context must first point to the running `CI Gate` and
    report `pending`, then report `success` when the fresh `CI` dependency and
    composite gate finish.

12. Delete the temporary request and backup files only after ruleset restoration
    and GitHub status verification both succeed.

    ```powershell
    Remove-Item -LiteralPath $BackupPath, $TemporaryPath, $RestorePath -Force
    ```

    Confirm that the short-lived Git branch is deleted after merge according to
    the Git workflow standard.

## Verification

The bootstrap is complete only when all of the following are true:

- `main` contains the reviewed squash commit;
- TeamCity exposes `WaterMyPlants_WaterMyPlantsCiGate`;
- the gate runs a fresh `CI` snapshot dependency for the `main` revision;
- GitHub records exactly one authoritative `TeamCity CI` context and it changes
  from `pending` to `success`;
- the `Main` ruleset is active, requires strict `TeamCity CI`, and retains every
  non-status protection from the backup;
- no temporary ruleset file or short-lived branch remains;
- a post-gate `Figma Sync` failure is assessed independently. A `modelHash`
  mismatch means the Figma MCP publication workflow must use the generated
  `main` artifact; it does not invalidate a successful CI gate bootstrap.

## Recovery

- If any precondition or ruleset shape check fails, make no mutation and update
  this runbook to match the reviewed contract.
- If temporary relaxation succeeds but the pull request merge fails, restore
  the saved ruleset immediately and verify it before investigating the merge.
- If TeamCity cannot import `CI Gate`, restore the ruleset, pause merges, inspect
  versioned-settings generation, and deliver the correction through a new
  short-lived branch.
- If `CI Gate` runs but GitHub receives no status, keep the ruleset restored and
  inspect the generated gate XML, `teamcity-commit-status.log`, and GitHub VCS
  root permissions. Never fabricate a successful status.
- If restoration is uncertain, stop all merges and compare the live ruleset
  with the saved JSON before deleting any temporary file.

## Prohibited Actions

- Do not disable the complete `Main` ruleset or remove protections other than
  the single verified `TeamCity CI` rule.
- Do not leave branch protection relaxed while waiting for builds or debugging.
- Do not push directly to `main`, rewrite `main`, or merge a different head SHA.
- Do not add `commit-status-publisher` to Pipeline YAML or repair the status
  from the read-only Pipeline UI.
- Do not print, commit, or persist GitHub, TeamCity, or Cloudflare credentials.
- Do not disable CSRF, replay session cookies, expose `localhost:8111`, or bypass
  Cloudflare as the routine start mechanism.
- Do not treat a Figma metadata mismatch as permission to publish a local or
  short-lived-branch `design-model.json`.

## Sources

- [TeamCity versioned settings](../../.teamcity/README.md)
- [Main branch protection](../ci/main-branch-protection.md)
- [TeamCity Cloudflare Access](teamcity-cloudflare-access.md)
- [Git workflow standard](../standards/git-workflow.md)
- [Documentation standard](../documentation.md)
