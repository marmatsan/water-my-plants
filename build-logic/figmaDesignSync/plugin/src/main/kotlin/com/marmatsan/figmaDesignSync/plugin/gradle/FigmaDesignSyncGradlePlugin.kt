package com.marmatsan.figmaDesignSync.plugin.gradle

import com.marmatsan.figmaDesignSync.plugin.task.generate.GenerateFigmaDesignModelTask
import com.marmatsan.figmaDesignSync.plugin.task.sync.CheckFigmaTrunkSyncTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * Registers Gradle tasks that generate and verify the Figma design model.
 *
 * Apply plugin id `com.marmatsan.figmaDesignSync` on the repository root. The
 * plugin exposes the `figmaDesignSync` extension and creates:
 *
 * - `generateFigmaDesignModel`
 * - `checkFigmaTrunkSync`
 */
@Suppress("unused")
class FigmaDesignSyncGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<figmaDesignSyncExtension>("figmaDesignSync")

        extension.designModelMetadataNodeUrl.convention(FIGMA_PAGE_URL)

        project.tasks.register<GenerateFigmaDesignModelTask>("generateFigmaDesignModel") {
            group = "documentation"
            description = "Generates the design model JSON consumed by the Figma MCP sync step."

            versionsFile.set(extension.versionsFile)
            rootSettingsFile.set(extension.rootSettingsFile)
            buildLogicSettingsFile.set(extension.buildLogicSettingsFile)
            projectRootDirectory.set(project.layout.projectDirectory)
            buildLogicRootDirectory.set(project.layout.projectDirectory.dir("build-logic"))
            outputFile.set(extension.designModelFile)
        }

        project.tasks.register<CheckFigmaTrunkSyncTask>("checkFigmaTrunkSync") {
            group = "verification"
            description = "Checks that Figma sync metadata matches the design model generated from the current checkout."

            metadataNodeUrl.set(extension.designModelMetadataNodeUrl)
            versionsFile.set(extension.versionsFile)
            rootSettingsFile.set(extension.rootSettingsFile)
            buildLogicSettingsFile.set(extension.buildLogicSettingsFile)
            projectRootDirectory.set(project.layout.projectDirectory)
            buildLogicRootDirectory.set(project.layout.projectDirectory.dir("build-logic"))
            figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
        }
    }

    private companion object {
        const val FIGMA_PAGE_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908"
    }
}
