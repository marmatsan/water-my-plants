package com.marmatsan.projectConfig.settings

import com.marmatsan.dependencies.gradle.DependencyCatalogSettingsExtension
import com.marmatsan.dependencies.gradle.DependencyCatalogSettingsPlugin
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/** Settings entry point for one consumer-owned dependency catalog definition. */
class ProjectConfigSettingsPlugin : Plugin<Settings> {
    /** Registers the `projectConfig` DSL and materializes it after Settings evaluation. */
    override fun apply(
        settings: Settings
    ) {
        settings.pluginManager.apply(DependencyCatalogSettingsPlugin::class.java)
        val dependencyCatalog =
            settings.extensions.getByType(DependencyCatalogSettingsExtension::class.java)
        val extension =
            settings.extensions.create(
                "projectConfig",
                ProjectConfigSettingsExtension::class.java,
                settings.rootDir.resolve("versions.properties")
            )

        ProjectConfigCatalogRegistration(
            settings = settings,
            dependencyCatalog = dependencyCatalog,
            extension = extension
        ).register()
    }
}
