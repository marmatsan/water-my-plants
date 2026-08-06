package com.marmatsan.figmaDocumentationSync.teamcity.operations.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/** Consumer-owned configuration for the optional TeamCity Figma operations. */
abstract class FigmaTeamCityOperationsExtension
    @Inject
    constructor(
        objects: ObjectFactory,
        layout: ProjectLayout
    ) {
        /** TeamCity build configuration that owns the canonical Figma Sync pipeline. */
        val buildTypeId: Property<String> = objects.property(String::class.java)

        /** Canonical branch on which the pipeline may be reused or queued. */
        val branch: Property<String> = objects.property(String::class.java)

        /** TeamCity branch spellings accepted as the canonical branch in downloaded artifacts. */
        val mainBranchAliases: ListProperty<String> = objects.listProperty(String::class.java)

        /** Build configuration display-name fragment allowed to publish canonical artifacts. */
        val requiredBuildTypeName: Property<String> = objects.property(String::class.java)

        /** Public HTTPS TeamCity origin used by supervised rerun operations. */
        val serverUrl: Property<String> = objects.property(String::class.java)

        /** Safe local root beneath which downloaded artifacts may be expanded. */
        val destinationRoot: DirectoryProperty = objects.directoryProperty()

        init {
            destinationRoot.convention(layout.projectDirectory.dir("tmp/teamcity"))
        }
    }
