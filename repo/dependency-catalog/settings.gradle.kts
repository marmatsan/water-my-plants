@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }

    val versions: java.util.Properties =
        java.util.Properties().apply {
            file("versions.properties").inputStream().use(::load)
        }

    versionCatalogs {
        create("libs") {
            library(
                "io.kotest.runner.junit5",
                "io.kotest",
                "kotest-runner-junit5"
            ).version(versions.getProperty("kotestLibraryVersion"))
            library(
                "io.kotest.assertions.core",
                "io.kotest",
                "kotest-assertions-core"
            ).version(versions.getProperty("kotestLibraryVersion"))
            bundle(
                "kotestBundle",
                listOf(
                    "io.kotest.runner.junit5",
                    "io.kotest.assertions.core"
                )
            )
            library(
                "io.mockk",
                "io.mockk",
                "mockk"
            ).version(versions.getProperty("mockkLibraryVersion"))
            library(
                "org.junit.platform.launcher",
                "org.junit.platform",
                "junit-platform-launcher"
            ).withoutVersion()
            library(
                "com.marmatsan.repo.unit.test.dsl",
                "com.marmatsan.repo",
                "unit-test-dsl"
            ).version(versions.getProperty("unitTestDslLibraryVersion"))
        }

        create("plugins") {
            plugin(
                "org.jetbrains.dokka",
                "org.jetbrains.dokka"
            ).version(versions.getProperty("dokkaPluginVersion"))
            plugin(
                "org.jetbrains.kotlin.jvm",
                "org.jetbrains.kotlin.jvm"
            ).version(versions.getProperty("kotlinVersion"))
        }
    }
}

rootProject.name = "dependency-catalog"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":catalog-api",
    ":catalog-core",
    ":catalog-gradle-plugin",
    ":catalog-tree-gradle-plugin"
)
