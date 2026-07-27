@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    val versions =
        java.util.Properties().apply {
            file("versions.properties").inputStream().use(::load)
        }

    plugins {
        id("org.jetbrains.kotlin.jvm") version versions.getProperty("kotlinVersion")
        id("org.jetbrains.kotlin.plugin.serialization") version versions.getProperty("kotlinVersion")
        id("org.jetbrains.dokka") version versions.getProperty("dokkaPluginVersion")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
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
            library(
                "org.junit.platform.suite",
                "org.junit.platform",
                "junit-platform-suite",
            ).withoutVersion()
            library(
                "io.cucumber.bom",
                "io.cucumber",
                "cucumber-bom",
            ).version(versions.getProperty("cucumberLibraryVersion"))
            library(
                "io.cucumber.java8",
                "io.cucumber",
                "cucumber-java8",
            ).withoutVersion()
            library(
                "io.cucumber.junit.platform.engine",
                "io.cucumber",
                "cucumber-junit-platform-engine",
            ).withoutVersion()
            library(
                "com.pinterest.ktlint.rule.engine",
                "com.pinterest.ktlint",
                "ktlint-rule-engine",
            ).version(versions.getProperty("ktlintLibraryVersion"))
            library(
                "com.pinterest.ktlint.ruleset.standard",
                "com.pinterest.ktlint",
                "ktlint-ruleset-standard",
            ).version(versions.getProperty("ktlintLibraryVersion"))
        }
    }
}

rootProject.name = "verification-platform"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":domain",
    ":data",
    ":plugin",
)
