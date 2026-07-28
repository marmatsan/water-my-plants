@file:Suppress("UnstableApiUsage")

pluginManagement {
    // The application composition root supplies source substitution; reusable builds only know
    // the generic property and can otherwise resolve the published plugin independently.
    val dependencyCatalogSourceBuildProperty = "dependencyCatalogSourceBuild"
    if (dependencyCatalogSourceBuildProperty !in gradle.startParameter.projectProperties) {
        gradle.startParameter.projectProperties = gradle.startParameter.projectProperties +
            (dependencyCatalogSourceBuildProperty to file("repo/dependency-catalog").absolutePath)
    }

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

// Regular composite inclusions expose repository-owned library coordinates.
includeBuild("./repo/gradle-plugins")
includeBuild("./repo/unit-testing")

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
