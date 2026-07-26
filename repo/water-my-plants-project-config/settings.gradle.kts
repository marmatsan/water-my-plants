@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    val versions =
        java.util.Properties().apply {
            file("versions.properties").inputStream().use(::load)
        }

    plugins {
        id("org.jetbrains.kotlin.jvm") version versions.getProperty("kotlinVersion")
    }
}

// This product-only composition build is the authorized owner of sibling
// build wiring. Reusable included builds never include one another.
includeBuild("../dependency-catalog")
includeBuild("../figma-documentation-sync")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        providers.gradleProperty("figmaDocumentationSyncPublicationRepository").orNull?.let { repository ->
            maven { url = uri(repository) }
        }
        providers.gradleProperty("figmaDocumentationSyncCatalogPublicationRepository").orNull?.let { repository ->
            maven { url = uri(repository) }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    val versions =
        java.util.Properties().apply {
            file("versions.properties").inputStream().use(::load)
        }

    versionCatalogs {
        create("libs") {
            library(
                "org.jetbrains.kotlinx.serialization.json",
                "org.jetbrains.kotlinx",
                "kotlinx-serialization-json",
            ).version(versions.getProperty("serializationLibraryVersion"))
            library(
                "io.kotest.runner.junit5",
                "io.kotest",
                "kotest-runner-junit5",
            ).version(versions.getProperty("kotestLibraryVersion"))
            library(
                "io.kotest.assertions.core",
                "io.kotest",
                "kotest-assertions-core",
            ).version(versions.getProperty("kotestLibraryVersion"))
            library(
                "org.junit.jupiter.platform.launcher",
                "org.junit.platform",
                "junit-platform-launcher",
            ).withoutVersion()
        }
    }
}

rootProject.name = "water-my-plants-project-config"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":catalog",
    ":plugin",
)
