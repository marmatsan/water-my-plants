# Standalone Consumer Fixture

This fixture resolves `com.marmatsan.figmaDocumentationSync` only from staged Maven
artifacts. It must not add `repo/figma-documentation-sync` as an included build.

Run it through the owning build so the portable artifacts are staged first:

```powershell
.\gradlew.bat :figma-documentation-sync:verifyStagedPublication
```
