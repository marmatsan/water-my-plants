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
        id("org.jetbrains.dokka") version versions.getProperty("dokkaPluginVersion")
        id("org.jetbrains.kotlin.jvm") version versions.getProperty("kotlinVersion")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    val versions =
        java.util.Properties().apply {
            file("versions.properties").inputStream().use(::load)
        }

    versionCatalogs {
        create("libs") {
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
                "io.mockk",
                "io.mockk",
                "mockk",
            ).version(versions.getProperty("mockkLibraryVersion"))
            library(
                "org.junit.jupiter.platform.launcher",
                "org.junit.platform",
                "junit-platform-launcher",
            ).withoutVersion()
            library(
                "com.marmatsan.repo.unit.test.dsl",
                "com.marmatsan.repo",
                "unit-test-dsl",
            ).version(versions.getProperty("unitTestDslLibraryVersion"))
        }

        create("plugins") {
            plugin(
                "org.jetbrains.dokka",
                "org.jetbrains.dokka",
            ).version(versions.getProperty("dokkaPluginVersion"))
            plugin(
                "org.jetbrains.kotlin.jvm",
                "org.jetbrains.kotlin.jvm",
            ).version(versions.getProperty("kotlinVersion"))
        }
    }
}

rootProject.name = "dependency-catalog"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":catalog-api",
    ":catalog-core",
    ":catalog-gradle-plugin",
    ":catalog-tree-gradle-plugin",
)
