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

    versionCatalogs {
        create("plugins") {
            plugin(
                "com.marmatsan.figmaDocumentationSync",
                "com.marmatsan.figmaDocumentationSync",
            ).version(
                providers.gradleProperty("figmaDocumentationSyncVersion").get(),
            )
        }
    }
}

rootProject.name = "figma-documentation-sync-standalone-consumer"
