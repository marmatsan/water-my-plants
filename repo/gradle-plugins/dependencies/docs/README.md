# Version Catalog Helpers

## Purpose

The `:dependencies` module contains reusable helpers used by convention
plugins to read aliases from version catalogs already supplied by a consuming
settings build. It is not a settings plugin and does not select a product
catalog.

## Boundary

- Product settings apply `com.marmatsan.dependencyCatalog` through their own
  composition adapter.
- Convention plugins depend on this module's helper API and Gradle's
  `VersionCatalogsExtension`; the module delegates coordinate-to-alias identity
  to the stable `com.marmatsan.repo:catalog-api` contract.
- This module must not import `WaterMyPlantsCatalog`, catalog implementation
  classes, or sibling included builds.
- Product versions and trees live under
  `repo/water-my-plants-project-config`; this build's compile/test versions
  live in `repo/gradle-plugins/versions.properties`.

## Verification

```powershell
.\gradlew.bat -p repo\gradle-plugins :dependencies:check
.\gradlew.bat checkModuleBoundaries
```
