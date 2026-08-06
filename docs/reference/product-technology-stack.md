---
title: Product technology stack
type: reference
scope: product-modules
owner: android
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - versions.properties
  - settings.gradle.kts
  - app/build.gradle.kts
  - repo/gradle-plugins/android
  - repo/gradle-plugins/compose
  - repo/gradle-plugins/unit-test
  - docs/decisions/adr-0011-standardize-typed-errors-with-kotlin-result.md
  - docs/decisions/adr-0015-use-room-and-proto-datastore-for-product-persistence.md
  - docs/decisions/adr-0016-use-ktor-and-kotlin-serialization-for-product-apis.md
  - docs/standards/api-client.md
  - docs/standards/architecture.md
---

# Product Technology Stack

## Purpose

Record the exact technology selections available to Water My Plants product
modules, their activation state, and the decisions that remain intentionally
open. Repository infrastructure under `repo/`, TeamCity, and Figma publication
technology are outside this product-runtime inventory.

## Selection Status

- **Active foundation:** applied by a product convention plugin or consumed by
  checked-in product source.
- **Consumer-gated:** selected or cataloged, but added to a product module only
  when that module has the first real consumer.
- **Unselected:** no implementation is authorized. The first selection follows
  the ADR or implementation workflow named by the owning standard.

Catalog presence alone does not prove product usage. `settings.gradle.kts`
defines available aliases, while module build files and convention plugins
define active consumption.

## Contract

### Active Foundations

| Capability                | Technology                               | Canonical version or configuration                                      | Activation and ownership                                                                                                                    |
|---------------------------|------------------------------------------|-------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| Android build             | Android Gradle Plugin                    | `androidGradlePluginVersion`                                            | Product Android modules apply `com.marmatsan.android`; the convention owns SDK and Java 21 configuration.                                   |
| Language                  | Kotlin                                   | `kotlinVersion`; language and JVM targets in the Android convention     | Kotlin is the implementation language for product modules.                                                                                  |
| Dependency injection      | kotlin-inject with KSP                   | `kotlinInjectLibraryVersion`, `kspPluginVersion`                        | The Android convention supplies the compiler and runtime. `:app` owns the product composition root.                                         |
| Concurrency               | Kotlin coroutines and Flow               | `androidCoroutinesLibraryVersion`                                       | The Android convention supplies coroutine support; structured concurrency and lifecycle rules remain mandatory.                             |
| UI                        | Jetpack Compose and Material 3           | `composeBomLibraryVersion`, `activityComposeLibraryVersion`             | `com.marmatsan.compose` enables Compose and supplies BOM-managed UI dependencies. Shared tokens live in `:core:ui`.                         |
| Lifecycle                 | AndroidX Lifecycle and ViewModel Compose | `lifecycleLibraryVersion`                                               | State owners expose read-only observable state and routes collect it with lifecycle awareness.                                              |
| Navigation foundation     | Navigation Compose                       | `navigationComposeLibraryVersion`                                       | The Compose convention supplies the library. `:app` owns the navigation graph; feature screens expose callbacks instead of `NavController`. |
| Design binding            | Figma Code Connect                       | `figmaCodeConnectLibraryVersion`, `figmaCodeConnectPluginVersion`       | The runtime is available to Compose modules; the plugin activates only with `figmaCodeConnectEnabled=true`.                                 |
| Unit testing              | Kotest, MockK, and JUnit Platform        | `kotestLibraryVersion`, `mockkLibraryVersion`; `com.marmatsan.unitTest` | Kotest owns assertions and specifications, MockK owns external collaborator doubles, and JUnit Platform executes tests.                     |
| Business behavior testing | Cucumber on JUnit Platform               | `cucumberLibraryVersion`; `com.marmatsan.bddTest`                       | Reserved for stable business behavior and cross-boundary living documentation.                                                              |

### Consumer-Gated Technologies

| Capability                  | Technology                                   | Activation contract                                                                                                                                                                                            |
|-----------------------------|----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Typed recoverable failures  | `com.michael-bull.kotlin-result` 2.3.1       | Approved by ADR-0011. Add it to the product catalog with the first production consumer and only to modules whose code uses it.                                                                                 |
| Relational persistence      | Room                                         | Selected by ADR-0015 for plants, watering records, and other relational data. Add Room runtime and compiler aliases with the first database consumer, use KSP, and keep entities and DAOs in the data adapter. |
| Typed settings persistence  | AndroidX Proto DataStore                     | Selected by ADR-0015 for small typed settings. Add DataStore aliases with the first settings consumer; one owned Protocol Buffers schema backs each store.                                                     |
| Remote API                  | Ktor Client                                  | Selected by ADR-0016. Add product-owned client and engine aliases with the first endpoint and configure one client at the transport composition boundary.                                                      |
| JSON serialization          | Kotlin Serialization                         | Selected by ADR-0016 for Ktor request and response DTOs. Apply the plugin and JSON library only to consuming modules and keep DTOs inside the transport adapter.                                               |
| Type-safe navigation routes | Kotlin Serialization with Navigation Compose | Apply the serialization plugin and library with the first serializable route. Route types carry stable arguments; the graph remains in `:app`.                                                                 |
| Typed persistence schemas   | Protocol Buffers                             | `protobufLibraryVersion` and `protobufPluginVersion` are cataloged. The first approved use is Proto DataStore; generated messages remain adapter types.                                                        |
| Application startup         | AndroidX SplashScreen                        | Add the product alias when `:app` implements startup. The application shell owns installation and keeps the splash visible only for observable initialization that gates the first destination.                |

### Unselected Product Technologies

| Capability            | Current contract                                                                                                                                                            |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Image loading         | No image loading library is selected. Choose one with the first real plant-image source and verify caching, cancellation, errors, and Compose accessibility.                |
| MVI implementation    | No external MVI framework is selected. Build the documented unidirectional presentation contract from Compose, Lifecycle ViewModel, StateFlow, coroutines, and saved state. |
| Background scheduling | No scheduler is selected. Select Android scheduling or notification technology only with the first watering-reminder contract.                                              |

## Invariants

- Root `versions.properties` owns Water My Plants product version values;
  `settings.gradle.kts` owns their library and plugin coordinates.
- Product modules consume dependencies through generated catalogs and
  repository convention plugins, never hard-coded coordinates or versions.
- A module adds a consumer-gated dependency only when checked-in production
  source uses it. Catalog availability is not permission to add unused runtime
  dependencies.
- Infrastructure, serialization, persistence, navigation, and Compose types do
  not become domain models. Adapters map them at an explicit boundary.
- A technology selection that requires an ADR remains unselected until that ADR
  is accepted. A guide, specification, dependency alias, or legacy implementation
  cannot substitute for the decision.
- Adding, replacing, or retiring a product technology updates this reference,
  the owning standard or ADR, its catalog declaration, and affected module
  documentation in the same change.

## Sources

- [`versions.properties`](../../versions.properties)
- [`settings.gradle.kts`](../../settings.gradle.kts)
- [Product architecture standard](../standards/architecture.md)
- [Compose standard](../standards/compose.md)
- [API client standard](../standards/api-client.md)
- [Testing standard](../standards/testing.md)
- [Typed error handling standard](../standards/error-handling.md)
- [ADR-0011](../decisions/adr-0011-standardize-typed-errors-with-kotlin-result.md)
- [ADR-0015](../decisions/adr-0015-use-room-and-proto-datastore-for-product-persistence.md)
- [ADR-0016](../decisions/adr-0016-use-ktor-and-kotlin-serialization-for-product-apis.md)
- [`com.marmatsan.android`](../../repo/gradle-plugins/android/docs/README.md)
- [`com.marmatsan.compose`](../../repo/gradle-plugins/compose/docs/README.md)
- [`com.marmatsan.unitTest`](../../repo/gradle-plugins/unit-test/docs/README.md)
