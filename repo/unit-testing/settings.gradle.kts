@file:Suppress("UnstableApiUsage")

rootProject.name = "unit-testing"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

val versions =
    java.util.Properties().apply {
        file("versions.properties").inputStream().use(::load)
    }

fun version(
    key: String,
): String =
    versions.getProperty(key)
        ?: error("Missing version property '$key' in repo/unit-testing/versions.properties")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            library(
                "io.kotest.runner.junit5",
                "io.kotest",
                "kotest-runner-junit5",
            ).version(version("kotestLibraryVersion"))
            library(
                "io.kotest.assertions.core",
                "io.kotest",
                "kotest-assertions-core",
            ).version(version("kotestLibraryVersion"))
            library(
                "org.junit.jupiter.platform.launcher",
                "org.junit.platform",
                "junit-platform-launcher",
            ).withoutVersion()
        }

        create("plugins") {
            plugin(
                "org.jetbrains.kotlin.jvm",
                "org.jetbrains.kotlin.jvm",
            ).version(version("kotlinVersion"))
            plugin(
                "org.jetbrains.dokka",
                "org.jetbrains.dokka",
            ).version(version("dokkaPluginVersion"))
        }
    }
}

include(":unit-test-dsl")
