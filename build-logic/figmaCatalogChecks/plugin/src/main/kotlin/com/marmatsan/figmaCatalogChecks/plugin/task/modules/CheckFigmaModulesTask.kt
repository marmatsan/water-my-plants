package com.marmatsan.figmaCatalogChecks.plugin.task.modules

import com.marmatsan.figmaCatalogChecks.plugin.checker.modules.FigmaModulesCheckRequest
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

abstract class CheckFigmaModulesTask : DefaultTask() {
    @get:Input
    abstract val pageUrl: Property<String>

    @get:Input
    abstract val moduleComponentUrl: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootSettingsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val buildLogicSettingsFile: RegularFileProperty

    @get:Internal
    abstract val figmaToken: Property<String>

    @TaskAction
    fun checkModules() {
        val token = figmaToken.orNull
            ?: throw GradleException("Missing FIGMA_FILE_CONTENT_ACCESS_TOKEN environment variable")

        val result = FigmaCatalogChecksComponent::class.create().modulesChecker.check(
            FigmaModulesCheckRequest(
                pageUrl = pageUrl.get(),
                moduleComponentUrl = moduleComponentUrl.get(),
                rootSettingsFile = rootSettingsFile.get().asFile,
                buildLogicSettingsFile = buildLogicSettingsFile.get().asFile,
                token = token
            )
        )

        logger.lifecycle(
            "Figma .module component '${result.moduleComponentNodeId}' matches ${result.repositoryModuleCount} repository modules."
        )
    }
}
