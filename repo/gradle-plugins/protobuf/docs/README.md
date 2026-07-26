# Protobuf Gradle Convention Plugin

## Plugin

```kotlin
plugins {
    id("com.marmatsan.protobuf")
}
```

## Purpose

Configures shared Protocol Buffers generation for modules that own `.proto`
schemas.

Use this convention when a module should generate lite Kotlin and Java Protobuf
types.

## Behavior

- Applies `com.google.protobuf`.
- Configures `protoc` from the project dependency catalog.
- Configures all Protobuf generation tasks to create Kotlin lite outputs.
- Configures all Protobuf generation tasks to create Java lite outputs.
- Adds the `protobuf-kotlin` runtime dependency.

## Requirements

Water My Plants Protobuf versions and coordinates remain in
`repo/dependency-catalog`; the Gradle plugin version needed to compile this
included build is also declared locally in
`repo/gradle-plugins/versions.properties`.

The consuming module owns its `.proto` sources.

## Verification

For changes to this plugin, prefer focused gradle-plugins verification:

```powershell
.\gradlew.bat -p repo/gradle-plugins :protobuf:check
```
