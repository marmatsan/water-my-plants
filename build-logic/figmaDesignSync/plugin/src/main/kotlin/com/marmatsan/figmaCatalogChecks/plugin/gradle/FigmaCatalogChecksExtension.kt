package com.marmatsan.figmaDesignSync.plugin.gradle


import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class figmaDesignSyncExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) {
    val designModelMetadataNodeUrl: Property<String> = objects.property(String::class.java)
    val versionsFile: RegularFileProperty = objects.fileProperty()
    val rootSettingsFile: RegularFileProperty = objects.fileProperty()
    val buildLogicSettingsFile: RegularFileProperty = objects.fileProperty()
    val designModelFile: RegularFileProperty = objects.fileProperty()

    init {
        versionsFile.convention(layout.projectDirectory.file("build-logic/versions.properties"))
        rootSettingsFile.convention(layout.projectDirectory.file("settings.gradle.kts"))
        buildLogicSettingsFile.convention(layout.projectDirectory.file("build-logic/settings.gradle.kts"))
        designModelFile.convention(layout.buildDirectory.file("reports/figma-sync/design-model.json"))
    }
}
