package com.marmatsan.dependencies.gradle.tree

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import com.marmatsan.dependencies.gradle.tree.dsl.LibraryCatalogTreesScope
import com.marmatsan.dependencies.gradle.tree.dsl.PluginCatalogTreesScope
import com.marmatsan.dependencies.gradle.tree.mapping.DependencyCatalogTreeApiMapper
import com.marmatsan.dependencies.gradle.tree.provider.TreeResolvedDependencyCatalogProvider
import com.marmatsan.dependencies.gradle.tree.version.PropertiesDependencyVersionResolver
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
        defaultVersionsFile: File,
    ) {
        private val libraryTrees = LibraryCatalogTreesScope()
        private val pluginTrees = PluginCatalogTreesScope()
        private val versionResolver = PropertiesDependencyVersionResolver()

        internal lateinit var registerCatalog: (TreeResolvedDependencyCatalogProvider, String, String) -> Unit

        /**
         * Version registry owned by the consuming build.
         *
         * The default is `versions.properties` in the settings directory. [version] reads values
         * from this file and fixes its canonical path on the first lookup, so configure this
         * property before declaring any versioned artifact or plugin.
         */
        val versionsFile: RegularFileProperty =
            objects.fileProperty().fileValue(defaultVersionsFile)

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
            content: LibraryCatalogTreesScope.() -> Unit,
        ) {
            libraryTrees.content()
        }

        /**
         * Adds Gradle plugin id trees to the catalog named by [pluginsCatalogName].
         *
         * Multiple calls accumulate declarations. Root values must remain unique across all calls.
         *
         * @param content Plugin roots and their relative id paths.
         */
        fun plugins(
            content: PluginCatalogTreesScope.() -> Unit,
        ) {
            pluginTrees.content()
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
            key: String,
        ): String =
            versionResolver.resolve(
                file = versionsFile.get().asFile,
                key = key,
            )

        internal fun register() {
            val trees =
                DependencyCatalogTrees(
                    libraries = libraryTrees.values(),
                    plugins = pluginTrees.values(),
                )
            require(trees.libraries.isNotEmpty() || trees.plugins.isNotEmpty()) {
                "Tree dependency catalog must declare at least one library or plugin root"
            }
            registerCatalog(
                TreeResolvedDependencyCatalogProvider(
                    catalog = DependencyCatalogTreeApiMapper().map(trees),
                ),
                librariesCatalogName.get(),
                pluginsCatalogName.get(),
            )
        }
    }
