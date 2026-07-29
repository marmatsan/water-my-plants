package com.marmatsan.dependencies.gradle.tree

import com.marmatsan.dependencies.gradle.DependencyCatalogSettingsExtension
import com.marmatsan.dependencies.gradle.DependencyCatalogSettingsPlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/**
 * Registers the `dependencyCatalogTree` settings extension and its generated version catalogs.
 *
 * The plugin delegates Gradle catalog registration to the provider-based settings plugin and
 * materializes all collected trees after settings evaluation. A consuming build therefore only
 * configures [TreeDependencyCatalogSettingsExtension]; it does not call a terminal registration
 * operation.
 */
class TreeDependencyCatalogSettingsPlugin : Plugin<Settings> {
    /**
     * Adds `dependencyCatalogTree` to [settings] and schedules automatic catalog registration.
     *
     * @param settings Consumer settings that own declarations, versions, and generated catalogs.
     */
    override fun apply(
        settings: Settings
    ) {
        settings.pluginManager.apply(DependencyCatalogSettingsPlugin::class.java)
        val providerExtension =
            settings.extensions.getByType(DependencyCatalogSettingsExtension::class.java)
        val treeExtension =
            settings.extensions.create(
                "dependencyCatalogTree",
                TreeDependencyCatalogSettingsExtension::class.java,
                settings.rootDir.resolve("versions.properties")
            )
        treeExtension.registerCatalog = { provider, librariesCatalogName, pluginsCatalogName ->
            providerExtension.librariesCatalogName.set(librariesCatalogName)
            providerExtension.pluginsCatalogName.set(pluginsCatalogName)
            providerExtension.from(provider)
        }

        settings.gradle.settingsEvaluated(
            object : Action<Settings> {
                override fun execute(
                    evaluatedSettings: Settings
                ) {
                    if (evaluatedSettings == settings) {
                        treeExtension.register()
                    }
                }
            }
        )
    }
}
