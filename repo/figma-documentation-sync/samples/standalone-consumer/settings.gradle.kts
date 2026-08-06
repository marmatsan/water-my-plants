pluginManagement {
    val pluginRepository: String =
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
    versionCatalogs {
        create("plugins") {
            plugin(
                "com.marmatsan.figmaDocumentationSync",
                "com.marmatsan.figmaDocumentationSync"
            ).version(
                providers.gradleProperty("figmaDocumentationSyncVersion").get()
            )
            plugin(
                "com.marmatsan.figmaDocumentationSync.teamcityOperations",
                "com.marmatsan.figmaDocumentationSync.teamcityOperations"
            ).version(
                providers.gradleProperty("figmaDocumentationSyncVersion").get()
            )
        }
    }
}

rootProject.name = "figma-documentation-sync-standalone-consumer"
