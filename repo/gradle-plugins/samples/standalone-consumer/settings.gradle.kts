pluginManagement {
    repositories {
        maven {
            url = uri(providers.gradleProperty("gradlePluginsPublicationRepository").get())
        }
        maven {
            url = uri(providers.gradleProperty("dependencyCatalogPublicationRepository").get())
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri(providers.gradleProperty("gradlePluginsPublicationRepository").get())
        }
        maven {
            url = uri(providers.gradleProperty("dependencyCatalogPublicationRepository").get())
        }
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            library(
                "io.kotest.runner.junit5",
                "io.kotest",
                "kotest-runner-junit5",
            ).version(providers.gradleProperty("kotestVersion").get())
            library(
                "io.kotest.assertions.core",
                "io.kotest",
                "kotest-assertions-core",
            ).version(providers.gradleProperty("kotestVersion").get())
            bundle(
                "kotest",
                listOf(
                    "io.kotest.runner.junit5",
                    "io.kotest.assertions.core",
                ),
            )
            library(
                "io.mockk",
                "io.mockk",
                "mockk",
            ).version(providers.gradleProperty("mockkVersion").get())
            library(
                "org.junit.platform.launcher",
                "org.junit.platform",
                "junit-platform-launcher",
            ).withoutVersion()
        }

        create("testLibs") {
            library(
                "com.marmatsan.repo.unit.test.dsl",
                "com.marmatsan.repo",
                "unit-test-dsl",
            ).version(providers.gradleProperty("unitTestDslVersion").get())
        }

        create("plugins") {
            val conventionVersion: String = providers.gradleProperty("gradlePluginsVersion").get()
            plugin(
                "com.marmatsan.android",
                "com.marmatsan.android",
            ).version(conventionVersion)
            plugin(
                "com.marmatsan.bddTest",
                "com.marmatsan.bddTest",
            ).version(conventionVersion)
            plugin(
                "com.marmatsan.compose",
                "com.marmatsan.compose",
            ).version(conventionVersion)
            plugin(
                "com.marmatsan.dokkaDocumentation",
                "com.marmatsan.dokkaDocumentation",
            ).version(conventionVersion)
            plugin(
                "com.marmatsan.protobuf",
                "com.marmatsan.protobuf",
            ).version(conventionVersion)
            plugin(
                "com.marmatsan.unitTest",
                "com.marmatsan.unitTest",
            ).version(conventionVersion)
            plugin(
                "org.jetbrains.kotlin.jvm",
                "org.jetbrains.kotlin.jvm",
            ).version(providers.gradleProperty("kotlinVersion").get())
        }
    }
}

rootProject.name = "gradle-plugins-standalone-consumer"
