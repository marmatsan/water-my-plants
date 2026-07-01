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

- Loads version values from `versions.properties`.
- Configures Gradle `dependencyResolutionManagement`.
- Generates library and plugin version catalog entries from the dependency tree
  model.
- Keeps catalog alias generation centralized so consumers can request Maven
  coordinates without hardcoding generated aliases.

## Requirements

This is a settings plugin, so it applies to `Settings`, not `Project`.

When adding or changing dependency versions:

- Update `build-logic/versions.properties`.
- Update `Versions.kt` when a new version key is introduced.
- Update `LibraryTrees.kt` or `PluginTrees.kt` for new dependency tree entries.

## Verification

For changes to this plugin, prefer focused build-logic verification:

```powershell
.\gradlew.bat -p build-logic :dependencies:check
```
