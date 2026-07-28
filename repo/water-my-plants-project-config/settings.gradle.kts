@file:Suppress("UnstableApiUsage")

pluginManagement {
    val versions =
        java.util.Properties().apply {
            file("versions.properties").inputStream().use(::load)
        }
    val dependencyCatalogSourceBuild =
        providers.gradleProperty("dependencyCatalogSourceBuild").orNull
            ?: file("../dependency-catalog").absolutePath
    includeBuild(dependencyCatalogSourceBuild)

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.marmatsan.dependencyCatalog.tree") version
            versions.getProperty("dependencyCatalogPluginVersion")
    }
}

plugins {
    id("com.marmatsan.dependencyCatalog.tree")
}

// This product-only composition build is the authorized owner of sibling
// build wiring. Reusable included builds never include one another.
includeBuild("../dependency-catalog")
includeBuild("../figma-documentation-sync")
includeBuild("../gradle-plugins")
includeBuild("../unit-testing")

val portableVersion =
    providers
        .gradleProperty("figmaDocumentationSyncVersion")
        .getOrElse("0.1.0-SNAPSHOT")

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
}

dependencyCatalogTree {
    versionsFile.set(file("versions.properties"))

    libraries {
        // Portable composition APIs. Local included builds substitute these
        // versioned coordinates during repository development.
        library(
            group = "com.marmatsan.repo",
            artifact = "catalog-api",
            version = portableVersion,
        )
        library(
            group = "com.marmatsan.repo",
            artifact = "catalog-core",
            version = portableVersion,
        )
        library(
            group = "com.marmatsan.repo",
            artifact = "catalog-gradle-plugin",
            version = portableVersion,
        )
        library(
            group = "com.marmatsan.repo",
            artifact = "unit-test-dsl",
        )
        library(
            group = "com.marmatsan.figma-documentation-sync",
            artifact = "domain",
            version = portableVersion,
        )
        library(
            group = "com.marmatsan.figma-documentation-sync",
            artifact = "data",
            version = portableVersion,
        )
        library(
            group = "com.marmatsan.figma-documentation-sync",
            artifact = "plugin",
            version = portableVersion,
        )
        library(
            group = "com.marmatsan.figma-documentation-sync",
            artifact = "teamcity-adapter",
            version = portableVersion,
        )
        library(
            group = "com.michael-bull.kotlin-result",
            artifact = "kotlin-result",
            version = version("kotlinResultLibraryVersion"),
        )
        library(
            group = "org.jetbrains.kotlinx",
            artifact = "kotlinx-serialization-json",
            version = version("serializationLibraryVersion"),
        )
        library(
            group = "org.junit.platform",
            artifact = "junit-platform-launcher",
        )
        library(
            group = "io.kotest",
            artifact = "kotest-runner-junit5",
            version = version("kotestLibraryVersion"),
        )
        library(
            group = "io.kotest",
            artifact = "kotest-assertions-core",
            version = version("kotestLibraryVersion"),
        )
    }

    plugins {
        plugin(
            id = "org.jetbrains.kotlin.jvm",
            version = version("kotlinVersion"),
        )
        plugin(
            id = "org.jetbrains.dokka",
            version = version("dokkaPluginVersion"),
        )
    }
}

rootProject.name = "water-my-plants-project-config"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":catalog",
    ":plugin",
)
