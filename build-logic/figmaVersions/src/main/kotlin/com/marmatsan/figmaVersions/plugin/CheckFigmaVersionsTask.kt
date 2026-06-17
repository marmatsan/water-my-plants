package com.marmatsan.figmaVersions.plugin

import com.marmatsan.figmaVersions.FigmaFileContentClient
import com.marmatsan.figmaVersions.FigmaFileVersionsReader
import com.marmatsan.figmaVersions.FigmaNodeUrl
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

        val repositoryVersions = VersionsPropertiesReader.read(versionsFile.get().asFile)
        val page = FigmaNodeUrl.parse(pageUrl.get())
        val section = FigmaNodeUrl.parse(sectionUrl.get())
        val versionComponent = FigmaNodeUrl.parse(versionComponentUrl.get())

        validateSameFile(
            page = page,
            section = section,
            versionComponent = versionComponent
        )

        val figmaClient = FigmaFileContentClient()
        val sectionContent = figmaClient.getNodeContent(
            fileKey = section.fileKey,
            token = token,
            nodeId = section.nodeId
        )
        val figmaVersions = FigmaFileVersionsReader.readSection(
            section = sectionContent,
            sectionNodeId = section.nodeId,
            versionComponentNodeId = versionComponent.nodeId
        )
        val result = VersionsComparison.compare(
            repositoryVersions = repositoryVersions,
            figmaVersions = figmaVersions
        )

        if (!result.matches) {
            throw GradleException(result.report())
        }

        logger.lifecycle("Figma section '${section.nodeId}' matches ${repositoryVersions.size} repository versions.")
    }

    private fun validateSameFile(
        page: FigmaNodeUrl,
        section: FigmaNodeUrl,
        versionComponent: FigmaNodeUrl
    ) {
        val fileKeys = setOf(
            page.fileKey,
            section.fileKey,
            versionComponent.fileKey
        )

        if (fileKeys.size != 1) {
            throw GradleException(
                "Figma URLs must point to the same file. Found file keys: ${fileKeys.sorted().joinToString()}"
            )
        }
    }
}
