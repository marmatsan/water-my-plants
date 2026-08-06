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

providers.gradleProperty("dependencyCatalogSourceBuild").orNull?.let { sourceBuild ->
    includeBuild(sourceBuild)
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        providers.gradleProperty("dependencyCatalogPublicationRepository").orNull?.let { repository ->
            maven { url = uri(repository) }
        }
        mavenCentral()
    }
}

rootProject.name = "project-config"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyCatalogTree {
    versionsFile.set(file("versions.properties"))

    libraries {
        root("com") {
            library("marmatsan.repo") {
                artifact(
                    artifact = "catalog-core",
                    version = version("dependencyCatalogVersion")
                )
                artifact(
                    artifact = "catalog-gradle-plugin",
                    version = version("dependencyCatalogVersion")
                )
            }
        }
    }

    plugins {
        root("org") {
            plugin("jetbrains") {
                plugin(
                    id = "dokka",
                    version = version("dokkaPluginVersion")
                )
                plugin(
                    id = "kotlin.jvm",
                    version = version("kotlinVersion")
                )
            }
        }
    }
}

include(":plugin")
