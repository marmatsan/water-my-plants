# Dependency Catalog

`repo/dependency-catalog` is a reusable included build for describing and
registering Gradle version catalogs. It contains no Water My Plants dependency
declarations and can be built or published without any sibling source build.

## Modules

| Module | Responsibility |
|--------|----------------|
| `:catalog-api` | Immutable catalog model plus segregated resolved and version-aliased provider boundaries. |
| `:catalog-core` | Shared catalog-tree builder, version-resolution strategies, traversal, and mappers used by providers and settings adapters. |
| `:catalog-gradle-plugin` | Settings plugin that maps a provider's API model to Gradle version catalogs. Depends only on `:catalog-api`. |
| `:catalog-tree-gradle-plugin` | Settings-facing tree DSL plugin that reads a consumer-owned version registry and delegates registration to `:catalog-gradle-plugin`. |

The allowed dependency direction is:

```text
catalog-gradle-plugin -> catalog-api
catalog-core          (independent implementation toolkit)
catalog-tree-gradle-plugin -> catalog-core + catalog-api + catalog-gradle-plugin
```

The included-build root publishes no compatibility artifact. The four modules
publish `com.marmatsan.repo:catalog-api`, `catalog-core`,
`catalog-gradle-plugin`, and `catalog-tree-gradle-plugin`. Their plugin markers
expose `com.marmatsan.dependencyCatalog` and
`com.marmatsan.dependencyCatalog.tree` respectively.

`versions.properties` belongs only to this included build's compile/test
toolchain. Product dependency values do not live here.

## Consumer Contract

A consuming repository implements
`com.marmatsan.dependencies.catalog.api.ResolvedDependencyCatalogProvider`,
applies `com.marmatsan.dependencyCatalog`, and supplies the provider during
settings evaluation. A repository that also publishes version aliases for
documentation may implement the aggregate `DependencyCatalogProvider`:

```kotlin
plugins {
    id("com.marmatsan.dependencyCatalog") version "<version>"
}

dependencyCatalog {
    from(ExampleCatalogProvider())
}
```

The provider owns concrete version resolution. It may use the shared
`dependencyCatalogTrees(versionResolver) { ... }` builder from `catalog-core`,
its own implementation, or a different adapter; the Gradle plugin never
imports a product implementation. The shared builder lets one declaration
produce concrete Gradle versions through `PropertiesDependencyVersionResolver`
and symbolic documentation aliases through `DependencyVersionAliasResolver`.

Water My Plants keeps its implementation in
`repo/water-my-plants-project-config/catalog` and its product versions in
`repo/water-my-plants-project-config/versions.properties`.

Each autonomous included build declares only the dependencies it consumes in
its own `versions.properties` and local catalog. A dependency first used by
repository tooling, such as `kotlin-result` in Verification Platform and Figma
Documentation Sync, is not copied into the Water My Plants product catalog. It
enters that catalog only when production app source has a real consumer; until
then it is deliberately absent from both the generated product catalog and its
Figma dependency tree.

When autonomous builds must use one compatible tooling version, the root
`boundaries.alignedVersion` contract compares that property only across builds
that declare it. A build without that dependency omits the property instead of
copying an unused catalog entry solely for alignment.

See [the adoption guide](docs/guides/adopt-dependency-catalog.md) for the full
integration contract and the [tree settings DSL API reference](docs/reference/tree-settings-dsl.md)
for every public property and declaration operation.

`figma-documentation-sync`, `gradle-plugins`, `unit-testing`, and
`verification-platform` construct their local build catalogs with the tree
settings plugin and their own `versions.properties`. Water My Plants uses the
same core declaration syntax in its product provider because it also needs a
symbolic view for Figma. The repository composition root injects the
source-build location for local substitution; an independent checkout resolves
the same plugin id and version from its configured Maven repository. The
`dependency-catalog` producer keeps its own catalog manual to avoid a
self-hosting plugin-resolution cycle.

## Verification

```powershell
.\gradlew.bat checkDependencyCatalogArchitecture
.\gradlew.bat :dependency-catalog:dokkaGenerate
.\gradlew.bat verifyDependencyCatalogDistribution
```

`verifyDependencyCatalogDistribution` delegates to the included build's
`verifyStagedPublication`, which publishes the API, core, plugin, and plugin
marker to a temporary Maven repository, then applies them from
`samples/standalone-consumer` with no `includeBuild` or source dependency.

## Sources Of Truth

- Public provider/model API: `catalog-api/src/main/kotlin/`.
- Shared catalog-tree builder and optional tree DSL: `catalog-core/src/main/kotlin/`.
- Gradle settings adapter: `catalog-gradle-plugin/src/main/kotlin/`.
- Independent consumer proofs: `samples/standalone-consumer/` and
  `samples/standalone-tree-consumer/`.
- Build tool versions: `versions.properties`.
