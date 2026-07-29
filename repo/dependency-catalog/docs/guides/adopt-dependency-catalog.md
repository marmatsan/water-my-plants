---
title: Adopt the reusable dependency catalog
type: guide
scope: repo/dependency-catalog
owner: dependency-catalog
status: active
last-reviewed: 2026-07-29
review-cycle-days: 180
sources:
  - repo/dependency-catalog/catalog-api/src/main/kotlin/com/marmatsan/dependencies/catalog/api/DependencyCatalog.kt
  - repo/dependency-catalog/catalog-api/src/main/kotlin/com/marmatsan/dependencies/catalog/api/DependencyCatalogProvider.kt
  - repo/dependency-catalog/catalog-api/src/main/kotlin/com/marmatsan/dependencies/catalog/api/ResolvedDependencyCatalogProvider.kt
  - repo/dependency-catalog/catalog-api/src/main/kotlin/com/marmatsan/dependencies/catalog/api/VersionAliasedDependencyCatalogProvider.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/dsl/DependencyCatalogTreesDsl.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/version/DependencyVersionResolver.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/version/PropertiesDependencyVersionResolver.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/library/LibraryScope.kt
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/plugin/PluginScope.kt
  - repo/dependency-catalog/catalog-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/DependencyCatalogSettingsExtension.kt
  - repo/dependency-catalog/catalog-tree-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/tree/TreeDependencyCatalogSettingsExtension.kt
  - repo/dependency-catalog/samples/standalone-consumer/settings.gradle.kts
  - repo/dependency-catalog/samples/standalone-tree-consumer/settings.gradle.kts
---

# Adopt the Reusable Dependency Catalog

## Outcome

A Gradle repository owns its dependency declarations and version registry while
reusing the published catalog tree DSL and settings plugin. It does not include
the Water My Plants source build and does not create a catalog module for every
included build.

## Applicable Standards

- Follow [ADR-0009](../../../../docs/decisions/adr-0009-separate-product-catalog-from-build-tool-versions.md).
- Keep each independently evaluated build's compile/test versions in that
  build's local `versions.properties`.

## Steps

1. Add the release Maven repository to `pluginManagement` and apply
   `com.marmatsan.dependencyCatalog` at the selected release version.
2. Add a repository-owned `versions.properties`. This file contains the
   versions for the product catalog owned by that build; an included build that
   owns only tooling versions needs no product catalog provider.
3. Implement `ResolvedDependencyCatalogProvider`. Its `resolved(rootDir)`
   operation loads concrete values from the repository-owned file. If the same
   repository also needs documentation aliases, implement
   `VersionAliasedDependencyCatalogProvider`; the aggregate
   `DependencyCatalogProvider` combines both consumer-specific ports. Every
   `LibraryCatalogNode.group` and `PluginCatalogNode.id` stores exactly one
   segment; represent complete coordinates through nested root nodes rather
   than storing dots in a node value.
4. Register the provider during settings evaluation. Configure custom catalog
   names before the terminal `from` call when `libs` and `plugins` are not
   appropriate:

   ```kotlin
   plugins {
       id("com.marmatsan.dependencyCatalog")
   }

   dependencyCatalog {
       librariesCatalogName.set("libs")
       pluginsCatalogName.set("plugins")
       from(ExampleCatalogProvider())
   }
   ```

5. If the repository also adopts Figma Documentation Sync, create a product
   adapter from `VersionAliasedDependencyCatalogProvider` to Figma's
   `DependencyDslCatalogProvider`, then configure
   `dependencyCatalogProviderClassName` and `versionsFile`. Define visual
   catalog targets only for trees used to produce that repository's product.
   Included builds may still contribute modules and usage metadata while
   `publishesCatalogs` remains `false`.
6. Add the catalog architecture, local version ownership, and standalone staged
   consumer tasks to the repository's existing authoritative CI gate. Do not
   create an additional required GitHub status only for this subsystem.

## Provider-Owned Shared Tree Declaration

A product provider that needs concrete Gradle versions and stable documentation
aliases should declare its trees once in `catalog-core` and inject only the
version strategy:

```kotlin
private fun exampleCatalog(
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

fun resolvedCatalog(
    versionsFile: File
): DependencyCatalogTrees =
    exampleCatalog(
        versionResolver = PropertiesDependencyVersionResolver(versionsFile)
    )

fun documentationCatalog(): DependencyCatalogTrees =
    exampleCatalog(
        versionResolver = DependencyVersionAliasResolver
    )
```

Map the resulting `DependencyCatalogTrees` to the public provider API at the
consumer boundary. This keeps product ownership in the consumer, prevents the
reusable settings plugin from importing a product, and prevents separate
library/plugin declarations from drifting between Gradle and documentation.

## Settings-Owned Tree Adapter

An included build that only needs a local compile/test catalog does not need to
implement or publish a provider. Apply the versioned tree adapter and declare
its roots directly during settings evaluation:

```kotlin
pluginManagement {
    repositories {
        maven { url = uri("https://example.invalid/releases") }
        gradlePluginPortal()
    }
    plugins {
        id("com.marmatsan.dependencyCatalog.tree") version "<version>"
    }
}

plugins {
    id("com.marmatsan.dependencyCatalog.tree")
}

dependencyCatalogTree {
    versionsFile.set(file("versions.properties"))

    libraries {
        root("io") {
            library("ktor") {
                artifact(
                    artifact = "ktor-bom",
                    version = version("ktorLibraryVersion")
                )
                artifact(artifact = "ktor-client-core")
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

Use explicit `root` blocks for a maintained repository catalog. They expose the
same hierarchy that `libraryTree` and `pluginTree` model and keep related
artifacts or plugin leaves together. The top-level scopes intentionally expose
only `root`; compact paths remain available through relative `library` and
`plugin` declarations inside that root.

For source-composite development, the consuming settings may accept the
optional `dependencyCatalogSourceBuild` Gradle property and pass it to
`pluginManagement.includeBuild`. The composition root supplies the concrete
absolute path; the reusable build must not embed a sibling path. When the
property is absent, plugin resolution uses the configured publication
repository.

The exact defaults, parameters, generated aliases, lifecycle, and failure
conditions of this DSL are defined in the
[tree settings DSL API reference](../reference/tree-settings-dsl.md).

## Compact Tree Paths

The optional `catalog-core` DSL accepts paths relative to the current tree node.
A dot separates real hierarchy segments; it is not stored as part of one node.
The following library declaration therefore creates the nodes `figma`, `code`,
and `connect` below the `com` root:

```kotlin
libraryTree(rootGroup = "com") {
    library("figma.code.connect") {
        artifact(
            artifact = "code-connect-lib",
            version = versions.figmaCodeConnectLibraryVersion
        )
    }
}
```

Plugin declarations collapse every linear namespace chain, including the
versioned terminal. A block remains only where a node owns multiple plugin
descendants:

```kotlin
pluginTree(rootId = "com") {
    plugin("android") {
        plugin(
            id = "application",
            version = versions.androidGradlePluginVersion
        )
        plugin(
            id = "library",
            version = versions.androidGradlePluginVersion
        )
    }

    plugin(
        id = "figma.code.connect",
        version = versions.figmaCodeConnectPluginVersion
    )

    plugin("google") {
        plugin(
            id = "devtools.ksp",
            version = versions.kspPluginVersion
        )
        plugin(
            id = "protobuf",
            version = versions.protobufPluginVersion
        )
    }
}
```

Repeated declarations reuse existing path prefixes and preserve declaration
order. Paths must not be blank or contain empty or whitespace-padded segments.
A plugin terminal may be enriched later with nested declarations, but declaring
that same terminal with a different version fails immediately.

## Verification

Water My Plants exercises both the plugin-level TestKit contract and a Maven
consumer with no source includes:

```powershell
.\gradlew.bat -p repo\dependency-catalog :catalog-gradle-plugin:test
.\gradlew.bat -p repo\dependency-catalog :catalog-tree-gradle-plugin:test
.\gradlew.bat -p repo\dependency-catalog verifyStagedPublication
```

The consumer repository should additionally verify that expected aliases exist
through `VersionCatalogsExtension` before enabling Figma publication.

## Related Documentation

- [Dependency Catalog README](../../README.md)
- [Tree settings DSL API reference](../reference/tree-settings-dsl.md)
- [Figma Documentation Sync adoption guide](../../../figma-documentation-sync/docs/guides/adopting-figma-documentation-sync.md)
- [Figma Documentation Sync distribution contract](../../../figma-documentation-sync/docs/reference/distribution-contract.md)
