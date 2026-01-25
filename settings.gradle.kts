@file:Suppress("UnstableApiUsage")

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
    // Custom Gradle plugins
    includeBuild("./build-logic")
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.marmatsan.dependencies") apply true
}

rootProject.name = "water-my-plants"

val appModule = listOf(
    ":app"
)
val coreModule = listOf(
    ":core:core_ui"
)
val onboardingModule = listOf(
    ":onboarding:onboarding_ui"
)

include(
    *appModule.toTypedArray(),
    *coreModule.toTypedArray(),
    *onboardingModule.toTypedArray()
)