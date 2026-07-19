# Repository CI Planner

`repo/ci` owns the provider-neutral Kotlin contract that decides which
verification units apply to a committed repository change.

## Boundaries

- Domain models and classification do not depend on TeamCity, GitHub Actions,
  Gradle APIs, PowerShell, or Figma Design Sync.
- The Git adapter resolves the committed diff against `origin/main`.
- The Gradle plugin is the current composition root and registers
  `generateCiPlan` in the Water My Plants root build.
- CI providers consume allow-listed unit identifiers and Gradle task names;
  they must never execute arbitrary commands read from the JSON report.

The generated contract is documented in
[`docs/reference/ci-verification-plan.md`](../../docs/reference/ci-verification-plan.md).

## Verification

```powershell
.\gradlew.bat :ci:check
.\gradlew.bat generateCiPlan
```

The first rollout is observational. TeamCity publishes the plan but continues
to use the existing Figma change-impact classifier to select the authoritative
verification path.
