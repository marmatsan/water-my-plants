---
title: Dependency version naming
type: standard
scope: repository-dependencies
owner: dependency-catalog
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - repo/dependency-catalog/versions.properties
  - repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/Versions.kt
---

# Dependency Version Naming

## Purpose

Use this standard when adding or renaming entries in
`repo/dependency-catalog/versions.properties`.

The names are rendered in the Figma `versions` section, so the file must keep a
stable semantic grouping instead of using generic `*Version` keys.

## Required Format

`versions.properties` must contain these sections in this order:

```properties
## Main project dependencies
androidGradlePluginVersion=...
kotlinVersion=...
## Libraries
exampleLibraryVersion=...
## Plugins
examplePluginVersion=...
```

Rules:

- `Main project dependencies` contains only `androidGradlePluginVersion` and
  `kotlinVersion`.
- Library version keys must end with `LibraryVersion`, for example
  `activityComposeLibraryVersion`.
- Plugin version keys must end with `PluginVersion`, for example
  `kspPluginVersion`.
- Do not add new generic `*Version` keys to `Libraries` or `Plugins`.
- If a plugin catalog node uses `androidGradlePluginVersion` or `kotlinVersion`, keep
  the version key in `Main project dependencies`; do not duplicate it as a
  plugin-specific key.

## File Style

- Keep keys alphabetically ordered inside each section. The generated model
  sorts keys as well, but the source file must remain easy to scan and review.
- Leave one blank line between version declarations.
- Use one comment line per Maven coordinate when a version owns several
  artifacts.
- Describe Gradle plugins with `Plugin id:` and use `Artifact:` when the build
  also consumes an implementation artifact.
- Add `Consumer:` only when ownership is not evident, for example a version
  used exclusively by `repo/figma-design-sync`.
- Use `#` for comments. Only lines beginning with `## ` declare sections in the
  generated Figma model.
- Do not append comments after a property value. Java `Properties` treats the
  trailing text as part of the value.

Example:

```properties
# io.kotest:kotest-assertions-core
# io.kotest:kotest-runner-junit5
kotestLibraryVersion=...

# Plugin id: org.jetbrains.dokka
# Artifact: org.jetbrains.dokka:dokka-gradle-plugin
dokkaPluginVersion=...
```

## Adding A Dependency

1. Add the version key under the correct section in
   `repo/dependency-catalog/versions.properties`.
2. Add the matching property in
   `repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/Versions.kt`.
3. Wire the key into `LibraryTrees.kt` or `PluginTrees.kt`.
4. Update any included-build settings catalog that consumes the key, such as
   `repo/gradle-plugins/settings.gradle.kts` or
   `repo/figma-design-sync/settings.gradle.kts`.
5. Run:

```powershell
.\gradlew.bat checkFigmaVersionNaming checkFigmaCatalogUsage
```

## CI Contract

`checkFigmaVersionNaming` is wired into the root Gradle `check` lifecycle.
TeamCity `CI` runs `.\gradlew.bat check`, so a pull request cannot merge if a
new version key breaks this naming contract.

`checkFigmaCatalogUsage` still owns the separate rule that dependency catalog
entries must be used by a module, convention plugin, or tool configuration.
