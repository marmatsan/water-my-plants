@file:Suppress("UnstableApiUsage")

pluginManagement {
    val versions: java.util.Properties =
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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "figma-documentation-sync"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyCatalogTree {
    versionsFile.set(file("versions.properties"))

    libraries {
        root("com") {
            library("michael-bull.kotlin-result") {
                artifact(
                    artifact = "kotlin-result",
                    version = version("kotlinResultLibraryVersion")
                )
            }
            library("marmatsan.repo") {
                artifact(
                    artifact = "unit-test-dsl",
                    version = version("unitTestDslLibraryVersion")
                )
            }
        }
        root("io") {
            library("ktor") {
                artifact(
                    artifact = "ktor-bom",
                    version = version("ktorLibraryVersion")
                )
                artifactsBundle(
                    "ktor-client-core",
                    "ktor-client-cio",
                    "ktor-client-content-negotiation",
                    "ktor-serialization-kotlinx-json",
                    alias = "ktorClientBundle"
                )
            }
            library("modelcontextprotocol") {
                artifact(
                    artifact = "kotlin-sdk-client",
                    version = version("mcpKotlinSdkLibraryVersion")
                )
            }
            library("cucumber") {
                artifact(
                    artifact = "cucumber-bom",
                    version = version("cucumberLibraryVersion")
                )
                artifactsBundle(
                    "cucumber-java8",
                    "cucumber-junit-platform-engine",
                    alias = "cucumberBundle"
                )
            }
            library("kotest") {
                artifactsBundle(
                    "kotest-runner-junit5",
                    "kotest-assertions-core",
                    alias = "kotestBundle",
                    version = version("kotestLibraryVersion")
                )
            }
        }
        root("me") {
            library("tatarka.inject") {
                artifact(
                    artifact = "kotlin-inject-compiler-ksp",
                    version = version("kotlinInjectLibraryVersion")
                )
                artifact(
                    artifact = "kotlin-inject-runtime",
                    version = version("kotlinInjectLibraryVersion")
                )
            }
        }
        root("org") {
            library("jetbrains.kotlinx") {
                artifact(
                    artifact = "kotlinx-serialization-json",
                    version = version("serializationLibraryVersion")
                )
            }
            library("snakeyaml") {
                artifact(
                    artifact = "snakeyaml-engine",
                    version = version("snakeYamlLibraryVersion")
                )
            }
            library("junit.platform") {
                artifact(
                    artifact = "junit-platform-launcher"
                )
                artifact(
                    artifact = "junit-platform-suite"
                )
            }
        }
    }

    plugins {
        root("com") {
            plugin(
                id = "google.devtools.ksp",
                version = version("kspPluginVersion")
            )
        }
        root("org") {
            plugin("jetbrains") {
                plugin(
                    id = "dokka",
                    version = version("dokkaPluginVersion")
                )
                plugin("kotlin") {
                    plugin(
                        id = "jvm",
                        version = version("kotlinVersion")
                    )
                    plugin(
                        id = "plugin.serialization",
                        version = version("kotlinVersion")
                    )
                }
            }
        }
    }
}

include(
    ":data",
    ":domain",
    ":plugin",
    ":teamcity-adapter",
    ":teamcity-operations"
)
