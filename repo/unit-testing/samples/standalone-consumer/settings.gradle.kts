pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri(providers.gradleProperty("unitTestingPublicationRepository").get())
        }
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            library(
                "com.marmatsan.repo.unit.test.dsl",
                "com.marmatsan.repo",
                "unit-test-dsl",
            ).version(providers.gradleProperty("unitTestDslVersion").get())
        }

        create("plugins") {
            plugin(
                "org.jetbrains.kotlin.jvm",
                "org.jetbrains.kotlin.jvm",
            ).version(providers.gradleProperty("kotlinVersion").get())
        }
    }
}

rootProject.name = "unit-test-dsl-standalone-consumer"
