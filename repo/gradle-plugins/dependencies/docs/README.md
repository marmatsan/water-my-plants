# Dependencies Settings Plugin

## Plugin

```kotlin
plugins {
    id("com.marmatsan.dependencies")
}
```

## Purpose

Configures dependency resolution and generated version catalogs for a Gradle
settings build.

Use this plugin from `settings.gradle.kts` when the build should consume the
project's dependency and plugin tree model.

## Behavior

- Requests the resolved catalog through
  `WaterMyPlantsCatalog.resolved(settings.rootDir)`. The catalog facade owns
  loading version values from `versions.properties`.
- Configures Gradle `dependencyResolutionManagement`.
- Converts the returned `DependencyCatalogTrees` into Gradle library and plugin
  version catalog entries.
- Keeps catalog alias generation centralized so consumers can request Maven
  coordinates without hardcoding generated aliases.

The plugin declares both catalog modules explicitly:

```kotlin
implementation("com.marmatsan.repo:catalog-core")
implementation("com.marmatsan.repo:water-my-plants-catalog")
```

`catalog-core` supplies reusable tree types and mappers.
`water-my-plants-catalog` supplies the concrete repository catalog facade.

## Requirements

This is a settings plugin, so it applies to `Settings`, not `Project`.

When adding or changing dependency versions:

- Update `repo/dependency-catalog/versions.properties`.
- Update `repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/Versions.kt` when a new version key is introduced.
- Update `repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt` or `repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt` for new dependency tree entries.

## Verification

For changes to this plugin, prefer focused gradle-plugins verification:

```powershell
.\gradlew.bat -p repo/gradle-plugins :dependencies:check
```
