package com.marmatsan.figmaCatalogChecks.plugin

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
    val versionComponentUrl: Property<String> = objects.property(String::class.java)
    val versionsFile: RegularFileProperty = objects.fileProperty()

    init {
        versionsFile.convention(layout.projectDirectory.file("build-logic/versions.properties"))
    }
}
