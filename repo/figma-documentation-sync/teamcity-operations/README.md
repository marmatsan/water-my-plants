# TeamCity Figma Operations

`teamcity-operations` is the optional Gradle operations boundary for
Figma Documentation Sync repositories that use TeamCity. It owns canonical
artifact handoff, verified PNG upload, idempotent rerun behavior, Cloudflare
credential adapters, and their Gradle tasks.

The plugin id is:

```text
com.marmatsan.figmaDocumentationSync.teamcityOperations
```

Consumers configure the `figmaTeamCityOperations` extension with their own
TeamCity build configuration, canonical branch, accepted branch aliases,
artifact-producing job name, and public HTTPS origin. This module contains no
Water My Plants identity or path.

Dependency direction remains one-way:

```text
teamcity-operations -> teamcity-adapter -> data -> domain
teamcity-operations -> data + domain
```

The main Figma Gradle plugin does not depend on this module. Repositories that
do not use TeamCity omit it.

Verify the operations plugin from the repository root:

```powershell
.\gradlew.bat :figma-documentation-sync:teamcity-operations:check
```
