package com.marmatsan.figmaDocumentationSync.data.gradle.modules

import com.marmatsan.figmaDocumentationSync.data.gradle.isInsideNestedGradleBuild
import com.marmatsan.figmaDocumentationSync.domain.model.modules.ModuleDependency
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
    fun readMain(rootDir: File): Set<ModuleDependency> {
        val modulePathsByProjectAccessor = rootDir.modulePathsByProjectAccessor(modulePathPrefix = "")

        return rootDir
            .walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
            .filterNot { file -> file.isInsideNestedGradleBuild(rootDir) }
            .flatMap { buildFile ->
                buildFile.readModuleDependencies(
                    rootDir = rootDir,
                    modulePathPrefix = "",
                    modulePathsByProjectAccessor = modulePathsByProjectAccessor
                )
            }
            .toSortedSet()
    }

    /**
     * Reads dependencies between modules inside an included build and prefixes
     * every module path with [modulePathPrefix].
     */
    fun readIncludedBuild(
        rootDir: File,
        modulePathPrefix: String
    ): Set<ModuleDependency> {
        val modulePathsByProjectAccessor = rootDir.modulePathsByProjectAccessor(modulePathPrefix)

        return rootDir
            .walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
            .flatMap { buildFile ->
                buildFile.readModuleDependencies(
                    rootDir = rootDir,
                    modulePathPrefix = modulePathPrefix,
                    modulePathsByProjectAccessor = modulePathsByProjectAccessor
                )
            }
            .toSortedSet()
    }

    private fun File.readModuleDependencies(
        rootDir: File,
        modulePathPrefix: String,
        modulePathsByProjectAccessor: Map<String, String>
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
            .flatMap { dependenciesBlock ->
                dependenciesBlock.dependencyModulePaths(
                    modulePathPrefix = modulePathPrefix,
                    modulePathsByProjectAccessor = modulePathsByProjectAccessor
                )
            }
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

    private fun String.dependencyModulePaths(
        modulePathPrefix: String,
        modulePathsByProjectAccessor: Map<String, String>
    ): Set<String> {
        val projectCallPaths = ProjectCallRegex
            .findAll(this)
            .map { match -> match.groupValues[1] }

        val projectAccessorPaths = ProjectAccessorRegex
            .findAll(this)
            .map { match ->
                val projectAccessor = match.groupValues[1]
                modulePathsByProjectAccessor[projectAccessor]
                    ?: projectAccessor.toModulePath(modulePathPrefix)
            }

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

    private fun File.modulePathsByProjectAccessor(
        modulePathPrefix: String
    ): Map<String, String> =
        walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
            .mapNotNull { buildFile ->
                val relativePath = toPath().relativize(buildFile.parentFile.toPath()).toString()
                val segments = relativePath
                    .split(File.separatorChar, '/', '\\')
                    .filter(String::isNotBlank)

                if (segments.isEmpty()) {
                    null
                } else {
                    segments
                        .joinToString(".") { segment -> segment.toProjectAccessorSegment() } to
                        buildFile.parentFile.toModulePath(
                            rootDir = this,
                            modulePathPrefix = modulePathPrefix
                        )
                }
            }
            .toMap()

    private fun String.toProjectAccessorSegment(): String =
        split('-', '_')
            .filter(String::isNotEmpty)
            .mapIndexed { index, segment ->
                if (index == 0) {
                    segment
                } else {
                    segment.replaceFirstChar(Char::uppercaseChar)
                }
            }
            .joinToString("")

    private companion object {
        const val BUILD_FILE_NAME = "build.gradle.kts"
        const val ROOT_MODULE = ":"

        val ProjectCallRegex = Regex("project\\s*\\(\\s*(?:path\\s*=\\s*)?\"(:[^\"]+)\"")
        val ProjectAccessorRegex = Regex("""\bprojects\.([A-Za-z0-9_.]+)\b""")
        val DependenciesBlockStartRegex = Regex("""\bdependencies\s*\{""")
    }
}
