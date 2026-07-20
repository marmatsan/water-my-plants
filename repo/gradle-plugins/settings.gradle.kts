rootProject.name = "gradle-plugins"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

includeBuild("../dependency-catalog")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Versions owned by dependency-catalog and consumed by this included build.
val versions = java.util.Properties().apply {
    file("../dependency-catalog/versions.properties").inputStream().use(::load)
}

fun version(
    key: String
): String = versions.getProperty(key)
    ?: error("Missing version property '$key' in repo/dependency-catalog/versions.properties")

fun VersionCatalogBuilder.library(
    alias: String,
    group: String,
    artifact: String
) = library(
    alias,
    group,
    artifact
)

dependencyResolutionManagement {
    versionCatalogs {
        // gradle-plugins libraries
        create("libs") {
            /* Build */
            library(
                alias = "com.android.tools.build.gradle",
                group = "com.android.tools.build",
                artifact = "gradle"
            ).version(version("androidGradlePluginVersion"))

            library(
                alias = "com.google.protobuf.gradle.plugin",
                group = "com.google.protobuf",
                artifact = "protobuf-gradle-plugin"
            ).version(version("protobufPluginVersion"))

            library(
                alias = "org.jetbrains.kotlin.gradle.plugin",
                group = "org.jetbrains.kotlin",
                artifact = "kotlin-gradle-plugin"
            ).version(version("kotlinVersion"))

            library(
                alias = "org.jetbrains.dokka.gradle.plugin",
                group = "org.jetbrains.dokka",
                artifact = "dokka-gradle-plugin"
            ).version(version("dokkaPluginVersion"))
            
            /* Testing */
            // JUnit Platform
            library(
                alias = "org.junit.jupiter.platform.launcher",
                group = "org.junit.platform",
                artifact = "junit-platform-launcher"
            ).withoutVersion()

            // Kotest
            library(
                alias = "io.kotest.runner.junit5",
                group = "io.kotest",
                artifact = "kotest-runner-junit5"
            ).version(version("kotestLibraryVersion"))

            library(
                alias = "io.kotest.assertions.core",
                group = "io.kotest",
                artifact = "kotest-assertions-core"
            ).version(version("kotestLibraryVersion"))

            // MockK
            library(
                alias = "io.mockk",
                group = "io.mockk",
                artifact = "mockk"
            ).version(version("mockkLibraryVersion"))
        }
    }
}

include(
    ":android",
    ":bdd-test",
    ":compose",
    ":dependencies",
    ":dokka-documentation",
    ":protobuf",
    ":unit-test"
)
