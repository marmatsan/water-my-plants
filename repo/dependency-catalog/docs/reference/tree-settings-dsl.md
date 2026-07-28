---
title: Tree settings dependency catalog DSL API
type: reference
scope: repo/dependency-catalog
owner: dependency-catalog
status: active
last-reviewed: 2026-07-28
review-cycle-days: 180
sources:
  - repo/dependency-catalog/build.gradle.kts
  - repo/dependency-catalog/settings.gradle.kts
  - repo/dependency-catalog/catalog-tree-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/tree/TreeDependencyCatalogSettingsExtension.kt
  - repo/dependency-catalog/catalog-tree-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/tree/dsl/LibraryCatalogTreesScope.kt
  - repo/dependency-catalog/catalog-tree-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/tree/dsl/PluginCatalogTreesScope.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/library/LibraryScope.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/plugin/PluginScope.kt
  - repo/dependency-catalog/catalog-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/VersionCatalogBuilderExtension.kt
  - repo/dependency-catalog/samples/standalone-tree-consumer/settings.gradle.kts
---

# Tree Settings Dependency Catalog DSL API

## Purpose

This reference defines the exact public settings DSL exposed by the
`com.marmatsan.dependencyCatalog.tree` plugin, including defaults, declaration
parameters, generated coordinates and aliases, registration timing, and
validation failures.

## Contract

Applying `com.marmatsan.dependencyCatalog.tree` registers the
`dependencyCatalogTree` extension on `Settings`. The declaration hierarchy is:

```text
dependencyCatalogTree
├── versionsFile
├── librariesCatalogName
├── pluginsCatalogName
├── libraries
│   └── root
│       └── library
│           ├── artifact
│           ├── artifactsBundle
│           └── library
└── plugins
    └── root
        └── plugin
            └── plugin
```

### Settings properties and operations

| API | Type/default | Contract |
|-----|--------------|----------|
| `versionsFile` | `RegularFileProperty`; `<settings-dir>/versions.properties` | Consumer-owned property file used by every `version(key)` lookup. Configure it before the first lookup. |
| `librariesCatalogName` | `Property<String>`; `libs` | Name of the generated library version catalog and its type-safe root accessor. |
| `pluginsCatalogName` | `Property<String>`; `plugins` | Name of the generated plugin version catalog and its type-safe root accessor. |
| `libraries { ... }` | `LibraryCatalogTreesScope` | Accumulates Maven group roots. Multiple calls contribute to the same generated library catalog. |
| `plugins { ... }` | `PluginCatalogTreesScope` | Accumulates Gradle plugin id roots. Multiple calls contribute to the same generated plugin catalog. |
| `version(key)` | `String` | Returns the value of the exact, case-sensitive key in `versionsFile`. The properties are loaded once per settings evaluation. |

### Library declarations

| API | Parameters | Contract |
|-----|------------|----------|
| `root(group) { ... }` | `group: String`, `content` | Creates a unique top-level Maven group value, conventionally `com`, `io`, `me`, or `org`. It becomes the first part of descendant coordinates and aliases. |
| `library(group) { ... }` | `group: String`, optional `content` | Creates or reuses a relative Maven group path. Dots create real nested nodes: `library("figma.code")` equals nested `figma` and `code` declarations. A node without entries is only a namespace. |
| `artifact(artifact, version)` | `artifact: String`, `version: String? = null` | Registers `<full-group>:<artifact>`. A null version calls Gradle's `withoutVersion()` and requires external version management. |
| `artifactsBundle(*artifacts, alias, version)` | artifact names, required bundle `alias`, optional shared `version` | Registers every artifact individually and creates `<librariesCatalogName>.bundles.<alias>` from their generated aliases. A null version makes every artifact versionless. |

Library aliases start with the complete group. When the artifact begins with a
suffix already represented by that group, the longest repeated prefix is
removed; hyphens in the remaining artifact part become dots.

| Maven coordinate | Default catalog alias/accessor |
|------------------|--------------------------------|
| `io.ktor:ktor-client-core` | `io.ktor.client.core` / `libs.io.ktor.client.core` |
| `androidx.compose:compose-bom` | `androidx.compose.bom` / `libs.androidx.compose.bom` |
| `com.google.protobuf:protoc` | `com.google.protobuf.protoc` / `libs.com.google.protobuf.protoc` |

### Plugin declarations

| API | Parameters | Contract |
|-----|------------|----------|
| `root(id) { ... }` | `id: String`, `content` | Creates a unique top-level plugin id value, conventionally `com` or `org`. |
| `plugin(id, version) { ... }` | `id: String`, `version: String? = null`, optional `content` | Creates or reuses a relative plugin id path. Dots create nested nodes. Only nodes with a version are registered; unversioned nodes are namespaces. |

A registered plugin uses its complete dotted path as both its Gradle plugin id
and catalog alias. For example, the following declaration registers
`org.jetbrains.kotlin.jvm` and exposes it through
`plugins.org.jetbrains.kotlin.jvm`:

```kotlin
plugins {
    root("org") {
        plugin("jetbrains.kotlin") {
            plugin(
                id = "jvm",
                version = version("kotlinVersion"),
            )
        }
    }
}
```

### Registration lifecycle

The plugin collects declarations while `settings.gradle.kts` is evaluated and
registers both catalogs automatically from `settingsEvaluated`. There is no
terminal `build`, `register`, or `from` call. Catalog names and `versionsFile`
must therefore be configured inside `dependencyCatalogTree` before settings
evaluation finishes.

### API documentation publication

Every `dependency-catalog` module applies Dokka. Its `check` task depends on
`dokkaGenerate`, documents public and internal declarations, reports
undocumented declarations, and fails the build on Dokka warnings. The included
build aggregate is:

```powershell
.\gradlew.bat :dependency-catalog:dokkaGenerate
```

The two Gradle plugin modules use the declared `org.jetbrains.kotlin.jvm`
version plus `gradleApi()`. They do not apply `kotlin-dsl`, because mixing its
embedded Kotlin version with the build's declared Kotlin plugin prevents Dokka
from loading one consistent Kotlin model. Plugin implementation code uses the
public Gradle API directly.

### IDE quick documentation

Dokka generates browsable HTML, but it does not attach KDoc to the Kotlin DSL
script classpath used by Android Studio. Both Gradle plugin modules therefore
publish a `sources` variant with `java.withSourcesJar()`. The variant makes the
Kotlin sources and their KDoc available to source-aware consumers; it does not
guarantee that Android Studio will associate them with Kotlin DSL script
dependencies.

After changing or upgrading either plugin, reload all Gradle projects so the IDE
refreshes the settings script classpath. If Quick Documentation still shows only
a decompiled signature such as `public final fun libraries(...)`, the IDE has
not associated that binary with its source path. This is a known limitation of
the current Kotlin DSL script model/import flow; adding or duplicating KDoc on
the declaration cannot repair the missing association. Use the generated Dokka
publication or navigate directly to the linked source until the IDE attaches
the source. JetBrains tracks improvements to this association in
[KTIJ-33975](https://youtrack.jetbrains.com/issue/KTIJ-33975).

## Invariants

- At least one library or plugin root must be declared.
- A library `root(group)` value is unique within one settings extension.
- A plugin `root(id)` value is unique within one settings extension.
- Relative `library` and `plugin` paths cannot be blank or contain empty or
  whitespace-padded segments.
- Repeated relative paths reuse existing prefix nodes and retain declaration
  order.
- A plugin path cannot be redeclared with a different non-null version.
- The canonical `versionsFile` path cannot change after the first `version`
  lookup.
- A missing versions file or invalid path produces `IllegalArgumentException`;
  a missing version key produces `IllegalStateException`.
- Artifact names, bundle aliases, catalog names, and duplicate generated aliases
  are ultimately validated by Gradle during catalog registration.
- All public and internal declarations must remain documented so strict Dokka
  generation can complete without warnings.

## Sources

- Settings extension: `catalog-tree-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/tree/TreeDependencyCatalogSettingsExtension.kt`.
- Library tree DSL: `catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/library/LibraryScope.kt`.
- Plugin tree DSL: `catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/plugin/PluginScope.kt`.
- Alias generation: `catalog-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/VersionCatalogBuilderExtension.kt`.
- Executable external-consumer example: `samples/standalone-tree-consumer/settings.gradle.kts`.
