package com.marmatsan.dependencies.gradle

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create

/** Registers one repository-owned dependency tree as Gradle version catalogs. */
class DependencyCatalogSettingsPlugin : Plugin<Settings> {
    override fun apply(
        settings: Settings,
    ) {
        val extension =
            settings.extensions.create<DependencyCatalogSettingsExtension>(
                "dependencyCatalog",
            )
        extension.registerCatalogs = { provider, librariesCatalogName, pluginsCatalogName ->
            settings.dependencyResolutionManagement {
                configureVersionCatalogs(
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
}
