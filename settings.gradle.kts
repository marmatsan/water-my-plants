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
    includeBuild("./repo/dependency-catalog")
    includeBuild("./repo/gradle-plugins")
    includeBuild("./repo/figma-documentation-sync")
    includeBuild("./repo/verification-platform")
    includeBuild("./repo/water-my-plants-project-config")
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.marmatsan.waterMyPlantsSettings") apply true
}

rootProject.name = "water-my-plants"

val appModule =
    listOf(
        ":app",
    )
val coreModule =
    listOf(
        ":core:ui",
    )
val onboardingModule =
    listOf(
        ":onboarding:ui",
    )

include(
    *appModule.toTypedArray(),
    *coreModule.toTypedArray(),
    *onboardingModule.toTypedArray(),
)
