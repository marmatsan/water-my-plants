package com.marmatsan.figmaDesignSync.data.gradle.modules

import com.marmatsan.figmaDesignSync.data.gradle.isInsideNestedGradleBuild
import com.marmatsan.figmaDesignSync.domain.model.modules.ModuleDependency
import java.io.File
import me.tatarka.inject.annotations.Inject

/**
 * Parses Gradle `dependencies` blocks into directed [ModuleDependency] edges.
 *
 * The reader intentionally works from source files instead of Gradle's runtime
 * dependency model because the design documentation needs a lightweight,
 * repeatable snapshot of project-to-project dependencies.
 */
@Inject
class GradleModuleDependenciesReader {
    /**
     * Reads dependencies between modules in the root project, excluding nested
     * Gradle builds such as build tooling and repository tools.
     */
    fun readMain(rootDir: File): Set<ModuleDependency> =
        rootDir
            .walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
            .filterNot { file -> file.isInsideNestedGradleBuild(rootDir) }
            .flatMap { buildFile ->
                buildFile.readModuleDependencies(
                    rootDir = rootDir,
                    modulePathPrefix = ""
                )
            }
            .toSortedSet()

    /**
     * Reads dependencies between modules inside an included build and prefixes
     * every module path with [modulePathPrefix].
     */
    fun readIncludedBuild(
        rootDir: File,
        modulePathPrefix: String
    ): Set<ModuleDependency> =
        rootDir
            .walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
            .flatMap { buildFile ->
                buildFile.readModuleDependencies(
                    rootDir = rootDir,
                    modulePathPrefix = modulePathPrefix
                )
            }
            .toSortedSet()

    private fun File.readModuleDependencies(
        rootDir: File,
        modulePathPrefix: String
    ): Set<ModuleDependency> {
        val dependentModule = parentFile.toModulePath(
            rootDir = rootDir,
            modulePathPrefix = modulePathPrefix
        )

        if (dependentModule == ROOT_MODULE || dependentModule == modulePathPrefix) {
            return emptySet()
        }

        return readText()
            .dependenciesBlocks()
            .flatMap { dependenciesBlock -> dependenciesBlock.dependencyModulePaths(modulePathPrefix) }
            .map { dependencyModule ->
                ModuleDependency(
                    dependentModule = dependentModule,
                    dependencyModule = dependencyModule
                )
            }
            .toSet()
    }

    private fun String.dependenciesBlocks(): Sequence<String> =
        sequence {
            var searchIndex = 0
            while (searchIndex < length) {
                val match = DependenciesBlockStartRegex.find(this@dependenciesBlocks, searchIndex) ?: break
                val openBraceIndex = match.range.last
                val closeBraceIndex = findMatchingBrace(openBraceIndex)
                if (closeBraceIndex == -1) {
                    break
                }

                yield(substring(openBraceIndex + 1, closeBraceIndex))
                searchIndex = closeBraceIndex + 1
            }
        }

    private fun String.findMatchingBrace(openBraceIndex: Int): Int {
        var depth = 0
        for (index in openBraceIndex until length) {
            when (this[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) {
                        return index
                    }
                }
            }
        }

        return -1
    }

    private fun String.dependencyModulePaths(modulePathPrefix: String): Set<String> {
        val projectCallPaths = ProjectCallRegex
            .findAll(this)
            .map { match -> match.groupValues[1] }

        val projectAccessorPaths = ProjectAccessorRegex
            .findAll(this)
            .map { match -> match.groupValues[1].toModulePath(modulePathPrefix) }

        return (projectCallPaths + projectAccessorPaths)
            .filter { modulePath -> modulePath.isNotBlank() }
            .toSet()
    }

    private fun File.toModulePath(
        rootDir: File,
        modulePathPrefix: String
    ): String {
        val relativePath = rootDir.toPath().relativize(toPath()).toString()

        if (relativePath.isEmpty()) {
            return modulePathPrefix.ifBlank { ROOT_MODULE }
        }

        val modulePath = relativePath
            .replace(File.separatorChar, ':')
            .replace('/', ':')
            .replace('\\', ':')

        return "$modulePathPrefix:$modulePath"
    }

    private fun String.toModulePath(modulePathPrefix: String): String {
        val modulePath = replace(".", ":")

        return "$modulePathPrefix:$modulePath"
    }

    private companion object {
        const val BUILD_FILE_NAME = "build.gradle.kts"
        const val ROOT_MODULE = ":"

        val ProjectCallRegex = Regex("project\\s*\\(\\s*(?:path\\s*=\\s*)?\"(:[^\"]+)\"")
        val ProjectAccessorRegex = Regex("""\bprojects\.([A-Za-z0-9_.]+)\b""")
        val DependenciesBlockStartRegex = Regex("""\bdependencies\s*\{""")
    }
}
