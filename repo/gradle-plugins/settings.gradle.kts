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
        id("org.jetbrains.kotlin.jvm") version versions.getProperty("kotlinVersion")
        id("org.jetbrains.dokka") version versions.getProperty("dokkaPluginVersion")
    }
}

plugins {
    id("com.marmatsan.dependencyCatalog.tree")
}

rootProject.name = "gradle-plugins"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyCatalogTree {
    versionsFile.set(file("versions.properties"))

    libraries {
        root("com") {
            library("android.tools.build") {
                artifact(
                    artifact = "gradle",
                    version = version("androidGradlePluginVersion"),
                )
            }
            library("google.protobuf") {
                artifact(
                    artifact = "protobuf-gradle-plugin",
                    version = version("protobufPluginVersion"),
                )
            }
            library("marmatsan.repo") {
                artifact(
                    artifact = "unit-test-dsl",
                    version = version("unitTestDslLibraryVersion"),
                )
            }
        }

        root("org") {
            library("jetbrains.kotlin") {
                artifact(
                    artifact = "kotlin-gradle-plugin",
                    version = version("kotlinVersion"),
                )
            }
            library("jetbrains.dokka") {
                artifact(
                    artifact = "dokka-gradle-plugin",
                    version = version("dokkaPluginVersion"),
                )
            }
            library("junit.platform") {
                artifact("junit-platform-launcher")
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
            library("mockk") {
                artifact(
                    artifact = "mockk",
                    version = version("mockkLibraryVersion"),
                )
            }
        }
    }

    plugins {
        root("org") {
            plugin("jetbrains") {
                plugin("kotlin") {
                    plugin(
                        id = "jvm",
                        version = version("kotlinVersion"),
                    )
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
    ":android",
    ":bdd-test",
    ":compose",
    ":dependencies",
    ":dokka-documentation",
    ":protobuf",
    ":unit-test",
)
