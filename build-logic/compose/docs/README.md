# Compose Gradle Convention Plugin

## Plugin

```kotlin
plugins {
    id("com.marmatsan.compose")
}
```

## Purpose

Configures shared Jetpack Compose defaults for Android application and library
modules.

Use this convention when an Android module renders UI with Compose.

## Behavior

- Detects whether the target project applies the Android application plugin.
- Enables Compose build features for app and library modules.
- Enables support-library vector drawables.
- Applies `org.jetbrains.kotlin.plugin.compose`.
- Applies `com.figma.code.connect` only when the
  `figmaCodeConnectEnabled=true` Gradle property is set.
- Adds Compose BOM-managed dependencies, Material 3, material icons, lifecycle
  Compose integrations, Navigation Compose, Activity Compose, and Figma Code
  Connect runtime dependencies.

## Requirements

The consuming project must already be an Android application or Android library
project before this convention is applied.

Use `figmaCodeConnectEnabled` only when running Code Connect workflows.

## Verification

For changes to this plugin, prefer focused build-logic verification:

```powershell
.\gradlew.bat -p build-logic :compose:check
```
