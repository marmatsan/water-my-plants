# Gradle Plugins

This autonomous included build owns the reusable Gradle convention plugins.
It contains no Water My Plants product configuration and can publish every
plugin marker to a Maven repository without hard-coding a sibling source path.
See the [documentation index](docs/README.md) for its boundaries and shared
build-policy contract.

| Module | Plugin ID |
|--------|-----------|
| `android` | `com.marmatsan.android` |
| `bdd-test` | `com.marmatsan.bddTest` |
| `compose` | `com.marmatsan.compose` |
| `dokka-documentation` | `com.marmatsan.dokkaDocumentation` |
| `protobuf` | `com.marmatsan.protobuf` |
| `unit-test` | `com.marmatsan.unitTest` |

The `dependencies` module is an implementation library shared by convention
plugins. It consumes the stable `com.marmatsan.repo:catalog-api` alias policy
instead of duplicating catalog identity rules. The typed test API is deliberately separate in `repo/unit-testing`;
`unit-test` consumes `com.marmatsan.repo:unit-test-dsl` through the consumer's
`testLibs` catalog, while Kotest, MockK, and JUnit remain in `libs`.

`versions.properties` owns the compile, test, and publication versions for
this build. Build scripts use type-safe `libs` and `plugins` accessors. A
plugin module declares a test engine and test libraries only when it owns test
sources; source-free plugin modules rely on compilation and standalone
distribution verification instead of carrying an unused test classpath.

Every plugin marker published by this build belongs to the coordinated
`gradlePluginsVersion` release train. The modules are verified and staged as
one distribution, so consumers use that shared version instead of implying
independent release boundaries for individual convention plugins. Each
consumer still owns the matching key in its local `versions.properties`; root
verification checks alignment without coupling either build to the other's
file.

## Verification

```powershell
.\gradlew.bat -p repo/gradle-plugins check verifyStagedPublication
```

`verifyStagedPublication` publishes every marker and implementation to an
isolated staging repository, then applies them from
`samples/standalone-consumer` without `includeBuild`. Development composition
may provide `dependencyCatalogSourceBuild`; an external invocation instead
provides `dependencyCatalogPublicationRepository` containing the matching
`catalog-api` version. The source-build form publishes that API into an
isolated repository before launching the source-independent fixture.
