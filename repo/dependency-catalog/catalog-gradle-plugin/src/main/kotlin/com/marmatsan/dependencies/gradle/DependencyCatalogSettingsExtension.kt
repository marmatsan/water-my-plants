package com.marmatsan.dependencies.gradle

import com.marmatsan.dependencies.catalog.api.ResolvedDependencyCatalogProvider
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/** Configuration for the portable dependency catalog settings plugin. */
abstract class DependencyCatalogSettingsExtension
    @Inject
    constructor(
        objects: ObjectFactory
    ) {
        internal lateinit var registerCatalogs: (ResolvedDependencyCatalogProvider, String, String) -> Unit
        private var catalogsRegistered = false

        /**
         * Registers the trees returned by [provider]. Call this after configuring the optional
         * catalog names because Gradle must receive version catalogs while evaluating settings.
         */
        fun from(
            provider: ResolvedDependencyCatalogProvider
        ) {
            check(!catalogsRegistered) {
                "dependencyCatalog.from(provider) can only be called once"
            }
            registerCatalogs(
                provider,
                librariesCatalogName.get(),
                pluginsCatalogName.get()
            )
            catalogsRegistered = true
        }

        /** Name of the generated library version catalog. */
        val librariesCatalogName: Property<String> =
            objects
                .property(String::class.java)
                .convention("libs")

        /** Name of the generated plugin version catalog. */
        val pluginsCatalogName: Property<String> =
            objects
                .property(String::class.java)
                .convention("plugins")
    }
