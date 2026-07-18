package com.marmatsan.figmaDesignSync.plugin.gradle

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.register
import javax.inject.Inject

/**
 * Included build that contributes data to `design-model.json`.
 */
abstract class FigmaDesignSyncIncludedBuild @Inject constructor(
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
     * Whether this included build declares `libs` and `plugins` catalogs in
     * its settings file.
     */
    val publishesCatalogs: Property<Boolean> = objects
        .property(Boolean::class.javaObjectType)
        .convention(true)

    /**
     * Whether this included build publishes repository convention plugins.
     */
    val publishesConventionPlugins: Property<Boolean> = objects
        .property(Boolean::class.javaObjectType)
        .convention(false)

    override fun getName(): String = buildName
}

/**
 * Gradle extension for configuring the Figma design sync plugin.
 *
 * Defaults assume the plugin is applied to the repository root project. A build
 * can override these properties when the repository layout or Figma metadata
 * node changes.
 */
abstract class figmaDesignSyncExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) {
    /**
     * Figma design URL pointing to the metadata node that stores sync plugin
     * data such as `modelHash`.
     */
    val designModelMetadataNodeUrl: Property<String> = objects.property(String::class.java)

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

    /**
     * Effective TeamCity configuration generated from the versioned Kotlin DSL.
     */
    val teamCityGeneratedConfigurationDirectory: DirectoryProperty = objects.directoryProperty()

    /**
     * Included builds that contribute repository model data.
     */
    val includedBuilds: NamedDomainObjectContainer<FigmaDesignSyncIncludedBuild> =
        objects.domainObjectContainer(FigmaDesignSyncIncludedBuild::class.java) { buildName ->
            objects.newInstance(FigmaDesignSyncIncludedBuild::class.java, buildName)
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
        versionsFile.convention(layout.projectDirectory.file("repo/dependency-catalog/versions.properties"))
        rootSettingsFile.convention(layout.projectDirectory.file("settings.gradle.kts"))
        ciExternalTopologyFile.convention(layout.projectDirectory.file("docs/ci/external-topology.yaml"))
        ciWindowsRuntimeFile.convention(layout.projectDirectory.file("docs/ci/windows-runtime.yaml"))
        teamCityGeneratedConfigurationDirectory.convention(
            layout.projectDirectory.dir(".teamcity/target/generated-configs")
        )
        designModelFile.convention(layout.buildDirectory.file("reports/figma-sync/design-model.json"))
        changeImpactPolicyFile.convention(
            layout.projectDirectory.file("repo/figma-design-sync/change-impact-policy.json")
        )
        changeImpactFile.convention(layout.buildDirectory.file("reports/figma-sync/change-impact.json"))

        includedBuilds.register("dependency-catalog") {
            modelName.convention("dependencyCatalog")
            settingsFile.convention(layout.projectDirectory.file("repo/dependency-catalog/settings.gradle.kts"))
            rootDirectory.convention(layout.projectDirectory.dir("repo/dependency-catalog"))
            modulePathPrefix.convention(":dependency-catalog")
            publishesCatalogs.convention(false)
        }

        includedBuilds.register("figma-design-sync") {
            modelName.convention("figmaDesignSync")
            settingsFile.convention(layout.projectDirectory.file("repo/figma-design-sync/settings.gradle.kts"))
            rootDirectory.convention(layout.projectDirectory.dir("repo/figma-design-sync"))
            modulePathPrefix.convention(":figma-design-sync")
        }

        includedBuilds.register("gradle-plugins") {
            modelName.convention("gradlePlugins")
            settingsFile.convention(layout.projectDirectory.file("repo/gradle-plugins/settings.gradle.kts"))
            rootDirectory.convention(layout.projectDirectory.dir("repo/gradle-plugins"))
            modulePathPrefix.convention(":gradle-plugins")
            publishesConventionPlugins.convention(true)
        }
    }
}
