# Project Config

`project-config` is the reusable composition boundary for a consuming Gradle
repository. It contains no product name, dependency tree, Figma node id,
repository path, or TeamCity build id.

The settings plugin `com.marmatsan.projectConfig.settings` captures one inline
`projectConfig { dependencyCatalog { ... } }` Kotlin tree declaration and
registers its resolved Gradle catalogs.
The same definition remains available at the project-phase composition boundary
for focused adapters that need stable version-property aliases.

The project plugin `com.marmatsan.projectConfig` verifies the matching Settings
composition and is the product-independent anchor for optional repository
capabilities. Focused adapters are added independently; the plugin does not own
Figma, TeamCity, or consumer identity.

Library and plugin trees deliberately remain inline in the consumer's root
`settings.gradle.kts`; this build does not introduce TOML, JSON, applied
scripts, or product-specific source modules for those declarations.

Verify the included build from the repository root:

```powershell
.\gradlew.bat :project-config:check
```

Verify the published, source-independent consumer contract with:

```powershell
.\gradlew.bat verifyProjectConfigDistribution
```

The source-independent `samples/health-consumer` fixture proves that a second
product can apply both entry points and declare its own catalog without a
`health-project-config` build or source substitution.

The consumer applies `com.marmatsan.projectConfig.settings` in
`settings.gradle.kts`, sets `versionsFile`, and declares its `libraries` and
`plugins` below `dependencyCatalog`. The corresponding root project applies
`com.marmatsan.projectConfig`. Catalog names default to `libs` and `plugins`
and can be changed through `librariesCatalogName` and `pluginsCatalogName`.

See the [consumer contract](docs/reference/consumer-contract.md) for the exact
application order, DSL fields, version-key semantics, invariants, and failure
behavior.
