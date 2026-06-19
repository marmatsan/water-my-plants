package com.marmatsan.figmaCatalogChecks.plugin.gradle


import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class FigmaCatalogChecksExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) {
    val pageUrl: Property<String> = objects.property(String::class.java)
    val sectionUrl: Property<String> = objects.property(String::class.java)
    val libraryTreeSectionUrl: Property<String> = objects.property(String::class.java)
    val pluginTreeSectionUrl: Property<String> = objects.property(String::class.java)
    val customGradleConventionPluginTreeSectionUrl: Property<String> = objects.property(String::class.java)
    val buildLogicLibraryTreeSectionUrl: Property<String> = objects.property(String::class.java)
    val buildLogicPluginTreeSectionUrl: Property<String> = objects.property(String::class.java)
    val mainModuleDependenciesSectionUrl: Property<String> = objects.property(String::class.java)
    val buildLogicModuleDependenciesSectionUrl: Property<String> = objects.property(String::class.java)
    val versionComponentUrl: Property<String> = objects.property(String::class.java)
    val moduleComponentUrl: Property<String> = objects.property(String::class.java)
    val versionsFile: RegularFileProperty = objects.fileProperty()
    val rootSettingsFile: RegularFileProperty = objects.fileProperty()
    val buildLogicSettingsFile: RegularFileProperty = objects.fileProperty()

    init {
        versionsFile.convention(layout.projectDirectory.file("build-logic/versions.properties"))
        rootSettingsFile.convention(layout.projectDirectory.file("settings.gradle.kts"))
        buildLogicSettingsFile.convention(layout.projectDirectory.file("build-logic/settings.gradle.kts"))
    }
}
