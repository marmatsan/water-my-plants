# Dependency Catalog

`repo/dependency-catalog` is a reusable included build for describing and
registering Gradle version catalogs. It contains no Water My Plants dependency
declarations and can be built or published without any sibling source build.

## Modules

| Module | Responsibility |
|--------|----------------|
| `:catalog-api` | Immutable catalog model plus segregated resolved and version-aliased provider boundaries. |
| `:catalog-core` | Optional tree DSL, traversal, and mappers used to implement providers. |
| `:catalog-gradle-plugin` | Settings plugin that maps a provider's API model to Gradle version catalogs. Depends only on `:catalog-api`. |

The allowed dependency direction is:

```text
catalog-gradle-plugin -> catalog-api
catalog-core          (independent implementation toolkit)
```

The included-build root publishes no compatibility artifact. The three modules
publish `com.marmatsan.repo:catalog-api`, `catalog-core`, and
`catalog-gradle-plugin`; the plugin marker exposes
`com.marmatsan.dependencyCatalog`.

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

The provider owns concrete version resolution. It may use `catalog-core`, its
own implementation, or a different adapter; the Gradle plugin never imports a
product implementation.

Water My Plants keeps its implementation in
`repo/water-my-plants-project-config/catalog` and its product versions in
`repo/water-my-plants-project-config/versions.properties`.

Each autonomous included build declares only the dependencies it consumes in
its own `versions.properties` and local catalog. A dependency first used by
repository tooling, such as `kotlin-result` in `verification-platform`, is not
copied into the Water My Plants product catalog. It enters that catalog only
when production app source has a real consumer; until then it is deliberately
absent from both the generated product catalog and its Figma dependency tree.

See [the adoption guide](docs/guides/adopt-dependency-catalog.md) for the full
integration contract.

## Verification

```powershell
.\gradlew.bat checkDependencyCatalogArchitecture
.\gradlew.bat verifyDependencyCatalogDistribution
```

`verifyDependencyCatalogDistribution` delegates to the included build's
`verifyStagedPublication`, which publishes the API, core, plugin, and plugin
marker to a temporary Maven repository, then applies them from
`samples/standalone-consumer` with no `includeBuild` or source dependency.

## Sources Of Truth

- Public provider/model API: `catalog-api/src/main/kotlin/`.
- Optional tree DSL: `catalog-core/src/main/kotlin/`.
- Gradle settings adapter: `catalog-gradle-plugin/src/main/kotlin/`.
- Independent consumer proof: `samples/standalone-consumer/`.
- Build tool versions: `versions.properties`.
