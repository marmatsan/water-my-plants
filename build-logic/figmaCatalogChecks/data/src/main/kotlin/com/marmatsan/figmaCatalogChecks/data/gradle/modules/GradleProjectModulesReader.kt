package com.marmatsan.figmaCatalogChecks.data.gradle.modules



import me.tatarka.inject.annotations.Inject
import java.io.File

@Inject
class GradleProjectModulesReader {
    fun readModules(
        rootSettingsFile: File,
        buildLogicSettingsFile: File
    ): Set<String> {
        val rootModules = rootSettingsFile.readIncludedModules()
        val buildLogicModules = buildLogicSettingsFile
            .readIncludedModules()
            .map { module -> ":build-logic$module" }

        return (rootModules + buildLogicModules).toSortedSet()
    }

    private fun File.readIncludedModules(): Set<String> =
        stringLiteralRegex
            .findAll(readText())
            .map { match -> match.groupValues[1] }
            .filter { value -> value.startsWith(":") }
            .toSet()

    private companion object {
        val stringLiteralRegex = Regex(""""([^"]+)"""")
    }
}
