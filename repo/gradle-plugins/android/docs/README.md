# Android Gradle Convention Plugin

## Plugin

```kotlin
plugins {
    id("com.marmatsan.android")
}
```

## Purpose

Configures shared Android defaults for application and library modules.

Use this convention when a module is an Android module and should share the
project's SDK, Kotlin, JVM, dependency injection, coroutine, and Android core
defaults.

## Behavior

- Detects whether the target project applies the Android application plugin.
- Configures `ApplicationExtension` for app modules and `LibraryExtension` for
  library modules.
- Sets namespace from the Gradle project name.
- Sets compile, min, and target SDK versions.
- Sets Java compatibility to Java 21.
- Sets Kotlin language and JVM target settings.
- Applies `com.google.devtools.ksp`.
- Applies `de.mannodermaus.android-junit5`.
- Adds shared Android core, lifecycle, kotlin-inject, and Android coroutine
  dependencies through the project dependency catalog helpers.

## Requirements

The consuming project must already be an Android application or Android library
project before this convention is applied.

Water My Plants dependency versions belong in
`repo/water-my-plants-project-config/versions.properties`; versions used to compile and
test this included build belong in `repo/gradle-plugins/versions.properties`.
Dependency tree definitions belong in `repo/water-my-plants-project-config/catalog`.

## Verification

For changes to this plugin, prefer focused gradle-plugins verification:

```powershell
.\gradlew.bat -p repo/gradle-plugins :android:check
```
