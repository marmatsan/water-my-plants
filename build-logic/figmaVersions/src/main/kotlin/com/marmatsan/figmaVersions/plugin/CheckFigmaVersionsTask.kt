package com.marmatsan.figmaVersions.plugin

import com.marmatsan.figmaVersions.FigmaFileContentClient
import com.marmatsan.figmaVersions.FigmaFileVersionsReader
import com.marmatsan.figmaVersions.VersionsComparison
import com.marmatsan.figmaVersions.VersionsPropertiesReader
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
    abstract val fileKey: Property<String>

    @get:Input
    abstract val pageName: Property<String>

    @get:Input
    abstract val sectionName: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionsFile: RegularFileProperty

    @get:Internal
    abstract val figmaToken: Property<String>

    @TaskAction
    fun checkVersions() {
        val token = figmaToken.orNull
            ?: throw GradleException("Missing FIGMA_FILE_CONTENT_ACCESS_TOKEN environment variable")

        val repositoryVersions = VersionsPropertiesReader.read(versionsFile.get().asFile)
        val figmaClient = FigmaFileContentClient()
        val figmaResponse = figmaClient.getFileContent(
            fileKey = fileKey.get(),
            token = token,
            depth = 2
        )
        val section = FigmaFileVersionsReader.findSection(
            response = figmaResponse,
            pageName = pageName.get(),
            sectionName = sectionName.get()
        )
        val sectionContent = figmaClient.getNodeContent(
            fileKey = fileKey.get(),
            token = token,
            nodeId = section.id
        )
        val figmaVersions = FigmaFileVersionsReader.readSection(
            section = sectionContent,
            sectionName = sectionName.get()
        )
        val result = VersionsComparison.compare(
            repositoryVersions = repositoryVersions,
            figmaVersions = figmaVersions
        )

        if (!result.matches) {
            throw GradleException(result.report())
        }

        logger.lifecycle("Figma section '${sectionName.get()}' matches ${repositoryVersions.size} repository versions.")
    }
}
