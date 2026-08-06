---
title: Dependency version naming
type: standard
scope: repository-dependencies
owner: dependency-catalog
status: active
last-reviewed: 2026-07-29
review-cycle-days: 180
sources:
  - versions.properties
  - settings.gradle.kts
---

# Dependency Version Naming

## Purpose

Use this standard when adding or renaming entries in
root `versions.properties`.

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
- A coordinated repository plugin release train must end with
  `PluginsVersion`, for example `gradlePluginsVersion`. Use the singular suffix
  for a plugin that can be versioned independently.
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
  used exclusively by `repo/figma-documentation-sync`.
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

# Plugin ids: com.example.android, com.example.compose
gradlePluginsVersion=...
```

## Adding A Dependency

1. Add the version key under the correct section in
   root `versions.properties`.
2. Resolve the exact key with `version("<key>")` in the matching library or
   plugin declaration in
   root `settings.gradle.kts`.
3. Use the generated alias from the product module that consumes the
   dependency. Do not copy the product key into a reusable included build's
   local toolchain registry.
4. Run:

```powershell
.\gradlew.bat checkFigmaVersionNaming checkFigmaCatalogUsage
```

## CI Contract

`checkFigmaVersionNaming` is wired into the root Gradle `check` lifecycle.
TeamCity `CI` runs `.\gradlew.bat check`, so a pull request cannot merge if a
new version key breaks this naming contract.

`checkFigmaCatalogUsage` still owns the separate rule that dependency catalog
entries must be used by a module, convention plugin, or tool configuration.
