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
        id("org.jetbrains.kotlin.jvm") version versions.getProperty("kotlinVersion")
        id("org.jetbrains.kotlin.plugin.serialization") version versions.getProperty("kotlinVersion")
        id("org.jetbrains.dokka") version versions.getProperty("dokkaPluginVersion")
    }
}

plugins {
    id("com.marmatsan.dependencyCatalog.tree")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyCatalogTree {
    versionsFile.set(file("versions.properties"))

    libraries {
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
            group = "org.junit.platform",
            artifact = "junit-platform-suite",
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
        library(
            group = "io.cucumber",
            artifact = "cucumber-bom",
            version = version("cucumberLibraryVersion"),
        )
        library(
            group = "io.cucumber",
            artifact = "cucumber-java8",
        )
        library(
            group = "io.cucumber",
            artifact = "cucumber-junit-platform-engine",
        )
        library(
            group = "com.marmatsan.repo",
            artifact = "unit-test-dsl",
            version = version("unitTestDslLibraryVersion"),
        )
        library(
            group = "com.michael-bull.kotlin-result",
            artifact = "kotlin-result",
            version = version("kotlinResultLibraryVersion"),
        )
        library(
            group = "com.pinterest.ktlint",
            artifact = "ktlint-rule-engine",
            version = version("ktlintLibraryVersion"),
        )
        library(
            group = "com.pinterest.ktlint",
            artifact = "ktlint-ruleset-standard",
            version = version("ktlintLibraryVersion"),
        )
    }

    plugins {
        plugin(
            id = "org.jetbrains.kotlin.jvm",
            version = version("kotlinVersion"),
        )
        plugin(
            id = "org.jetbrains.kotlin.plugin.serialization",
            version = version("kotlinVersion"),
        )
        plugin(
            id = "org.jetbrains.dokka",
            version = version("dokkaPluginVersion"),
        )
    }
}

rootProject.name = "verification-platform"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":domain",
    ":data",
    ":plugin",
)
