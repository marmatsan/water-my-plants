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
        root("com") {
            library("marmatsan.repo") {
                artifact(
                    artifact = "catalog-api",
                    version = portableVersion,
                )
                artifact(
                    artifact = "catalog-core",
                    version = portableVersion,
                )
                artifact(
                    artifact = "catalog-gradle-plugin",
                    version = portableVersion,
                )
                artifact(
                    artifact = "unit-test-dsl",
                )
            }
            library("marmatsan.figma-documentation-sync") {
                artifact(
                    artifact = "domain",
                    version = portableVersion,
                )
                artifact(
                    artifact = "data",
                    version = portableVersion,
                )
                artifact(
                    artifact = "plugin",
                    version = portableVersion,
                )
                artifact(
                    artifact = "teamcity-adapter",
                    version = portableVersion,
                )
            }
            library("michael-bull.kotlin-result") {
                artifact(
                    artifact = "kotlin-result",
                    version = version("kotlinResultLibraryVersion"),
                )
            }
        }
        root("org") {
            library("jetbrains.kotlinx") {
                artifact(
                    artifact = "kotlinx-serialization-json",
                    version = version("serializationLibraryVersion"),
                )
            }
            library("junit.platform") {
                artifact(
                    artifact = "junit-platform-launcher",
                )
            }
        }
        root("io") {
            library("kotest") {
                artifact(
                    artifact = "kotest-runner-junit5",
                    version = version("kotestLibraryVersion"),
                )
                artifact(
                    artifact = "kotest-assertions-core",
                    version = version("kotestLibraryVersion"),
                )
            }
        }
    }

    plugins {
        root("org") {
            plugin("jetbrains") {
                plugin(
                    id = "dokka",
                    version = version("dokkaPluginVersion"),
                )
                plugin(
                    id = "kotlin.jvm",
                    version = version("kotlinVersion"),
                )
            }
        }
    }
}

rootProject.name = "water-my-plants-project-config"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":catalog",
    ":plugin",
)
