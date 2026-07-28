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
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
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

rootProject.name = "figma-documentation-sync"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyCatalogTree {
    versionsFile.set(file("versions.properties"))

    libraries {
        library(
            group = "com.michael-bull.kotlin-result",
            artifact = "kotlin-result",
            version = version("kotlinResultLibraryVersion"),
        )
        library(
            group = "com.marmatsan.repo",
            artifact = "unit-test-dsl",
            version = version("unitTestDslLibraryVersion"),
        )
        library(
            group = "io.ktor",
            artifact = "ktor-bom",
            version = version("ktorLibraryVersion"),
        )
        library(
            group = "io.ktor",
            artifact = "ktor-client-core",
        )
        library(
            group = "io.ktor",
            artifact = "ktor-client-cio",
        )
        library(
            group = "io.ktor",
            artifact = "ktor-client-content-negotiation",
        )
        library(
            group = "io.ktor",
            artifact = "ktor-serialization-kotlinx-json",
        )
        library(
            group = "io.modelcontextprotocol",
            artifact = "kotlin-sdk-client",
            version = version("mcpKotlinSdkLibraryVersion"),
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
            group = "me.tatarka.inject",
            artifact = "kotlin-inject-compiler-ksp",
            version = version("kotlinInjectLibraryVersion"),
        )
        library(
            group = "me.tatarka.inject",
            artifact = "kotlin-inject-runtime",
            version = version("kotlinInjectLibraryVersion"),
        )
        library(
            group = "org.jetbrains.kotlinx",
            artifact = "kotlinx-serialization-json",
            version = version("serializationLibraryVersion"),
        )
        library(
            group = "org.snakeyaml",
            artifact = "snakeyaml-engine",
            version = version("snakeYamlLibraryVersion"),
        )
        library(
            group = "org.junit.platform",
            artifact = "junit-platform-launcher",
        )
        library(
            group = "org.junit.platform",
            artifact = "junit-platform-suite",
        )
    }

    plugins {
        plugin(
            id = "com.google.devtools.ksp",
            version = version("kspPluginVersion"),
        )
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

include(
    ":data",
    ":domain",
    ":plugin",
    ":teamcity-adapter",
)
