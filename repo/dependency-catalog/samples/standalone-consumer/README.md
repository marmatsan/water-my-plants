# Standalone Dependency Catalog Consumer

This fixture resolves the published catalog plugin and API exclusively from a
staging Maven repository. It supplies its own `ResolvedDependencyCatalogProvider` and
contains no `includeBuild` or Water My Plants implementation dependency.

Run it through the owning build:

```powershell
.\gradlew.bat -p repo\dependency-catalog verifyStagedPublication
```
