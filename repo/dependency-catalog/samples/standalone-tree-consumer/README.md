# Standalone Tree Catalog Consumer

This fixture applies `com.marmatsan.dependencyCatalog.tree` exclusively from a
staged Maven repository. It verifies compact tree expansion, local
`versions.properties` ownership, and generated library and plugin accessors
without including the Dependency Catalog source build.

Run it through the owning distribution gate:

```powershell
.\gradlew.bat -p repo\dependency-catalog verifyStagedPublication
```
