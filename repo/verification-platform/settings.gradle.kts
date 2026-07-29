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
                artifactsBundle(
                    "kotest-runner-junit5",
                    "kotest-assertions-core",
                    alias = "kotest",
                    version = version("kotestLibraryVersion"),
                )
            }
            library("cucumber") {
                artifact(
                    artifact = "cucumber-bom",
                    version = version("cucumberLibraryVersion"),
                )
                artifactsBundle(
                    "cucumber-java8",
                    "cucumber-junit-platform-engine",
                    alias = "cucumber",
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
                artifactsBundle(
                    "ktlint-rule-engine",
                    "ktlint-ruleset-standard",
                    alias = "ktlint",
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
