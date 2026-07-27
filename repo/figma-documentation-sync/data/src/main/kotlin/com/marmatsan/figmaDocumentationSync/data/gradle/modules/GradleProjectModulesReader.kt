package com.marmatsan.figmaDocumentationSync.data.gradle.modules

import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * Reads Gradle module paths from the root build and configured included builds.
 *
 * Included-build modules receive their configured prefix in the generated model
 * so they can coexist with root project modules without path collisions.
 */
@Inject
class GradleProjectModulesReader {
    /** Reads logical module identities from the main build and [includedBuilds]. */
    fun readModules(
        rootSettingsFile: File,
        includedBuilds: List<IncludedBuild>,
    ): Set<String> {
        val rootModules = rootSettingsFile.readIncludedModules()
        val includedBuildModules =
            includedBuilds.flatMap { includedBuild ->
                includedBuild.readIncludedBuildModules()
            }

        return (rootModules + includedBuildModules).toSortedSet()
    }

    /**
     * Physical included-build settings and logical module prefix.
     *
     * @property settingsFile included-build settings script.
     * @property modulePathPrefix logical prefix used by generated module identities.
     */
    data class IncludedBuild(
        val settingsFile: File,
        val modulePathPrefix: String,
    )

    private fun IncludedBuild.readIncludedBuildModules(): Set<String> {
        val includedModules = settingsFile.readIncludedModules()
        val standaloneRootModule =
            if (includedModules.isEmpty() &&
                settingsFile.parentFile
                    .resolve(
                        relative = BUILD_FILE_NAME,
                    ).isFile
            ) {
                setOf(STANDALONE_ROOT_MODULE)
            } else {
                emptySet()
            }

        return (
            standaloneRootModule + includedModules +
                includedModules.existingAggregateModules(
                    rootDir = settingsFile.parentFile,
                )
        ).map { module -> "$modulePathPrefix$module" }
            .toSet()
    }

    private fun File.readIncludedModules(): Set<String> =
        stringLiteralRegex
            .findAll(
                input = readText(),
            ).map { match -> match.groupValues[1] }
            .filter { value ->
                value.startsWith(
                    prefix = ":",
                )
            }.toSet()

    private fun Set<String>.existingAggregateModules(
        rootDir: File,
    ): Set<String> =
        flatMap { module -> module.parentModules() }
            .filter { module ->
                rootDir
                    .resolve(
                        relative = module.toRelativePath(),
                    ).isDirectory
            }.toSet()

    private fun String.parentModules(): List<String> {
        val segments = split(":").filter(String::isNotBlank)
        return segments
            .dropLast(1)
            .runningFold("") { modulePath, segment -> "$modulePath:$segment" }
            .drop(1)
    }

    private fun String.toRelativePath(): String =
        removePrefix(":").replace(
            ":",
            File.separator,
        )

    private companion object {
        const val BUILD_FILE_NAME = "build.gradle.kts"
        const val STANDALONE_ROOT_MODULE = ""

        val stringLiteralRegex = Regex(""""([^"]+)"""")
    }
}
