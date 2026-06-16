package com.marmatsan.figmaVersions

import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class FigmaVersionsExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) {
    val fileKey: Property<String> = objects.property(String::class.java)
    val pageName: Property<String> = objects.property(String::class.java)
    val sectionName: Property<String> = objects.property(String::class.java)
    val versionsFile: RegularFileProperty = objects.fileProperty()

    init {
        pageName.convention("🐘 Gradle dependencies")
        sectionName.convention("build-logic\\versions.properties")
        versionsFile.convention(layout.projectDirectory.file("build-logic/versions.properties"))
    }
}
