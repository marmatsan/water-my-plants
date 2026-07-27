pluginManagement {
    val pluginRepository =
        providers
            .gradleProperty("figmaDocumentationSyncPublicationRepository")
            .get()
    repositories {
        maven { url = uri(pluginRepository) }
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.marmatsan.figmaDocumentationSync") version
            providers
                .gradleProperty("figmaDocumentationSyncVersion")
                .get()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri(providers.gradleProperty("figmaDocumentationSyncPublicationRepository").get())
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "figma-documentation-sync-standalone-consumer"
