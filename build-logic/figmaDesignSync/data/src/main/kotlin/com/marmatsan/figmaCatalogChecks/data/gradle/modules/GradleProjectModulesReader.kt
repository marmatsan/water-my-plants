package com.marmatsan.figmaDesignSync.data.gradle.modules



import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
class GradleProjectModulesReader {
    fun readModules(
        rootSettingsFile: File,
        buildLogicSettingsFile: File
    ): Set<String> {
        val rootModules = rootSettingsFile.readIncludedModules()
        val buildLogicIncludedModules = buildLogicSettingsFile.readIncludedModules()
        val buildLogicModules = (
            buildLogicIncludedModules + buildLogicIncludedModules.existingAggregateModules(
                rootDir = buildLogicSettingsFile.parentFile
            )
        )
            .map { module -> ":build-logic$module" }

        return (rootModules + buildLogicModules).toSortedSet()
    }

    private fun File.readIncludedModules(): Set<String> =
        stringLiteralRegex
            .findAll(readText())
            .map { match -> match.groupValues[1] }
            .filter { value -> value.startsWith(":") }
            .toSet()

    private fun Set<String>.existingAggregateModules(
        rootDir: File
    ): Set<String> =
        flatMap { module -> module.parentModules() }
            .filter { module -> rootDir.resolve(module.toRelativePath()).isDirectory }
            .toSet()

    private fun String.parentModules(): List<String> {
        val segments = split(":").filter(String::isNotBlank)
        return segments
            .dropLast(1)
            .runningFold("") { modulePath, segment -> "$modulePath:$segment" }
            .drop(1)
    }

    private fun String.toRelativePath(): String =
        removePrefix(":").replace(":", File.separator)

    private companion object {
        val stringLiteralRegex = Regex(""""([^"]+)"""")
    }
}
