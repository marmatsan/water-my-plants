---
title: Use trunk-based development
type: adr
scope: repository
owner: repository-tooling
status: accepted
last-reviewed: 2026-07-20
review-cycle-days: 365
sources:
  - AGENTS.md
  - docs/ci/main-branch-protection.md
  - .teamcity/settings.kts
---

# ADR-0007: Use Trunk-Based Development

## Context

The repository already treats `main` as its stable and releasable branch, uses
short-lived pull request branches, and requires `TeamCity CI` before merge.
Those rules were split between agent instructions and CI documentation, while
the daily branch lifecycle, release stabilization, and hotfix recovery were not
owned by one canonical engineering standard.

The project currently has one maintainer, one protected trunk, and a CI pipeline
that validates branches and pull requests before running separate post-merge
Figma documentation verification. The branching model must keep that feedback
loop short without introducing permanent integration or environment branches.

## Decision

Use trunk-based development with `main` as the only permanent branch. Changes
enter `main` through short-lived `feature/*`, `fix/*`, or `chore/*` branches and
are integrated by squash merge after required verification succeeds.

Keep `fix/*` as a semantic distinction for ordinary defect correction even
though trunk-based development does not require type-specific branch prefixes.
Use temporary `release/<x.y.z>` branches only when focused stabilization is
necessary and `hotfix/*` branches only for urgent production corrections.

Mark releases with immutable semantic-version tags in the form `vX.Y.Z` on
verified `main` commits. Use feature flags or hidden entry points when incomplete
work cannot be delivered safely in one short-lived branch.

## Consequences

- `main` remains the only integration and release line.
- Pull requests stay small and are expected to merge within three working days.
- Squash merge makes the Conventional Commit pull request title the single
  reviewed commit that reaches `main`.
- Release and hotfix branches are exceptional and are deleted after merge.
- No `develop`, environment, personal, or permanent release branches are used.
- Zero approving reviews are acceptable while the repository has one
  maintainer; at least one approval becomes mandatory when another maintainer
  can review changes.
- Branch naming and lifecycle rules become candidates for executable
  verification rather than remaining prose-only conventions.

## Alternatives

- GitFlow with permanent `develop` and release branches. Rejected because it
  delays integration and duplicates the role of the protected trunk.
- Direct pushes to `main`. Rejected because they bypass the required CI and
  review evidence.
- One generic `change/*` prefix. Rejected because `feature/*`, `fix/*`, and
  `chore/*` communicate intent and align with the repository's Conventional
  Commit history at little operational cost.
- Rebase merge as an additional merge method. Rejected because squash gives
  every pull request one predictable, reviewed commit on `main`.

## Supersession

None.
