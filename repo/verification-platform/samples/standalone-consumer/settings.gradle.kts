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
    versionCatalogs {
        create("plugins") {
            plugin(
                "com.marmatsan.verificationPlatform",
                "com.marmatsan.verificationPlatform"
            ).version(providers.gradleProperty("verificationPlatformVersion").get())
        }
    }
}

rootProject.name = "verification-platform-standalone-consumer"
