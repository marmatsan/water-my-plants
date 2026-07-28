# Gradle Plugins Documentation

## Purpose

This directory is the documentation entry point for the autonomous included
build that publishes the reusable Gradle convention plugins.

## Boundaries

The included-build root owns publication coordinates and policies shared by
all plugin modules. Each plugin module owns its plugin implementation,
capability-specific dependencies, and tests.

A module declares Kotest, MockK, and the JUnit launcher only when it contains
test sources. Modules without tests rely on compilation, plugin validation,
and the standalone distribution fixture instead of carrying an unused test
classpath.

## Dependencies

The `dependencies` implementation library provides shared convention-plugin
behavior through the published catalog API. The included build may resolve
that API from an explicit development source build, while the standalone
distribution consumes only staged Maven artifacts.

## Shared Standards

- [Architecture standard](../../../docs/standards/architecture.md)
- [Testing standard](../../../docs/standards/testing.md)
- [Documentation standard](../../../docs/documentation.md)

## Verification

Run the included-build checks and apply every staged plugin from the isolated
consumer fixture:

```powershell
.\gradlew.bat -p repo/gradle-plugins check verifyStagedPublication
```

## Module Documentation

- [Android convention plugin](../android/docs/README.md)
- [BDD test convention plugin](../bdd-test/docs/README.md)
- [Compose convention plugin](../compose/docs/README.md)
- [Shared dependency helpers](../dependencies/docs/README.md)
- [Dokka convention plugin](../dokka-documentation/docs/README.md)
- [Protobuf convention plugin](../protobuf/docs/README.md)
- [Unit-test convention plugin](../unit-test/docs/README.md)
