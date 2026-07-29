pluginManagement {
    val versions: java.util.Properties =
        java.util.Properties().apply {
            file("versions.properties").inputStream().use(::load)
        }

    val dependencyCatalogSourceBuild: String? =
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

// Regular composite inclusion supplies catalog-api dependency substitution. The pluginManagement
// inclusion above separately makes the settings plugin available during bootstrap.
providers.gradleProperty("dependencyCatalogSourceBuild").orNull?.let { sourceBuild ->
    includeBuild(sourceBuild)
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "gradle-plugins"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyCatalogTree {
    versionsFile.set(file("versions.properties"))

    libraries {
        root("com") {
            library("android.tools.build") {
                artifact(
                    artifact = "gradle",
                    version = version("androidGradlePluginVersion")
                )
            }
            library("google.protobuf") {
                artifact(
                    artifact = "protobuf-gradle-plugin",
                    version = version("protobufPluginVersion")
                )
            }
            library("marmatsan.repo") {
                artifact(
                    artifact = "catalog-api",
                    version = version("dependencyCatalogVersion")
                )
                artifact(
                    artifact = "unit-test-dsl",
                    version = version("unitTestDslLibraryVersion")
                )
            }
        }
        root("org") {
            library("jetbrains.kotlin") {
                artifact(
                    artifact = "kotlin-gradle-plugin",
                    version = version("kotlinVersion")
                )
            }
            library("jetbrains.dokka") {
                artifact(
                    artifact = "dokka-gradle-plugin",
                    version = version("dokkaPluginVersion")
                )
            }
            library("junit.platform") {
                artifact(
                    artifact = "junit-platform-launcher"
                )
            }
        }
        root("io") {
            library("kotest") {
                artifactsBundle(
                    "kotest-runner-junit5",
                    "kotest-assertions-core",
                    alias = "kotest",
                    version = version("kotestLibraryVersion")
                )
            }
            library("mockk") {
                artifact(
                    artifact = "mockk",
                    version = version("mockkLibraryVersion")
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

include(
    ":android",
    ":bdd-test",
    ":compose",
    ":dependencies",
    ":dokka-documentation",
    ":protobuf",
    ":unit-test"
)
