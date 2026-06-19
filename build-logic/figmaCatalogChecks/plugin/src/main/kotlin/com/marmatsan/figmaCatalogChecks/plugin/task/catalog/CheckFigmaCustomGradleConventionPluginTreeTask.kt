package com.marmatsan.figmaCatalogChecks.plugin.task.catalog

import com.marmatsan.figmaCatalogChecks.plugin.checker.catalog.FigmaCatalogTreeCheckRequest
import com.marmatsan.figmaCatalogChecks.plugin.di.FigmaCatalogChecksComponent
import com.marmatsan.figmaCatalogChecks.plugin.di.create
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class CheckFigmaCustomGradleConventionPluginTreeTask : DefaultTask() {
    @get:Input
    abstract val pageUrl: Property<String>

    @get:Input
    abstract val sectionUrl: Property<String>

    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    @get:Internal
    abstract val figmaToken: Property<String>

    @TaskAction
    fun checkPluginTree() {
        val token = figmaToken.orNull
            ?: throw GradleException("Missing FIGMA_FILE_CONTENT_ACCESS_TOKEN environment variable")

        val result = FigmaCatalogChecksComponent::class.create()
            .catalogTreeChecker
            .checkCustomGradleConventionPluginTree(
                FigmaCatalogTreeCheckRequest(
                    pageUrl = pageUrl.get(),
                    sectionUrl = sectionUrl.get(),
                    projectRootDir = projectRootDirectory.get().asFile,
                    token = token
                )
        )

        logger.lifecycle(
            "Figma section 'Custom Gradle Convention Plugins' matches " +
                "${result.repositoryNodeCount} custom Gradle convention plugin nodes."
        )
    }
}
