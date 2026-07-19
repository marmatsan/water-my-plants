@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    val versions = java.util.Properties().apply {
        file("../dependency-catalog/versions.properties").inputStream().use(::load)
    }

    plugins {
        id("org.jetbrains.kotlin.jvm") version versions.getProperty("kotlinVersion")
        id("org.jetbrains.kotlin.plugin.serialization") version versions.getProperty("kotlinVersion")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    val versions = java.util.Properties().apply {
        file("../dependency-catalog/versions.properties").inputStream().use(::load)
    }

    versionCatalogs {
        create("libs") {
            library(
                "org.jetbrains.kotlinx.serialization.json",
                "org.jetbrains.kotlinx",
                "kotlinx-serialization-json"
            ).version(versions.getProperty("serializationLibraryVersion"))
            library(
                "io.kotest.runner.junit5",
                "io.kotest",
                "kotest-runner-junit5"
            ).version(versions.getProperty("kotestLibraryVersion"))
            library(
                "io.kotest.assertions.core",
                "io.kotest",
                "kotest-assertions-core"
            ).version(versions.getProperty("kotestLibraryVersion"))
            library(
                "org.junit.jupiter.platform.launcher",
                "org.junit.platform",
                "junit-platform-launcher"
            ).withoutVersion()
        }
    }
}

rootProject.name = "ci"
