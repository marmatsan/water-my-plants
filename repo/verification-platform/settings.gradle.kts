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
        root("org") {
            library("jetbrains.kotlinx") {
                artifact(
                    artifact = "kotlinx-serialization-json",
                    version = version("serializationLibraryVersion"),
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
        }
        root("com") {
            library("marmatsan.repo") {
                artifact(
                    artifact = "unit-test-dsl",
                    version = version("unitTestDslLibraryVersion"),
                )
            }
            library("michael-bull.kotlin-result") {
                artifact(
                    artifact = "kotlin-result",
                    version = version("kotlinResultLibraryVersion"),
                )
            }
            library("pinterest.ktlint") {
                artifact(
                    artifact = "ktlint-rule-engine",
                    version = version("ktlintLibraryVersion"),
                )
                artifact(
                    artifact = "ktlint-ruleset-standard",
                    version = version("ktlintLibraryVersion"),
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
                plugin("kotlin") {
                    plugin(
                        id = "jvm",
                        version = version("kotlinVersion"),
                    )
                    plugin(
                        id = "plugin.serialization",
                        version = version("kotlinVersion"),
                    )
                }
            }
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
