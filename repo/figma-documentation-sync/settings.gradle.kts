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
        root("com") {
            library("michael-bull.kotlin-result") {
                artifact(
                    artifact = "kotlin-result",
                    version = version("kotlinResultLibraryVersion"),
                )
            }
            library("marmatsan.repo") {
                artifact(
                    artifact = "unit-test-dsl",
                    version = version("unitTestDslLibraryVersion"),
                )
            }
        }

        root("io") {
            library("ktor") {
                artifact(
                    artifact = "ktor-bom",
                    version = version("ktorLibraryVersion"),
                )
                artifact(
                    artifact = "ktor-client-core",
                )
                artifact(
                    artifact = "ktor-client-cio",
                )
                artifact(
                    artifact = "ktor-client-content-negotiation",
                )
                artifact(
                    artifact = "ktor-serialization-kotlinx-json",
                )
            }
            library("modelcontextprotocol") {
                artifact(
                    artifact = "kotlin-sdk-client",
                    version = version("mcpKotlinSdkLibraryVersion"),
                )
            }
            library("cucumber") {
                artifact(
                    artifact = "cucumber-bom",
                    version = version("cucumberLibraryVersion"),
                )
                artifact(
                    artifact = "cucumber-java8",
                )
                artifact(
                    artifact = "cucumber-junit-platform-engine",
                )
            }
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

        root("me") {
            library("tatarka.inject") {
                artifact(
                    artifact = "kotlin-inject-compiler-ksp",
                    version = version("kotlinInjectLibraryVersion"),
                )
                artifact(
                    artifact = "kotlin-inject-runtime",
                    version = version("kotlinInjectLibraryVersion"),
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
            library("snakeyaml") {
                artifact(
                    artifact = "snakeyaml-engine",
                    version = version("snakeYamlLibraryVersion"),
                )
            }
            library("junit.platform") {
                artifact(
                    artifact = "junit-platform-launcher",
                )
                artifact(
                    artifact = "junit-platform-suite",
                )
            }
        }
    }

    plugins {
        root("com") {
            plugin("google.devtools") {
                plugin(
                    id = "ksp",
                    version = version("kspPluginVersion"),
                )
            }
        }

        root("org") {
            plugin("jetbrains") {
                plugin("kotlin") {
                    plugin(
                        id = "jvm",
                        version = version("kotlinVersion"),
                    )
                    plugin("plugin") {
                        plugin(
                            id = "serialization",
                            version = version("kotlinVersion"),
                        )
                    }
                }
                plugin(
                    id = "dokka",
                    version = version("dokkaPluginVersion"),
                )
            }
        }
    }
}

include(
    ":data",
    ":domain",
    ":plugin",
    ":teamcity-adapter",
)
