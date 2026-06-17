package com.marmatsan.figmaCatalogChecks.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

@Suppress("unused")
class FigmaCatalogChecksGradleConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<FigmaCatalogChecksExtension>("figmaCatalogChecks")

        extension.pageUrl.convention(FIGMA_PAGE_URL)
        extension.sectionUrl.convention(FIGMA_SECTION_URL)
        extension.versionComponentUrl.convention(FIGMA_VERSION_COMPONENT_URL)

        project.tasks.register<CheckFigmaVersionsTask>("checkFigmaVersions") {
            group = "verification"
            description = "Checks that Figma versions.properties variables match build-logic/versions.properties."

            pageUrl.set(extension.pageUrl)
            sectionUrl.set(extension.sectionUrl)
            versionComponentUrl.set(extension.versionComponentUrl)
            versionsFile.set(extension.versionsFile)
            figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
        }
    }

    private companion object {
        const val FIGMA_PAGE_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908"
        const val FIGMA_SECTION_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62936-183&t=gxgxBWEWgZjRldAX-4"
        const val FIGMA_VERSION_COMPONENT_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63075-591&t=gxgxBWEWgZjRldAX-4"
    }
}
