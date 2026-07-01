package com.marmatsan.figmaDesignSync.plugin.gradle


import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

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
     * Build-logic settings file used to discover included-build modules and
     * build-logic catalogs.
     */
    val buildLogicSettingsFile: RegularFileProperty = objects.fileProperty()

    /**
     * Output file for the generated `design-model.json` artifact.
     */
    val designModelFile: RegularFileProperty = objects.fileProperty()

    init {
        versionsFile.convention(layout.projectDirectory.file("build-logic/versions.properties"))
        rootSettingsFile.convention(layout.projectDirectory.file("settings.gradle.kts"))
        buildLogicSettingsFile.convention(layout.projectDirectory.file("build-logic/settings.gradle.kts"))
        designModelFile.convention(layout.buildDirectory.file("reports/figma-sync/design-model.json"))
    }
}
