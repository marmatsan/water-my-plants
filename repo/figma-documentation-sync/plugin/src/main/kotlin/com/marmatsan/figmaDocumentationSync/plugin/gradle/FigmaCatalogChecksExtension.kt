package com.marmatsan.figmaDocumentationSync.plugin.gradle

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.register
import javax.inject.Inject

/**
 * Included build that contributes data to `design-model.json`.
 */
abstract class FigmaDocumentationSyncIncludedBuild
    @Inject
    constructor(
        private val buildName: String,
        objects: ObjectFactory
    ) : Named {
        /**
         * JSON object name used inside `content.catalogs` and
         * `content.moduleDependencies`.
         */
        val modelName: Property<String> = objects.property(String::class.java).convention(buildName)

        /**
         * Included-build settings file used to discover modules and catalogs.
         */
        val settingsFile: RegularFileProperty = objects.fileProperty()

        /**
         * Included-build root directory used to scan build files.
         */
        val rootDirectory: DirectoryProperty = objects.directoryProperty()

        /**
         * Logical Gradle path prefix assigned to modules from this included build.
         */
        val modulePathPrefix: Property<String> = objects.property(String::class.java).convention(":$buildName")

        /**
         * Whether this included build's `libs` and `plugins` catalogs belong in
         * the host's generated documentation model. Local tool catalogs stay
         * private by default even when the settings file declares them.
         */
        val publishesCatalogs: Property<Boolean> =
            objects
                .property(Boolean::class.javaObjectType)
                .convention(false)

        /**
         * Whether this included build publishes repository convention plugins.
         */
        val publishesConventionPlugins: Property<Boolean> =
            objects
                .property(Boolean::class.javaObjectType)
                .convention(false)

        /** Returns the stable Gradle container name of this included build. */
        override fun getName(): String = buildName
    }

/**
 * Gradle extension for configuring the Figma design sync plugin.
 *
 * Portable defaults cover only conventional root and output locations.
 * Repository identity, catalog adapters, Figma nodes, visual tooling, CI
 * adapters, and included builds must be supplied by a project-config plugin.
 */
@Suppress("ktlint:standard:class-naming")
abstract class figmaDocumentationSyncExtension
    @Inject
    constructor(
        objects: ObjectFactory,
        layout: ProjectLayout
    ) {
        /**
         * Figma design URL pointing to the metadata node that stores sync plugin
         * data such as `modelHash`.
         */
        val designModelMetadataNodeUrl: Property<String> = objects.property(String::class.java)

        /** Shared plugin-data namespace used for Figma sync metadata. */
        val metadataNamespace: Property<String> = objects.property(String::class.java)

        /** JSON key used for the repository's primary dependency catalog. */
        val primaryCatalogModelName: Property<String> = objects.property(String::class.java)

        /** Legacy project adapter implementing the portable dependency catalog contract. */
        val dependencyCatalogProviderClassName: Property<String> = objects.property(String::class.java)

        /** Dependency catalog trees serialized by a reusable project-config adapter. */
        val dependencyCatalogTreesJson: Property<String> = objects.property(String::class.java)

        /** Whether this project publishes the optional CI documentation model. */
        val ciDocumentationEnabled: Property<Boolean> =
            objects
                .property(Boolean::class.javaObjectType)
                .convention(false)

        /** Stable JSON key used for the effective CI configuration. */
        val ciConfigurationModelName: Property<String> = objects.property(String::class.java)

        /** Project adapter implementing the generated CI configuration contract. */
        val ciConfigurationProviderClassName: Property<String> = objects.property(String::class.java)

        /** Provider-specific branch label that represents the repository default branch. */
        val ciDefaultBranchAlias: Property<String> = objects.property(String::class.java)

        /**
         * Version declarations used by the generated model.
         */
        val versionsFile: RegularFileProperty = objects.fileProperty()

        /**
         * Root Gradle settings file used to discover project modules.
         */
        val rootSettingsFile: RegularFileProperty = objects.fileProperty()

        /**
         * Versioned external systems and connections rendered in CI documentation.
         */
        val ciExternalTopologyFile: RegularFileProperty = objects.fileProperty()

        /**
         * Versioned Windows services rendered in CI documentation.
         */
        val ciWindowsRuntimeFile: RegularFileProperty = objects.fileProperty()

        /** Effective configuration directory read by the selected CI adapter. */
        val ciGeneratedConfigurationDirectory: DirectoryProperty = objects.directoryProperty()

        /** Optional CI adapter command that materializes effective configuration. */
        val ciConfigurationCommand: ListProperty<String> = objects.listProperty(String::class.java)

        /** Working directory used by [ciConfigurationCommand]. */
        val ciConfigurationWorkingDirectory: DirectoryProperty = objects.directoryProperty()

        /** TypeScript package containing the portable Figma writer. */
        val toolsDirectory: DirectoryProperty = objects.directoryProperty()

        /** Serialized writer configuration supplied by the consuming project composition root. */
        val writerProjectConfigJson: Property<String> = objects.property(String::class.java)

        /**
         * Included builds that contribute repository model data.
         */
        val includedBuilds: NamedDomainObjectContainer<FigmaDocumentationSyncIncludedBuild> =
            objects.domainObjectContainer(FigmaDocumentationSyncIncludedBuild::class.java) { buildName ->
                objects.newInstance(
                    FigmaDocumentationSyncIncludedBuild::class.java,
                    buildName
                )
            }

        /**
         * Output file for the generated `design-model.json` artifact.
         */
        val designModelFile: RegularFileProperty = objects.fileProperty()

        /**
         * Versioned path policy used to classify repository changes for Figma.
         */
        val changeImpactPolicyFile: RegularFileProperty = objects.fileProperty()

        /**
         * Machine-readable output produced by `classifyFigmaChangeImpact`.
         */
        val changeImpactFile: RegularFileProperty = objects.fileProperty()

        init {
            rootSettingsFile.convention(layout.projectDirectory.file("settings.gradle.kts"))
            designModelFile.convention(layout.buildDirectory.file("reports/figma-sync/design-model.json"))
            changeImpactFile.convention(layout.buildDirectory.file("reports/figma-sync/change-impact.json"))
            ciConfigurationCommand.convention(emptyList())
            ciConfigurationWorkingDirectory.convention(layout.projectDirectory)
        }
    }
