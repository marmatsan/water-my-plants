@file:Suppress("UnstableApiUsage")

pluginManagement {
    val projectConfigVersion: String =
        providers.gradleProperty("projectConfigVersion").get()

    repositories {
        maven {
            url = uri(providers.gradleProperty("projectConfigPublicationRepository").get())
        }
        maven {
            url = uri(providers.gradleProperty("dependencyCatalogPublicationRepository").get())
        }
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.marmatsan.projectConfig.settings") version projectConfigVersion
        id("com.marmatsan.projectConfig") version projectConfigVersion
    }
}

plugins {
    id("com.marmatsan.projectConfig.settings")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri(providers.gradleProperty("projectConfigPublicationRepository").get())
        }
        maven {
            url = uri(providers.gradleProperty("dependencyCatalogPublicationRepository").get())
        }
        mavenCentral()
    }
}

projectConfig {
    versionsFile.set(file("versions.properties"))

    dependencyCatalog {
        libraries {
            root("io") {
                library("ktor") {
                    artifact(
                        artifact = "ktor-client-core",
                        version = version("ktorLibraryVersion")
                    )
                }
            }
        }

        plugins {
            root("org") {
                plugin("jetbrains.kotlin") {
                    plugin(
                        id = "jvm",
                        version = version("kotlinVersion")
                    )
                }
            }
        }
    }
}

rootProject.name = "health"
