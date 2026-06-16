package com.marmatsan.figmaVersions.plugin

import com.marmatsan.figmaVersions.FigmaVersionsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

@Suppress("unused")
class FigmaVersionsGradleConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<FigmaVersionsExtension>("figmaVersions")

        extension.fileKey.convention(FIGMA_FILE_KEY)
        extension.pageName.convention(FIGMA_PAGE_NAME)
        extension.sectionName.convention(FIGMA_SECTION_NAME)

        project.tasks.register<CheckFigmaVersionsTask>("checkFigmaVersions") {
            group = "verification"
            description = "Checks that Figma versions.properties variables match build-logic/versions.properties."

            fileKey.set(extension.fileKey)
            pageName.set(extension.pageName)
            sectionName.set(extension.sectionName)
            versionsFile.set(extension.versionsFile)
            figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
        }
    }

    private companion object {
        const val FIGMA_FILE_KEY = "YBZXsd8oyGLbcI2KWxJvRK"
        const val FIGMA_PAGE_NAME = "🐘 Gradle dependencies"
        const val FIGMA_SECTION_NAME = "build-logic\\versions.properties"
    }
}
