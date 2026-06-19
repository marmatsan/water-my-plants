package com.marmatsan.figmaCatalogChecks.data.gradle.usage

import com.marmatsan.figmaCatalogChecks.domain.model.usage.LibraryCatalogUsageKey
import com.marmatsan.figmaCatalogChecks.domain.model.usage.ProjectCatalogUsage
import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
class GradleProjectCatalogUsageReader {
    fun read(rootDir: File): ProjectCatalogUsage {
        val buildFiles = rootDir
            .walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
            .toList()

        val libraryKeysByModule = mutableMapOf<String, Set<LibraryCatalogUsageKey>>()
        val libraryAccessorsByModule = mutableMapOf<String, Set<String>>()
        val pluginIdsByModule = mutableMapOf<String, Set<String>>()

        buildFiles.forEach { buildFile ->
            val modulePath = buildFile.parentFile.toModulePath(rootDir)

            if (modulePath == ROOT_MODULE) {
                return@forEach
            }

            val searchableText = when {
                modulePath in BuildLogicConventionPluginModules -> buildFile.parentFile.kotlinSourcesText()
                modulePath.startsWith(BUILD_LOGIC_MODULE_PREFIX) -> ""
                else -> buildFile.readText()
            }

            libraryKeysByModule[modulePath] = searchableText.libraryKeys()
            libraryAccessorsByModule[modulePath] = searchableText.libraryAccessors()
            pluginIdsByModule[modulePath] = searchableText.pluginIds()
        }

        return ProjectCatalogUsage(
            libraryKeysByModule = libraryKeysByModule,
            libraryAccessorsByModule = libraryAccessorsByModule,
            pluginIdsByModule = pluginIdsByModule
        )
    }

    private fun File.toModulePath(rootDir: File): String {
        val relativePath = rootDir.toPath().relativize(toPath()).toString()

        if (relativePath.isEmpty()) {
            return ":"
        }

        return ":" + relativePath
            .replace(File.separatorChar, ':')
            .replace('/', ':')
            .replace('\\', ':')
    }

    private fun File.kotlinSourcesText(): String {
        val sourceRoot = resolve("src/main/kotlin")

        if (!sourceRoot.exists()) {
            return ""
        }

        return sourceRoot
            .walkTopDown()
            .filter { file -> file.isFile && file.extension == "kt" }
            .joinToString(separator = "\n") { file -> file.readText() }
    }

    private fun String.libraryKeys(): Set<LibraryCatalogUsageKey> {
        val artifactKeys = LibraryGroupArtifactRegex
            .findAll(this)
            .map { match ->
                LibraryCatalogUsageKey.Artifact(
                    group = match.groupValues[1],
                    artifact = match.groupValues[2]
                )
            }

        val bundleKeys = BundleRegex
            .findAll(this)
            .map { match -> LibraryCatalogUsageKey.Bundle(alias = match.groupValues[1]) }

        return (artifactKeys + bundleKeys).toSet()
    }

    private fun String.libraryAccessors(): Set<String> =
        LibraryAccessorRegex
            .findAll(this)
            .map { match -> match.groupValues[1] }
            .filterNot { accessor -> accessor == "versions" }
            .toSet()

    private fun String.pluginIds(): Set<String> {
        val directIds = PluginIdRegex
            .findAll(this)
            .map { match -> match.groupValues[1] }

        val aliasIds = PluginAliasRegex
            .findAll(this)
            .map { match -> match.groupValues[1] }

        val appliedIds = PluginManagerApplyRegex
            .findAll(this)
            .map { match -> match.groupValues[1] }

        return (directIds + aliasIds + appliedIds).toSet()
    }

    private companion object {
        const val BUILD_FILE_NAME = "build.gradle.kts"
        const val ROOT_MODULE = ":"
        const val BUILD_LOGIC_MODULE_PREFIX = ":build-logic"

        val BuildLogicConventionPluginModules = setOf(
            ":build-logic:android",
            ":build-logic:bddTest",
            ":build-logic:compose",
            ":build-logic:protobuf",
            ":build-logic:unitTest"
        )

        val LibraryGroupArtifactRegex = Regex(
            pattern = "libraryGroup\\s*=\\s*\"([^\"]+)\"[\\s\\S]*?artifact\\s*=\\s*\"([^\"]+)\""
        )
        val BundleRegex = Regex("bundle\\s*=\\s*\"([^\"]+)\"")
        val LibraryAccessorRegex = Regex("""\blibs\.([A-Za-z0-9_.]+)""")
        val PluginIdRegex = Regex("""\bid\("([^"]+)"\)""")
        val PluginAliasRegex = Regex("""alias\(\s*plugins\.plugins\.([A-Za-z0-9_.]+)\s*\)""")
        val PluginManagerApplyRegex = Regex("""pluginManager\.apply\("([^"]+)"\)""")
    }
}
