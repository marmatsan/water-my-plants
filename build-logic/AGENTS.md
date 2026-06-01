# build-logic Agent Instructions

This directory contains Gradle convention plugins used by the rest of the project. Treat changes here as build infrastructure changes: small edits can affect every Android module.

## Scope

- Keep plugin behavior explicit and centralized in the existing plugin modules:
  - `android`: Android application/library defaults and shared Android dependencies.
  - `compose`: Jetpack Compose setup and shared Jetpack Compose dependencies.
  - `dependencies`: version catalog generation and dependency/plugin alias model.
  - `protobuf`: Protobuf Gradle plugin setup and lite runtime dependencies.
  - `unitTest`: JUnit 5 test configuration and shared test dependencies.
- Do not add product, UI, feature, or Android screen logic here.
- Do not edit generated Gradle outputs under `build/`, `.gradle/`, or `.kotlin/`.

## Dependency And Version Rules

- Keep version values in `versions.properties`.
- Library dependency trees are in `dependencies/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt`.
- Plugin dependency trees are in `dependencies/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt`.
- When adding a new version key, update `dependencies/src/main/kotlin/com/marmatsan/dependencies/Versions.kt`and the tree definitions that consume it.
- Prefer adding dependencies through the existing dependency tree helpers instead of hardcoding aliases across product modules.
- Keep build-logic's own catalog in `settings.gradle.kts` limited to dependencies needed to compile and test the convention plugins.

## Plugin Rules

- Preserve the existing plugin ID pattern: `com.marmatsan.<name>`.
- Keep implementation classes under `com.marmatsan.<name>.plugin`.
- Prefer Gradle typed APIs and Kotlin DSL helpers over strongly typed configuration when the API is available.
- Avoid `afterEvaluate` unless there is no stable lazy Gradle API for the behavior.
- Keep convention plugins idempotent and safe to apply to their intended project types.

## Android And Compose Conventions

- Shared Android defaults belong in `android/src/main/kotlin/com/marmatsan/android/plugin/AndroidPlugin.kt`.
- Shared Jetpack Compose setup belongs in `compose/src/main/kotlin/com/marmatsan/compose/plugin/ComposePlugin.kt`.
- If a dependency is required by every module applying a convention plugin, add it in that plugin. If only one feature needs it, keep it in that feature module.
- Be careful with SDK, Kotlin, JVM, and plugin version changes; they affect all consumers.

## Testing

- Tests must always use JUnit 5, AssertK, and MockK.
- These test dependencies are available through the build-logic version catalog declared in `settings.gradle.kts`.
- Structure every test with explicit `GIVEN`, `WHEN`, and `THEN` sections. These words are wrapped in a single-line comment.
- When creating or modifying tests, do not execute them unless the user explicitly asks for test execution.
