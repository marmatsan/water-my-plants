package com.marmatsan.dependencies.gradle.tree

import com.marmatsan.dependencies.catalog.dsl.DependencyCatalogTreesBuilder
import com.marmatsan.dependencies.catalog.dsl.LibraryCatalogTreesScope
import com.marmatsan.dependencies.catalog.dsl.PluginCatalogTreesScope
import com.marmatsan.dependencies.catalog.mapping.toDependencyCatalog
import com.marmatsan.dependencies.catalog.version.PropertiesDependencyVersionResolver
import com.marmatsan.dependencies.gradle.tree.provider.TreeResolvedDependencyCatalogProvider
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

/**
 * Settings DSL for building Gradle version catalogs from reusable dependency trees.
 *
 * Version ownership remains local to the consuming build through [versionsFile]. Library and
 * plugin declarations are collected independently and registered when settings evaluation ends.
 */
abstract class TreeDependencyCatalogSettingsExtension
    @Inject
    constructor(
        objects: ObjectFactory,
        defaultVersionsFile: File
    ) {
        /**
         * Version registry owned by the consuming build.
         *
         * The default is `versions.properties` in the settings directory. [version] reads values
         * from this file and fixes its canonical path on the first lookup, so configure this
         * property before declaring any versioned artifact or plugin.
         */
        val versionsFile: RegularFileProperty =
            objects.fileProperty().fileValue(defaultVersionsFile)

        private val catalogBuilder =
            DependencyCatalogTreesBuilder(
                versionResolver =
                    PropertiesDependencyVersionResolver(
                        source = { versionsFile.get().asFile }
                    )
            )

        internal lateinit var registerCatalog: (TreeResolvedDependencyCatalogProvider, String, String) -> Unit

        /**
         * Name of the generated Gradle library version catalog.
         *
         * The default is `libs`. Change it before settings evaluation completes when the consumer
         * already owns that name or needs a more specific generated accessor.
         */
        val librariesCatalogName: Property<String> =
            objects.property(String::class.java).convention("libs")

        /**
         * Name of the generated Gradle plugin version catalog.
         *
         * The default is `plugins`. Change it before settings evaluation completes when the
         * consumer needs a different generated accessor.
         */
        val pluginsCatalogName: Property<String> =
            objects.property(String::class.java).convention("plugins")

        /**
         * Adds Maven group trees to the catalog named by [librariesCatalogName].
         *
         * Multiple calls accumulate declarations. Root values must remain unique across all calls.
         *
         * @param content Library roots and their relative group paths.
         */
        fun libraries(
            content: LibraryCatalogTreesScope.() -> Unit
        ) {
            catalogBuilder.libraries(
                content = content
            )
        }

        /**
         * Adds Gradle plugin id trees to the catalog named by [pluginsCatalogName].
         *
         * Multiple calls accumulate declarations. Root values must remain unique across all calls.
         *
         * @param content Plugin roots and their relative id paths.
         */
        fun plugins(
            content: PluginCatalogTreesScope.() -> Unit
        ) {
            catalogBuilder.plugins(
                content = content
            )
        }

        /**
         * Resolves one version value by its exact property [key] in [versionsFile].
         *
         * The file is loaded once per settings evaluation. Subsequent lookups reuse the loaded
         * properties and must target the same canonical file.
         *
         * @param key Case-sensitive property name to resolve.
         * @return Non-null version text stored for [key].
         * @throws IllegalArgumentException when the configured file does not exist or changes after
         * the first lookup.
         * @throws IllegalStateException when [key] is absent.
         */
        fun version(
            key: String
        ): String =
            catalogBuilder.version(
                key = key
            )

        internal fun register() {
            val trees = catalogBuilder.build()
            registerCatalog(
                TreeResolvedDependencyCatalogProvider(
                    catalog = trees.toDependencyCatalog()
                ),
                librariesCatalogName.get(),
                pluginsCatalogName.get()
            )
        }
    }
