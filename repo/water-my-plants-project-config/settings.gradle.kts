@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// This product-only composition build is the authorized owner of sibling
// build wiring. Reusable included builds never include one another.
includeBuild("../dependency-catalog")
includeBuild("../figma-documentation-sync")
includeBuild("../gradle-plugins")
includeBuild("../unit-testing")

val versions =
    java.util.Properties().apply {
        file("versions.properties").inputStream().use(::load)
    }

fun version(
    key: String,
): String =
    versions.getProperty(key)
        ?: error("Missing version property '$key' in repo/water-my-plants-project-config/versions.properties")

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

    versionCatalogs {
        create("libs") {
            version(
                "portableTooling",
                portableVersion,
            )

            // Portable composition APIs. Local included builds substitute these
            // versioned coordinates during repository development.
            library(
                "com.marmatsan.repo.catalog.api",
                "com.marmatsan.repo",
                "catalog-api",
            ).versionRef("portableTooling")
            library(
                "com.marmatsan.repo.catalog.core",
                "com.marmatsan.repo",
                "catalog-core",
            ).versionRef("portableTooling")
            library(
                "com.marmatsan.repo.catalog.gradle.plugin",
                "com.marmatsan.repo",
                "catalog-gradle-plugin",
            ).versionRef("portableTooling")
            library(
                "com.marmatsan.figma.documentation.sync.domain",
                "com.marmatsan.figma-documentation-sync",
                "domain",
            ).versionRef("portableTooling")
            library(
                "com.marmatsan.figma.documentation.sync.data",
                "com.marmatsan.figma-documentation-sync",
                "data",
            ).versionRef("portableTooling")
            library(
                "com.marmatsan.figma.documentation.sync.plugin",
                "com.marmatsan.figma-documentation-sync",
                "plugin",
            ).versionRef("portableTooling")
            library(
                "com.marmatsan.figma.documentation.sync.teamcity.adapter",
                "com.marmatsan.figma-documentation-sync",
                "teamcity-adapter",
            ).versionRef("portableTooling")
            library(
                "com.marmatsan.repo.unit.test.dsl",
                "com.marmatsan.repo",
                "unit-test-dsl",
            ).withoutVersion()

            library(
                "org.jetbrains.kotlinx.serialization.json",
                "org.jetbrains.kotlinx",
                "kotlinx-serialization-json",
            ).version(version("serializationLibraryVersion"))
            library(
                "io.kotest.runner.junit5",
                "io.kotest",
                "kotest-runner-junit5",
            ).version(version("kotestLibraryVersion"))
            library(
                "io.kotest.assertions.core",
                "io.kotest",
                "kotest-assertions-core",
            ).version(version("kotestLibraryVersion"))
            library(
                "org.junit.jupiter.platform.launcher",
                "org.junit.platform",
                "junit-platform-launcher",
            ).withoutVersion()
        }

        create("plugins") {
            plugin(
                "org.jetbrains.kotlin.jvm",
                "org.jetbrains.kotlin.jvm",
            ).version(version("kotlinVersion"))
            plugin(
                "org.jetbrains.dokka",
                "org.jetbrains.dokka",
            ).version(version("dokkaPluginVersion"))
        }
    }
}

rootProject.name = "water-my-plants-project-config"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":catalog",
    ":plugin",
)
