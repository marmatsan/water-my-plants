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
        extension.libraryTreeSectionUrl.convention(FIGMA_LIBRARY_TREE_SECTION_URL)
        extension.pluginTreeSectionUrl.convention(FIGMA_PLUGIN_TREE_SECTION_URL)
        extension.buildLogicLibraryTreeSectionUrl.convention(FIGMA_BUILD_LOGIC_LIBRARY_TREE_SECTION_URL)
        extension.buildLogicPluginTreeSectionUrl.convention(FIGMA_BUILD_LOGIC_PLUGIN_TREE_SECTION_URL)
        extension.versionComponentUrl.convention(FIGMA_VERSION_COMPONENT_URL)
        extension.moduleComponentUrl.convention(FIGMA_MODULE_COMPONENT_URL)

        project.tasks.register<CheckFigmaVersionsTask>("checkFigmaVersions") {
            group = "verification"
            description = "Checks that Figma versions.properties variables match build-logic/versions.properties."

            pageUrl.set(extension.pageUrl)
            sectionUrl.set(extension.sectionUrl)
            versionComponentUrl.set(extension.versionComponentUrl)
            versionsFile.set(extension.versionsFile)
            figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
        }

        project.tasks.register<CheckFigmaLibraryTreeTask>("checkFigmaLibraryTree") {
            group = "verification"
            description = "Checks that the Figma library catalog tree matches build-logic dependency trees."

            pageUrl.set(extension.pageUrl)
            sectionUrl.set(extension.libraryTreeSectionUrl)
            versionsFile.set(extension.versionsFile)
            projectRootDirectory.set(project.layout.projectDirectory)
            figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
        }

        project.tasks.register<CheckFigmaPluginTreeTask>("checkFigmaPluginTree") {
            group = "verification"
            description = "Checks that the Figma plugin catalog tree matches build-logic dependency trees."

            pageUrl.set(extension.pageUrl)
            sectionUrl.set(extension.pluginTreeSectionUrl)
            versionsFile.set(extension.versionsFile)
            projectRootDirectory.set(project.layout.projectDirectory)
            figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
        }

        project.tasks.register<CheckFigmaBuildLogicLibraryTreeTask>("checkFigmaBuildLogicLibraryTree") {
            group = "verification"
            description = "Checks that the Figma build-logic library catalog tree matches build-logic/settings.gradle.kts."

            pageUrl.set(extension.pageUrl)
            sectionUrl.set(extension.buildLogicLibraryTreeSectionUrl)
            settingsFile.set(extension.buildLogicSettingsFile)
            figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
        }

        project.tasks.register<CheckFigmaBuildLogicPluginTreeTask>("checkFigmaBuildLogicPluginTree") {
            group = "verification"
            description = "Checks that the Figma build-logic plugin catalog tree matches build-logic/settings.gradle.kts."

            pageUrl.set(extension.pageUrl)
            sectionUrl.set(extension.buildLogicPluginTreeSectionUrl)
            settingsFile.set(extension.buildLogicSettingsFile)
            figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
        }

        project.tasks.register<CheckFigmaModulesTask>("checkFigmaModules") {
            group = "verification"
            description = "Checks that the Figma .module component variants match the repository modules."

            pageUrl.set(extension.pageUrl)
            moduleComponentUrl.set(extension.moduleComponentUrl)
            rootSettingsFile.set(extension.rootSettingsFile)
            buildLogicSettingsFile.set(extension.buildLogicSettingsFile)
            figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
        }
    }

    private companion object {
        const val FIGMA_PAGE_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908"
        const val FIGMA_SECTION_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62936-183&t=gxgxBWEWgZjRldAX-4"
        const val FIGMA_LIBRARY_TREE_SECTION_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-629&t=gxgxBWEWgZjRldAX-4"
        const val FIGMA_PLUGIN_TREE_SECTION_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-594&t=gxgxBWEWgZjRldAX-4"
        const val FIGMA_BUILD_LOGIC_LIBRARY_TREE_SECTION_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63099-951&t=gxgxBWEWgZjRldAX-4"
        const val FIGMA_BUILD_LOGIC_PLUGIN_TREE_SECTION_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63100-2952&t=gxgxBWEWgZjRldAX-4"
        const val FIGMA_VERSION_COMPONENT_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63075-591&t=gxgxBWEWgZjRldAX-4"
        const val FIGMA_MODULE_COMPONENT_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63085-793&t=gxgxBWEWgZjRldAX-4"
    }
}
