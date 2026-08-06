---
title: Tree settings dependency catalog DSL API
type: reference
scope: repo/dependency-catalog
owner: dependency-catalog
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - repo/dependency-catalog/build.gradle.kts
  - repo/dependency-catalog/settings.gradle.kts
  - repo/dependency-catalog/catalog-api/src/main/kotlin/com/marmatsan/dependencies/catalog/api/DependencyCatalog.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/dsl/DependencyCatalogTreesDsl.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/dsl/DependencyCatalogTreesBuilder.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/dsl/LibraryCatalogTreesScope.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/dsl/PluginCatalogTreesScope.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/definition/DependencyCatalogDefinition.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/definition/DependencyCatalogDefinitionProvider.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/mapping/DependencyCatalogApiMapping.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/version/DependencyVersionResolver.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/version/PropertiesDependencyVersionResolver.kt
  - repo/dependency-catalog/catalog-tree-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/tree/TreeDependencyCatalogSettingsExtension.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/library/LibraryScope.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/plugin/PluginScope.kt
  - repo/dependency-catalog/catalog-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/VersionCatalogBuilderExtension.kt
  - repo/dependency-catalog/samples/standalone-tree-consumer/settings.gradle.kts
---

# Tree Settings Dependency Catalog DSL API

## Purpose

This reference defines the shared catalog-tree builder and the exact public
settings DSL exposed by the `com.marmatsan.dependencyCatalog.tree` plugin,
including version strategies, defaults, declaration parameters, generated
coordinates and aliases, registration timing, and validation failures.

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
| `root(group) { ... }` | `group: String`, `content` | Creates a unique top-level Maven group value containing exactly one path segment, conventionally `com`, `io`, `me`, or `org`. It becomes the first part of descendant coordinates and aliases and may own artifacts directly for a single-segment Maven group. |
| `library(group) { ... }` | `group: String`, optional `content` | Creates or reuses a relative Maven group path. Dots create real nested nodes: below `root("org")`, `library("jetbrains.kotlinx")` equals nested `jetbrains` and `kotlinx` declarations. A node without entries is only a namespace. |
| `artifact(artifact, version)` | `artifact: String`, `version: String? = null` | Registers `<full-group>:<artifact>`. A null version calls Gradle's `withoutVersion()` and requires external version management. |
| `artifactsBundle(*artifacts, alias, version)` | artifact names, required `alias` ending in `Bundle`, optional shared `version` | Registers every artifact individually and creates `<librariesCatalogName>.bundles.<alias>` from their generated aliases. A null version makes every artifact versionless. The public catalog model rejects aliases without the `Bundle` suffix. |

Use `artifactsBundle` only when every artifact represents one cohesive dependency
set, shares version management, and is normally added to the same Gradle
configuration. Keep BOMs outside the bundle so consumers can add them through
`platform(...)`. Keep artifacts separate when they belong to different
configurations, such as a compiler on `ksp`, a runtime on `implementation`, or
a launcher on `testRuntimeOnly`.

Bundle aliases use lower camel case and the explicit `Bundle` suffix, for
example `composeBundle`, `lifecycleComposeBundle`, or `kotestBundle`. This keeps
bundle identity unambiguous in Gradle accessors, source scanners, and generated
documentation.

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
| `root(id, version) { ... }` | `id: String`, optional `version`, optional `content` | Creates a unique top-level plugin id containing exactly one path segment, conventionally `com` or `org`. A non-null version registers the root itself, which represents a single-segment plugin id. |
| `plugin(id, version) { ... }` | `id: String`, `version: String? = null`, optional `content` | Creates or reuses a relative plugin id path. Dots create nested nodes. Only nodes with a version are registered; unversioned nodes are namespaces. |

A registered plugin uses its complete dotted path as both its Gradle plugin id
and catalog alias. For example, the following declaration registers
`org.jetbrains.kotlin.jvm` and exposes it through
`plugins.org.jetbrains.kotlin.jvm`:

```kotlin
plugins {
    root("org") {
        plugin(
            id = "jetbrains.kotlin.jvm",
            version = version("kotlinVersion")
        )
    }
}
```

Maintained catalogs use the complete dotted path for every maximal linear
plugin chain. A namespace block is retained only at a branch point with
multiple plugin descendants; path resolution still creates one model node per
segment.

A versioned plugin whose complete id has one segment is represented by a
versioned root, without inventing an additional namespace node:

```kotlin
plugins {
    root(
        id = "quality",
        version = version("qualityPluginVersion")
    )
}
```

### Shared core builder

Provider-owned product catalogs use the same declaration hierarchy without
depending on Gradle Settings:

```kotlin
private fun productCatalog(
    versionResolver: DependencyVersionResolver
): DependencyCatalogTrees =
    dependencyCatalogTrees(
        versionResolver = versionResolver
    ) {
        libraries {
            root("io") {
                library("ktor") {
                    artifact(
                        artifact = "ktor-client-core",
                        version = version("ktorLibraryVersion")
                    )
                }
            }
        }

        plugins {
            root("org") {
                plugin(
                    id = "jetbrains.kotlin.jvm",
                    version = version("kotlinVersion")
                )
            }
        }
    }
```

`dependencyCatalogTrees` creates a single-use
`DependencyCatalogTreesBuilder`. Its `libraries`, `plugins`, `root`, `library`,
`artifact`, `artifactsBundle`, and `plugin` operations have the same contracts
as the settings extension. `version(key)` delegates to the injected
`DependencyVersionResolver`:

- `PropertiesDependencyVersionResolver` returns concrete values from one
  consumer-owned properties file and fixes its canonical path at first use;
- `DependencyVersionAliasResolver` returns the key unchanged so documentation
  adapters can retain stable version ownership;
- a consumer may supply another focused resolver without changing the tree
  declaration.

The builder fails when no root was declared and cannot be changed or built a
second time after `build()` returns. The settings adapter delegates collection
and those invariants to this builder; it owns only Gradle properties and
registration timing.

`DependencyCatalogTrees.toDependencyCatalog()` is the canonical mapping from
the tree implementation model to the stable `catalog-api` model. It preserves
root and child order, namespace nodes, artifacts, bundles, and version text.
Provider implementations and the Settings adapter use this same mapping rather
than maintaining consumer-specific conversions.

### Reusable catalog definitions

`dependencyCatalogDefinition { ... }` captures the library and plugin tree
without selecting how version keys are represented. The returned
`DependencyCatalogDefinition` is reusable: `catalog(versionResolver)`
materializes a new immutable public `DependencyCatalog` for each supplied
`DependencyVersionResolver`.

`DependencyCatalogDefinitionProvider` adapts one definition to the segregated
provider contract used by consumers:

- `resolved(rootDir)` reads concrete values through
  `PropertiesDependencyVersionResolver`. By default the version registry is
  `<rootDir>/versions.properties`; callers may inject another file resolver.
- `withVersionAliases()` uses `DependencyVersionAliasResolver`, so every
  `version("key")` call retains `key` as its value for documentation and other
  symbolic consumers.

The definition is the single source of truth for roots, nodes, artifacts,
bundles, and plugins. The provider owns only the choice of version strategy and
the location of the consumer-owned registry. Product composition roots remain
responsible for declaring their catalog and passing the correct root directory;
`dependency-catalog` does not contain product coordinates or product paths.

```kotlin
val catalogDefinition =
    dependencyCatalogDefinition {
        libraries {
            root("io") {
                library("ktor") {
                    artifact(
                        artifact = "ktor-client-core",
                        version = version("ktorLibraryVersion")
                    )
                }
            }
        }
    }

val provider =
    DependencyCatalogDefinitionProvider(
        definition = catalogDefinition
    )
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
- A `DependencyCatalogTreesBuilder` is single-use after `build()`.
- The only library and plugin declarations available at catalog top level are
  `root`; leaves cannot exist outside a root.
- A library `root(group)` value is unique within one settings extension.
- A plugin `root(id)` value is unique within one settings extension.
- Every root and stored catalog node contains exactly one non-blank path
  segment without dots or whitespace. Dotted relative declarations are parsed
  before storage and create one node per segment.
- Relative `library` and `plugin` paths cannot be blank or contain empty or
  whitespace-padded segments.
- Repeated relative paths reuse existing prefix nodes and retain declaration
  order.
- A plugin path cannot be redeclared with a different non-null version.
- The canonical `versionsFile` path cannot change after the first `version`
  lookup.
- A missing versions file or invalid path produces `IllegalArgumentException`;
  a missing version key produces `IllegalStateException`.
- Library aliases are derived from their Maven group and artifact. When a
  migrated manual alias disagrees with that coordinate-derived name, update the
  consumer accessor instead of adding a repository-specific alias exception;
  for example, `org.junit.platform:junit-platform-launcher` becomes
  `org.junit.platform.launcher`.
- Artifact names, bundle aliases, catalog names, and duplicate generated aliases
  are ultimately validated by Gradle during catalog registration.
- All public and internal declarations must remain documented so strict Dokka
  generation can complete without warnings.

## Sources

- Settings extension: `catalog-tree-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/tree/TreeDependencyCatalogSettingsExtension.kt`.
- Shared catalog builder: `catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/dsl/DependencyCatalogTreesDsl.kt`.
- Public API mapping: `catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/mapping/DependencyCatalogApiMapping.kt`.
- Reusable definitions and provider adapter: `catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/definition/`.
- Version strategies: `catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/version/`.
- Library tree DSL: `catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/library/LibraryScope.kt`.
- Plugin tree DSL: `catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/plugin/PluginScope.kt`.
- Alias generation: `catalog-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/VersionCatalogBuilderExtension.kt`.
- Executable external-consumer example: `samples/standalone-tree-consumer/settings.gradle.kts`.
