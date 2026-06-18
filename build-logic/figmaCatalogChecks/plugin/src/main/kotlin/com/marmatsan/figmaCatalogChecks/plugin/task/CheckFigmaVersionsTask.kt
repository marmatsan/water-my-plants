package com.marmatsan.figmaCatalogChecks.plugin.task

import com.marmatsan.figmaCatalogChecks.plugin.checker.*
import com.marmatsan.figmaCatalogChecks.plugin.di.*

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class CheckFigmaVersionsTask : DefaultTask() {
    @get:Input
    abstract val pageUrl: Property<String>

    @get:Input
    abstract val sectionUrl: Property<String>

    @get:Input
    abstract val versionComponentUrl: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionsFile: RegularFileProperty

    @get:Internal
    abstract val figmaToken: Property<String>

    @TaskAction
    fun checkVersions() {
        val token = figmaToken.orNull
            ?: throw GradleException("Missing FIGMA_FILE_CONTENT_ACCESS_TOKEN environment variable")

        val result = FigmaCatalogChecksComponent::class.create().checker.check(
            FigmaVersionsCheckRequest(
                pageUrl = pageUrl.get(),
                sectionUrl = sectionUrl.get(),
                versionComponentUrl = versionComponentUrl.get(),
                versionsFile = versionsFile.get().asFile,
                token = token
            )
        )

        logger.lifecycle(
            "Figma section '${result.sectionNodeId}' matches ${result.repositoryVersionCount} repository versions."
        )
    }
}
