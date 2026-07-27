# Standalone Consumer Fixture

This fixture resolves `com.marmatsan.dependencyCatalog` and
`com.marmatsan.figmaDocumentationSync` only from staged Maven artifacts. It owns
its provider and `versions.properties`; it must not add either source build as
an included build.

Run it through the owning build so the portable artifacts are staged first:

```powershell
.\gradlew.bat :figma-documentation-sync:verifyStagedPublication
```
