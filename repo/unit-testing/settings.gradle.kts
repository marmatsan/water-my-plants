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
        library(
            group = "org.junit.platform",
            artifact = "junit-platform-launcher",
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

include(":unit-test-dsl")
