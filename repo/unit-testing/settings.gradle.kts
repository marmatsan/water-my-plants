@file:Suppress("UnstableApiUsage")

pluginManagement {
    val versions =
        java.util.Properties().apply {
            file("versions.properties").inputStream().use(::load)
        }

    providers.gradleProperty("dependencyCatalogSourceBuild").orNull?.let { sourceBuild ->
        includeBuild(sourceBuild)
    }

    repositories {
        providers.gradleProperty("dependencyCatalogPublicationRepository").orNull?.let { repository ->
            maven { url = uri(repository) }
        }
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.marmatsan.dependencyCatalog.tree") version
            versions.getProperty("dependencyCatalogVersion")
    }
}

plugins {
    id("com.marmatsan.dependencyCatalog.tree")
}

rootProject.name = "unit-testing"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

dependencyCatalogTree {
    versionsFile.set(file("versions.properties"))

    libraries {
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
        root("org") {
            library("junit.platform") {
                artifact(
                    artifact = "junit-platform-launcher",
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

include(":unit-test-dsl")
