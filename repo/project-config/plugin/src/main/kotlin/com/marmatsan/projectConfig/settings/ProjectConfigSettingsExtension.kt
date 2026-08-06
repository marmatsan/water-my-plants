package com.marmatsan.projectConfig.settings

import com.marmatsan.dependencies.catalog.definition.DependencyCatalogDefinitionProvider
import com.marmatsan.dependencies.catalog.definition.dependencyCatalogDefinition
import com.marmatsan.dependencies.catalog.dsl.DependencyCatalogTreesBuilder
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

/** Consumer-owned Settings configuration for the reusable project composition boundary. */
abstract class ProjectConfigSettingsExtension
    @Inject
    constructor(
        objects: ObjectFactory,
        defaultVersionsFile: File
    ) {
        /** Version registry used when Gradle materializes the resolved catalogs. */
        val versionsFile: RegularFileProperty =
            objects.fileProperty().fileValue(defaultVersionsFile)

        /** Name of the generated library version catalog. */
        val librariesCatalogName: Property<String> =
            objects.property(String::class.java).convention("libs")

        /** Name of the generated plugin version catalog. */
        val pluginsCatalogName: Property<String> =
            objects.property(String::class.java).convention("plugins")

        private val declarations = mutableListOf<DependencyCatalogTreesBuilder.() -> Unit>()

        /**
         * Captures library and plugin roots through the canonical dependency-tree builder.
         *
         * Keeping this receiver around the nested declarations ensures that `version("key")`
         * is evaluated by the selected resolved or aliased version strategy.
         */
        fun dependencyCatalog(
            content: DependencyCatalogTreesBuilder.() -> Unit
        ) {
            declarations += content
        }

        /** Materializes both resolved and aliased views of the captured tree. */
        internal fun provider(): DependencyCatalogDefinitionProvider {
            val definition =
                dependencyCatalogDefinition {
                    declarations.forEach { declaration ->
                        declaration()
                    }
                }
            val configuredVersionsFile = versionsFile.get().asFile
            return DependencyCatalogDefinitionProvider(
                definition = definition,
                versionsFile = { configuredVersionsFile }
            )
        }
    }
