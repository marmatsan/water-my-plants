---
title: Git workflow standard
type: standard
scope: repository
owner: repository-tooling
status: active
last-reviewed: 2026-07-20
review-cycle-days: 180
sources:
  - docs/decisions/adr-0007-use-trunk-based-development.md
  - AGENTS.md
  - docs/ci/main-branch-protection.md
  - repo/verification-platform/domain/src/main/kotlin/com/marmatsan/verificationPlatform/domain/service/git/GitBranchNameValidator.kt
  - repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/git/CheckGitWorkflowTask.kt
---

# Git Workflow Standard

## Purpose

This standard defines the repository-wide branch, pull request, merge, and
release-tag contract. It is the normative source for Git workflow rules;
provider-specific protection and CI details remain in
[`../ci/main-branch-protection.md`](../ci/main-branch-protection.md).

## Rules

### Trunk

- `main` MUST be the only permanent branch.
- `main` MUST remain stable, tested, and releasable.
- Changes MUST enter `main` through a pull request and MUST NOT be pushed
  directly.
- A pull request MUST pass the strict `TeamCity CI` status and resolve all
  review conversations before merge.
- Pull requests MUST use squash merge. Merge commits and rebase merges into
  `main` are prohibited.

### Short-Lived Branches

| Branch                        | Purpose                                          | Example                         |
|-------------------------------|--------------------------------------------------|---------------------------------|
| `feature/<short-description>` | New user or system capability.                   | `feature/plant-reminders`       |
| `fix/<short-description>`     | Ordinary defect correction.                      | `fix/watering-date-calculation` |
| `chore/<short-description>`   | Documentation, CI, dependencies, or maintenance. | `chore/git-branching-strategy`  |
| `release/<x.y.z>`             | Exceptional stabilization for one release.       | `release/1.4.0`                 |
| `hotfix/<short-description>`  | Urgent production correction.                    | `hotfix/reminder-crash`         |

- Branches MUST start from the current `main`.
- Descriptions MUST use lowercase English `kebab-case` containing letters,
  digits, and single hyphens.
- Short-lived branches SHOULD merge or be abandoned within three working days.
  Work that exceeds that target SHOULD be split into smaller deliverable slices.
- Incomplete behavior MUST remain safe through a feature flag, hidden entry
  point, or another explicit non-public boundary.
- Branches MUST be deleted after merge or abandonment.
- `develop`, environment, personal, and permanent release branches MUST NOT be
  created.

### Pull Requests And Commits

- A pull request MUST contain one coherent change and enough verification
  evidence for that change.
- The pull request title MUST follow `<type>(<scope>): <summary>` so the squash
  commit on `main` is a Conventional Commit.
- The title summary MUST describe the concrete resulting change, not an
  activity such as "update files".
- Local commits MUST use `<type>(<scope>): <summary>` and keep one coherent
  change. The summary states the concrete result.
- Add a commit body when the reason is not evident from the diff. Explain why
  the change exists, the decision or tradeoff it preserves, and what future
  maintainers should avoid undoing accidentally.
- The branch MUST be up to date with `main` before the strict required status
  can permit merge.
- Zero approving reviews MAY be used while the repository has one maintainer.
  Once another maintainer is available, at least one approval MUST be required.

### Releases And Hotfixes

- A normal release MUST be tagged from a verified `main` commit as `vX.Y.Z`.
- A `release/<x.y.z>` branch MAY be created only when focused QA or last-mile
  stabilization cannot be completed directly through ordinary short-lived
  branches.
- Stabilization changes MUST return to `main` through a pull request before the
  release tag is created.
- A production emergency MUST use `hotfix/<short-description>`, pass the normal
  pull request and CI gates, merge to `main`, and create a new patch tag.
- Published version tags MUST NOT be moved or reused.

## Exceptions

An exception requires a documented reason in the pull request and explicit
maintainer approval. GitHub protection and required CI MUST NOT be bypassed.
Operational urgency changes prioritization, not the integrity of `main`.

## Verification

- GitHub protects `main` with strict `TeamCity CI`, linear history, pull request
  integration, and resolved review conversations.
- Repository settings allow squash merge and delete merged branches.
- `checkGitWorkflow` validates branch names locally and in TeamCity.
- `checkDocumentation` validates this standard and its linked documents.

## Sources

- [ADR-0007](../decisions/adr-0007-use-trunk-based-development.md)
- [Main branch protection](../ci/main-branch-protection.md)
- [Short-lived branch guide](../guides/work-with-short-lived-branches.md)
- [Release runbook](../runbooks/create-release.md)
- [Hotfix runbook](../runbooks/ship-hotfix.md)
