# Project Agent Instructions

## Branching

- Use trunk-based development as the branching strategy.
- Treat `main` as the trunk and keep it stable, tested, and releasable.
- Create short-lived branches from `main` using `feature/<short-description>`, `fix/<short-description>`, or `chore/<short-description>`.
- Keep branches small and merge them back into `main` quickly through pull requests.
- Prefer feature flags or hidden entry points for incomplete work instead of long-running branches.
- Delete short-lived branches after they have been merged into `main`.
- Use version tags such as `v1.4.0` to mark releases.
- Create temporary `release/<version>` stabilization branches only when a release needs focused QA or last-mile fixes.
- Create `hotfix/<short-description>` branches from `main` only for urgent production fixes, then merge the fix back into `main` and tag the patch release.
