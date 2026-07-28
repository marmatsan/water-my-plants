pluginManagement {
    repositories {
        maven {
            url = uri(providers.gradleProperty("verificationPlatformPublicationRepository").get())
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri(providers.gradleProperty("verificationPlatformPublicationRepository").get())
        }
        mavenCentral()
    }

    versionCatalogs {
        create("plugins") {
            plugin(
                "com.marmatsan.verificationPlatform",
                "com.marmatsan.verificationPlatform",
            ).version(providers.gradleProperty("verificationPlatformVersion").get())
        }
    }
}

rootProject.name = "verification-platform-standalone-consumer"
