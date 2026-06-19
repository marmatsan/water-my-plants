package com.marmatsan.figmaCatalogChecks.plugin.task.modules

import com.marmatsan.figmaCatalogChecks.plugin.checker.modules.FigmaModuleDependenciesCheckRequest
import com.marmatsan.figmaCatalogChecks.plugin.di.FigmaCatalogChecksComponent
import com.marmatsan.figmaCatalogChecks.plugin.di.create
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class CheckFigmaModuleDependenciesTask : DefaultTask() {
    @get:Input
    abstract val pageUrl: Property<String>

    @get:Input
    abstract val mainSectionUrl: Property<String>

    @get:Input
    abstract val buildLogicSectionUrl: Property<String>

    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    @get:Internal
    abstract val buildLogicRootDirectory: DirectoryProperty

    @get:Internal
    abstract val figmaToken: Property<String>

    @TaskAction
    fun checkModuleDependencies() {
        val token = figmaToken.orNull
            ?: throw GradleException("Missing FIGMA_FILE_CONTENT_ACCESS_TOKEN environment variable")

        val result = FigmaCatalogChecksComponent::class.create().moduleDependenciesChecker.check(
            FigmaModuleDependenciesCheckRequest(
                pageUrl = pageUrl.get(),
                mainSectionUrl = mainSectionUrl.get(),
                buildLogicSectionUrl = buildLogicSectionUrl.get(),
                projectRootDirectory = projectRootDirectory.get().asFile,
                buildLogicRootDirectory = buildLogicRootDirectory.get().asFile,
                token = token
            )
        )

        logger.lifecycle(
            "Figma module dependency sections match " +
                "${result.mainDependencyCount} main project dependencies and " +
                "${result.buildLogicDependencyCount} build-logic dependencies."
        )
    }
}
