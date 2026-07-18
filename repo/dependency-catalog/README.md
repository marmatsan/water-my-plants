# Dependency Catalog

`dependency-catalog` is the repository included build that owns the reusable
catalog tree engine and the concrete Water My Plants dependency catalog.

## Modules

| Module | Responsibility |
|--------|----------------|
| `:catalog-core` | Reusable tree model, DSL, traversal, and dependency mappers. It contains no Water My Plants dependency declarations. |
| `:water-my-plants-catalog` | Concrete library and plugin trees, version schema, and the `WaterMyPlantsCatalog` facade. |

The dependency direction is one-way:

```text
water-my-plants-catalog -> catalog-core
```

The included-build root is an organizational parent and does not publish a
compatibility artifact.

## Public Contract

The Water My Plants project adapter consumes:

```kotlin
WaterMyPlantsCatalog.resolved(rootDir)
WaterMyPlantsCatalog.withVersionAliases()
```

Both functions return `DependencyCatalogTrees`. Concrete `Versions`,
`libraryTrees`, and `pluginTrees` declarations are internal implementation
details of `:water-my-plants-catalog`.

Portable consumers declare only `catalog-core` and depend on an adapter
contract. `figma-design-sync:data` defines `DependencyCatalogProvider`; its
Water My Plants implementation lives in
`repo/figma-design-sync/project-config` and is the only Figma sync production
module that depends on `water-my-plants-catalog`.

The resulting dependency direction is:

```text
figma-design-sync:data -> catalog-core
figma-design-sync:project-config -> figma-design-sync:data
figma-design-sync:project-config -> water-my-plants-catalog -> catalog-core
```

Repository-specific consumers declare both stable coordinates only when they
need the concrete facade:

```kotlin
implementation("com.marmatsan.repo:catalog-core")
implementation("com.marmatsan.repo:water-my-plants-catalog")
```

## Sources Of Truth

- Version values: `versions.properties`.
- Library declarations: `water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt`.
- Plugin declarations: `water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt`.
- Reusable DSL: `catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/`.
