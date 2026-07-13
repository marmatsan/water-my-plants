@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    val versions = java.util.Properties().apply {
        file("versions.properties").inputStream().use(::load)
    }

    plugins {
        id("org.jetbrains.kotlin.jvm") version versions.getProperty("kotlinVersion")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "dependency-catalog"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":catalog-core",
    ":water-my-plants-catalog"
)
