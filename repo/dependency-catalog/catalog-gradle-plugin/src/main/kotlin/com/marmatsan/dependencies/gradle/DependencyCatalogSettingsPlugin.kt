package com.marmatsan.dependencies.gradle

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/** Registers one repository-owned dependency tree as Gradle version catalogs. */
class DependencyCatalogSettingsPlugin : Plugin<Settings> {
    /**
     * Adds the `dependencyCatalog` extension to [settings].
     *
     * @param settings Consumer settings that own the generated catalogs.
     */
    override fun apply(
        settings: Settings,
    ) {
        val extension =
            settings.extensions.create(
                "dependencyCatalog",
                DependencyCatalogSettingsExtension::class.java,
            )
        extension.registerCatalogs = { provider, librariesCatalogName, pluginsCatalogName ->
            settings.dependencyResolutionManagement.configureVersionCatalogs(
                catalog =
                    provider.resolved(
                        rootDir = settings.rootDir,
                    ),
                librariesCatalogName = librariesCatalogName,
                pluginsCatalogName = pluginsCatalogName,
            )
        }
    }
}
