pluginManagement {
    val pluginRepository = providers
        .gradleProperty("figmaDesignSyncPublicationRepository")
        .get()
    val catalogRepository = providers
        .gradleProperty("figmaDesignSyncCatalogPublicationRepository")
        .getOrElse(pluginRepository)

    repositories {
        maven { url = uri(pluginRepository) }
        maven { url = uri(catalogRepository) }
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("com.marmatsan.figmaDesignSync") version providers
            .gradleProperty("figmaDesignSyncVersion")
            .get()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri(providers.gradleProperty("figmaDesignSyncPublicationRepository").get())
        }
        maven {
            url = uri(
                providers
                    .gradleProperty("figmaDesignSyncCatalogPublicationRepository")
                    .orElse(providers.gradleProperty("figmaDesignSyncPublicationRepository"))
                    .get()
            )
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "figma-design-sync-standalone-consumer"
