---
title: Ship a production hotfix
type: runbook
scope: repository
owner: repository-tooling
status: active
last-reviewed: 2026-07-20
review-cycle-days: 90
sources:
  - docs/standards/git-workflow.md
  - docs/ci/main-branch-protection.md
---

# Ship A Production Hotfix

## Purpose

Deliver an urgent production correction without bypassing the protected trunk,
required CI, or immutable release history.

## Prerequisites

- A confirmed production defect requiring priority over ordinary work.
- The current released version and the next unused patch version.
- Maintainer permission to merge pull requests and push version tags.
- A clean checkout of the latest `main`.

## Procedure

1. Create the hotfix from current `main`:

   ```powershell
   git switch main
   git pull --ff-only
   git switch -c hotfix/reminder-crash
   ```

2. Implement the smallest safe correction and add regression coverage.

3. Run focused tests and the exhaustive repository check:

   ```powershell
   .\gradlew.bat check
   git diff --check
   ```

4. Commit and publish the branch:

   ```powershell
   git add <paths>
   git commit -m "fix(reminders): prevent production notification crash"
   git push --set-upstream origin hotfix/reminder-crash
   ```

5. Open a pull request to `main`, resolve every review conversation, and wait
   for strict `TeamCity CI` success.

6. Squash merge the pull request. Refresh `main` and create the next patch tag:

   ```powershell
   git switch main
   git pull --ff-only
   git tag -a v1.4.1 -m "Release v1.4.1"
   git push origin v1.4.1
   git fetch --prune
   ```

## Verification

- The regression test fails without the hotfix and passes with it.
- `TeamCity CI` succeeds on the pull request and the resulting `main` commit.
- The patch tag resolves to the squash commit on `main`.
- GitHub no longer contains the hotfix branch.

## Recovery

- If CI fails, keep the branch unmerged and correct it normally.
- If the merged hotfix causes another defect, revert or correct it through a new
  `hotfix/*` pull request and publish a new patch version.
- Never repair a published release by moving its existing tag.

## Prohibited Actions

- Do not push directly to `main`.
- Do not bypass required status checks or unresolved review conversations.
- Do not reduce regression coverage merely to shorten hotfix delivery.
- Do not force-update or reuse a published version tag.

## Sources

- [Git workflow standard](../standards/git-workflow.md)
- [Main branch protection](../ci/main-branch-protection.md)
- [Release runbook](create-release.md)
