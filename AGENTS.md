# Project Agent Instructions

## Branching

- Use Git Flow as the branching strategy.
- Treat `main` as the stable release branch.
- Treat `develop` as the integration branch for ongoing work.
- Create feature work from `develop` using `feature/<short-description>`.
- Merge completed feature branches back into `develop`.
- Delete feature branches after they have been merged into `develop`.
- Reserve `release/<version>` branches for release stabilization and `hotfix/<short-description>` branches for urgent fixes from `main`.
