rootProject.name = "build-logic"

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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Versions used by android and compose plugin by this build-logic module
val versions = java.util.Properties().apply {
    file("versions.properties").inputStream().use(::load)
}

fun version(key: String): String = versions.getProperty(key)
    ?: error("Missing version property '$key' in build-logic/versions.properties")

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
        // build-logic libraries
        create("libs") {
            /* Build */
            library(
                alias = "build.gradle",
                group = "com.android.tools.build",
                artifact = "gradle"
            ).version(version("androidGradlePlugin"))

            library(
                alias = "kotlin.gradle.plugin",
                group = "org.jetbrains.kotlin",
                artifact = "kotlin-gradle-plugin"
            ).version(version("kotlinVersion"))

            library(
                alias = "protobuf.gradle.plugin",
                group = "com.google.protobuf",
                artifact = "protobuf-gradle-plugin"
            ).version(version("protobufGradlePluginVersion"))

            /* Testing */
            // JUnit5
            library(
                alias = "org.junit.bom",
                group = "org.junit",
                artifact = "junit-bom"
            ).version(version("junit5BomVersion"))

            library(
                alias = "org.junit.jupiter.api",
                group = "org.junit.jupiter",
                artifact = "junit-jupiter-api"
            ).withoutVersion()

            library(
                alias = "org.junit.jupiter.engine",
                group = "org.junit.jupiter",
                artifact = "junit-jupiter-engine"
            ).withoutVersion()

            library(
                alias = "org.junit.jupiter.platform.launcher",
                group = "org.junit.platform",
                artifact = "junit-platform-launcher"
            ).withoutVersion()

            //AssertK
            library(
                alias = "com.willowtreeapps.assertk",
                group = "com.willowtreeapps.assertk",
                artifact = "assertk"
            ).version(version("assertkVersion"))

            // MockK
            library(
                alias = "io.mockk",
                group = "io.mockk",
                artifact = "mockk"
            ).version(version("mockkVersion"))
        }

        create("plugins") {
            plugin(
                alias = "org.jetbrains.kotlinx.kover",
                id = "org.jetbrains.kotlinx.kover"
            ).version(version("kotlinxKoverVersion"))
        }
    }
}

include(
    ":android",
    ":compose",
    ":dependencies",
    ":protobuf",
    ":unitTest"
)
