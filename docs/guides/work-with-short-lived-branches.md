---
title: Work with short-lived Git branches
type: guide
scope: repository
owner: repository-tooling
status: active
last-reviewed: 2026-07-20
review-cycle-days: 180
sources:
  - docs/standards/git-workflow.md
  - AGENTS.md
---

# Work With Short-Lived Git Branches

## Outcome

Create one focused branch from the latest `main`, verify it, merge it through a
pull request, and remove its local and remote references.

## Applicable Standards

Follow the [Git workflow standard](../standards/git-workflow.md) and use the
commit structure in [`AGENTS.md`](../../AGENTS.md).

## Steps

1. Start from a clean, current trunk:

   ```powershell
   git switch main
   git pull --ff-only
   git status --short --branch
   ```

2. Choose the branch type by outcome and create it. For example:

   ```powershell
   git switch -c feature/plant-reminders
   ```

   Use `fix/*` for an ordinary defect and `chore/*` for documentation, CI,
   dependencies, or maintenance.

3. Keep the branch focused. Commit coherent checkpoints with Conventional
   Commit messages:

   ```powershell
   git add <paths>
   git commit -m "feat(reminders): add scheduled watering notifications"
   ```

4. Run the checks proportional to the change. The exhaustive default is:

   ```powershell
   .\gradlew.bat check
   ```

   Documentation changes additionally run:

   ```powershell
   .\gradlew.bat checkDocumentation
   ```

5. Publish the branch and open a pull request to `main`:

   ```powershell
   git push --set-upstream origin feature/plant-reminders
   ```

   Use a Conventional Commit pull request title because squash merge promotes
   that title to the reviewed commit on `main`.

6. If `main` advances before merge, update the branch and rerun verification:

   ```powershell
   git fetch origin
   git rebase origin/main
   git push --force-with-lease
   ```

7. Merge only after `TeamCity CI` passes and all review conversations are
   resolved. Use squash merge.

8. Refresh the local checkout and remove stale references:

   ```powershell
   git switch main
   git pull --ff-only
   git branch -d feature/plant-reminders
   git fetch --prune
   ```

## Verification

- `git status --short --branch` shows clean `main` aligned with `origin/main`.
- The pull request appears as one Conventional Commit on `main`.
- GitHub no longer contains the merged short-lived branch.
- Required local checks and `TeamCity CI` succeeded.

## Related Documentation

- [Main branch protection](../ci/main-branch-protection.md)
- [Create a release](../runbooks/create-release.md)
- [Ship a hotfix](../runbooks/ship-hotfix.md)
