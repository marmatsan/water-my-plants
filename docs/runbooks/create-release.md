---
title: Create a release
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

# Create A Release

## Purpose

Create an immutable semantic-version tag from a verified `main` commit. Use a
temporary release branch only when focused QA or last-mile stabilization is
required.

## Prerequisites

- Maintainer permission to create and push tags.
- A chosen semantic version `X.Y.Z` that has not been published previously.
- A clean local checkout with access to `origin`.
- Successful `TeamCity CI` on the `main` commit to release.
- Completed required Figma documentation publication for that commit.

## Procedure

1. Refresh `main` and verify the intended commit:

   ```powershell
   git switch main
   git pull --ff-only
   git status --short --branch
   git log -1 --oneline
   .\gradlew.bat check
   ```

2. For a normal release, create and push an annotated tag:

   ```powershell
   git tag -a v1.4.0 -m "Release v1.4.0"
   git push origin v1.4.0
   ```

3. When stabilization is required, create the exceptional branch before the
   final tag:

   ```powershell
   git switch -c release/1.4.0
   git push --set-upstream origin release/1.4.0
   ```

4. Apply only release-blocking changes, verify them, and open a pull request
   from `release/1.4.0` to `main` with a Conventional Commit title.

5. Squash merge the release pull request after `TeamCity CI` succeeds. Return to
   the updated `main`, rerun step 1, and create the version tag there.

## Verification

```powershell
git show --no-patch --decorate v1.4.0
git ls-remote --tags origin refs/tags/v1.4.0
```

The tag resolves to the intended verified `main` commit, and any temporary
release branch has been removed after merge.

## Recovery

- If an incorrect tag has not been pushed, delete it locally with
  `git tag -d v1.4.0` and recreate it correctly.
- Do not move or reuse a published tag. Correct a published release with a new
  patch version such as `v1.4.1`.
- If release verification fails, do not tag. Fix the failure through a normal
  pull request or abandon the temporary release branch.

## Prohibited Actions

- Do not tag an unverified branch or detached local commit.
- Do not tag the release-branch tip before its changes reach `main`.
- Do not force-push, move, or reuse a published version tag.
- Do not use a release branch as a permanent integration branch.

## Sources

- [Git workflow standard](../standards/git-workflow.md)
- [Main branch protection](../ci/main-branch-protection.md)
- [Hotfix runbook](ship-hotfix.md)
