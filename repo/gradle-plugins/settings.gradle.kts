pluginManagement {
    val versions =
        java.util.Properties().apply {
            file("versions.properties").inputStream().use(::load)
        }

    val dependencyCatalogSourceBuild =
        providers.gradleProperty("dependencyCatalogSourceBuild").orNull
    if (
        dependencyCatalogSourceBuild != null &&
        "dependencyCatalogPublicationRepository" !in gradle.startParameter.projectProperties
    ) {
        gradle.startParameter.projectProperties =
            gradle.startParameter.projectProperties +
            (
                "dependencyCatalogPublicationRepository" to
                    file("build/dependency-catalog-publication-repository").absolutePath
            )
    }

    dependencyCatalogSourceBuild?.let { sourceBuild ->
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

// Regular composite inclusion supplies catalog-api dependency substitution. The pluginManagement
// inclusion above separately makes the settings plugin available during bootstrap.
providers.gradleProperty("dependencyCatalogSourceBuild").orNull?.let { sourceBuild ->
    includeBuild(sourceBuild)
}

rootProject.name = "gradle-plugins"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyCatalogTree {
    versionsFile.set(file("versions.properties"))

    libraries {
        library(
            group = "com.android.tools.build",
            artifact = "gradle",
            version = version("androidGradlePluginVersion"),
        )
        library(
            group = "com.google.protobuf",
            artifact = "protobuf-gradle-plugin",
            version = version("protobufPluginVersion"),
        )
        library(
            group = "com.marmatsan.repo",
            artifact = "catalog-api",
            version = version("dependencyCatalogVersion"),
        )
        library(
            group = "com.marmatsan.repo",
            artifact = "unit-test-dsl",
            version = version("unitTestDslLibraryVersion"),
        )
        library(
            group = "org.jetbrains.kotlin",
            artifact = "kotlin-gradle-plugin",
            version = version("kotlinVersion"),
        )
        library(
            group = "org.jetbrains.dokka",
            artifact = "dokka-gradle-plugin",
            version = version("dokkaPluginVersion"),
        )
        library(
            group = "org.junit.platform",
            artifact = "junit-platform-launcher",
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
            group = "io.mockk",
            artifact = "mockk",
            version = version("mockkLibraryVersion"),
        )
    }

    plugins {
        plugin(
            id = "org.jetbrains.kotlin.jvm",
            version = version("kotlinVersion"),
        )
        plugin(
            id = "org.jetbrains.dokka",
            version = version("dokkaPluginVersion"),
        )
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
