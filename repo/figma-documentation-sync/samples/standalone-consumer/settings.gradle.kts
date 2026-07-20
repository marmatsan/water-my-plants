pluginManagement {
    val pluginRepository =
        providers
            .gradleProperty("figmaDocumentationSyncPublicationRepository")
            .get()
    val catalogRepository =
        providers
            .gradleProperty("figmaDocumentationSyncCatalogPublicationRepository")
            .getOrElse(pluginRepository)

    repositories {
        maven { url = uri(pluginRepository) }
        maven { url = uri(catalogRepository) }
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
        maven {
            url =
                uri(
                    providers
                        .gradleProperty("figmaDocumentationSyncCatalogPublicationRepository")
                        .orElse(providers.gradleProperty("figmaDocumentationSyncPublicationRepository"))
                        .get(),
                )
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "figma-documentation-sync-standalone-consumer"
