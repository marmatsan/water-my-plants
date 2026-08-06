package com.marmatsan.projectConfig.settings

import com.marmatsan.dependencies.gradle.DependencyCatalogSettingsExtension
import com.marmatsan.projectConfig.catalog.ProjectConfigCatalogState
import org.gradle.api.Action
import org.gradle.api.initialization.Settings

/** Materializes the captured catalog once the consumer has evaluated Settings. */
internal class ProjectConfigCatalogRegistration(
    private val settings: Settings,
    private val dependencyCatalog: DependencyCatalogSettingsExtension,
    private val extension: ProjectConfigSettingsExtension
) : Action<Settings> {
    /** Registers this materializer for the end of Settings evaluation. */
    fun register() {
        settings.gradle.settingsEvaluated(this)
    }

    /** Publishes the catalog views for the Settings instance that owns this registration. */
    override fun execute(
        evaluatedSettings: Settings
    ) {
        if (evaluatedSettings != settings) {
            return
        }
        val provider = extension.provider()
        dependencyCatalog.librariesCatalogName.set(extension.librariesCatalogName)
        dependencyCatalog.pluginsCatalogName.set(extension.pluginsCatalogName)
        dependencyCatalog.from(provider)
        ProjectConfigCatalogState.store(
            gradle = settings.gradle,
            provider = provider
        )
    }
}
