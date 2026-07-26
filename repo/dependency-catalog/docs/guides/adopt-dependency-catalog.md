---
title: Adopt the reusable dependency catalog
type: guide
scope: repo/dependency-catalog
owner: dependency-catalog
status: active
last-reviewed: 2026-07-26
review-cycle-days: 180
sources:
  - repo/dependency-catalog/catalog-api/src/main/kotlin/com/marmatsan/dependencies/catalog/api/DependencyCatalogProvider.kt
  - repo/dependency-catalog/catalog-api/src/main/kotlin/com/marmatsan/dependencies/catalog/api/ResolvedDependencyCatalogProvider.kt
  - repo/dependency-catalog/catalog-api/src/main/kotlin/com/marmatsan/dependencies/catalog/api/VersionAliasedDependencyCatalogProvider.kt
  - repo/dependency-catalog/catalog-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/DependencyCatalogSettingsExtension.kt
  - repo/dependency-catalog/samples/standalone-consumer/settings.gradle.kts
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
   `DependencyCatalogProvider` combines both consumer-specific ports.
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

## Verification

Water My Plants exercises both the plugin-level TestKit contract and a Maven
consumer with no source includes:

```powershell
.\gradlew.bat -p repo\dependency-catalog :catalog-gradle-plugin:test
.\gradlew.bat -p repo\dependency-catalog verifyStagedPublication
```

The consumer repository should additionally verify that expected aliases exist
through `VersionCatalogsExtension` before enabling Figma publication.

## Related Documentation

- [Dependency Catalog README](../../README.md)
- [Figma Documentation Sync adoption guide](../../../figma-documentation-sync/docs/guides/adopting-figma-documentation-sync.md)
- [Figma Documentation Sync distribution contract](../../../figma-documentation-sync/docs/reference/distribution-contract.md)
