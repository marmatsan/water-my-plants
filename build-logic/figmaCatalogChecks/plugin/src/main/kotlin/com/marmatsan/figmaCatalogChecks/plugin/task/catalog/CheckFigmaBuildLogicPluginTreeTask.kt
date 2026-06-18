package com.marmatsan.figmaCatalogChecks.plugin.task.catalog

import com.marmatsan.figmaCatalogChecks.plugin.checker.catalog.FigmaBuildLogicCatalogTreeCheckRequest
import com.marmatsan.figmaCatalogChecks.plugin.di.FigmaCatalogChecksComponent
import com.marmatsan.figmaCatalogChecks.plugin.di.create
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class CheckFigmaBuildLogicPluginTreeTask : DefaultTask() {
    @get:Input
    abstract val pageUrl: Property<String>

    @get:Input
    abstract val sectionUrl: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val settingsFile: RegularFileProperty

    @get:Internal
    abstract val figmaToken: Property<String>

    @TaskAction
    fun checkPluginTree() {
        val token = figmaToken.orNull
            ?: throw GradleException("Missing FIGMA_FILE_CONTENT_ACCESS_TOKEN environment variable")

        val result = FigmaCatalogChecksComponent::class.create().catalogTreeChecker.checkBuildLogicPluginTree(
            FigmaBuildLogicCatalogTreeCheckRequest(
                pageUrl = pageUrl.get(),
                sectionUrl = sectionUrl.get(),
                settingsFile = settingsFile.get().asFile,
                token = token
            )
        )

        logger.lifecycle(
            "Figma section '${result.sectionNodeId}' matches ${result.repositoryNodeCount} build-logic plugin nodes."
        )
    }
}
