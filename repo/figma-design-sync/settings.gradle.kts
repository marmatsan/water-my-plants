@file:Suppress("UnstableApiUsage")

rootProject.name = "figma-design-sync"

pluginManagement {
    repositories {
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

}

includeBuild("../dependency-catalog")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Versions owned by dependency-catalog and consumed by this included build.
val versions = java.util.Properties().apply {
    file("../dependency-catalog/versions.properties").inputStream().use(::load)
}

fun version(key: String): String = versions.getProperty(key)
    ?: error("Missing version property '$key' in repo/dependency-catalog/versions.properties")

fun VersionCatalogBuilder.library(
    alias: String,
    group: String,
    artifact: String
) = library(alias, group, artifact)

fun VersionCatalogBuilder.plugin(
    alias: String,
    id: String
) = plugin(alias, id)

dependencyResolutionManagement {
    versionCatalogs {
        // figma-design-sync libraries
        create("libs") {
            /* Runtime */
            library(
                alias = "io.ktor.bom",
                group = "io.ktor",
                artifact = "ktor-bom"
            ).version(version("ktorLibraryVersion"))

            library(
                alias = "io.ktor.client.core",
                group = "io.ktor",
                artifact = "ktor-client-core"
            ).withoutVersion()

            library(
                alias = "io.ktor.client.cio",
                group = "io.ktor",
                artifact = "ktor-client-cio"
            ).withoutVersion()

            library(
                alias = "io.ktor.client.content.negotiation",
                group = "io.ktor",
                artifact = "ktor-client-content-negotiation"
            ).withoutVersion()

            library(
                alias = "io.ktor.serialization.kotlinx.json",
                group = "io.ktor",
                artifact = "ktor-serialization-kotlinx-json"
            ).withoutVersion()

            library(
                alias = "org.jetbrains.kotlinx.serialization.json",
                group = "org.jetbrains.kotlinx",
                artifact = "kotlinx-serialization-json"
            ).version(version("serializationLibraryVersion"))

            library(
                alias = "org.snakeyaml.engine",
                group = "org.snakeyaml",
                artifact = "snakeyaml-engine"
            ).version(version("snakeYamlLibraryVersion"))

            library(
                alias = "me.tatarka.inject.kotlin.inject.compiler.ksp",
                group = "me.tatarka.inject",
                artifact = "kotlin-inject-compiler-ksp"
            ).version(version("kotlinInjectLibraryVersion"))

            library(
                alias = "me.tatarka.inject.kotlin.inject.runtime",
                group = "me.tatarka.inject",
                artifact = "kotlin-inject-runtime"
            ).version(version("kotlinInjectLibraryVersion"))
            
            /* Testing */
            // JUnit Platform
            library(
                alias = "org.junit.jupiter.platform.launcher",
                group = "org.junit.platform",
                artifact = "junit-platform-launcher"
            ).withoutVersion()

            library(
                alias = "org.junit.platform.suite",
                group = "org.junit.platform",
                artifact = "junit-platform-suite"
            ).withoutVersion()

            // Cucumber
            library(
                alias = "io.cucumber.bom",
                group = "io.cucumber",
                artifact = "cucumber-bom"
            ).version(version("cucumberLibraryVersion"))

            library(
                alias = "io.cucumber.java8",
                group = "io.cucumber",
                artifact = "cucumber-java8"
            ).withoutVersion()

            library(
                alias = "io.cucumber.junit.platform.engine",
                group = "io.cucumber",
                artifact = "cucumber-junit-platform-engine"
            ).withoutVersion()

            // Kotest
            library(
                alias = "io.kotest.runner.junit5",
                group = "io.kotest",
                artifact = "kotest-runner-junit5"
            ).version(version("kotestLibraryVersion"))

            library(
                alias = "io.kotest.assertions.core",
                group = "io.kotest",
                artifact = "kotest-assertions-core"
            ).version(version("kotestLibraryVersion"))

        }

        create("plugins") {
            plugin(
                alias = "com.google.devtools.ksp",
                id = "com.google.devtools.ksp"
            ).version(version("kspPluginVersion"))

            plugin(
                alias = "org.jetbrains.kotlin.plugin.serialization",
                id = "org.jetbrains.kotlin.plugin.serialization"
            ).version(version("kotlinVersion"))

            plugin(
                alias = "org.jetbrains.dokka",
                id = "org.jetbrains.dokka"
            ).version(version("dokkaPluginVersion"))
        }
    }
}

include(
    ":data",
    ":domain",
    ":plugin",
    ":project-config",
    ":teamcity-adapter"
)
