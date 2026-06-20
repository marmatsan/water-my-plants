package com.marmatsan.figmaDesignSync.data.gradle.modules

import com.marmatsan.figmaDesignSync.domain.model.modules.ModuleDependency
import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
class GradleModuleDependenciesReader {
    fun readMain(rootDir: File): Set<ModuleDependency> =
        rootDir
            .walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
            .filterNot { file -> file.toRelativeString(rootDir).startsWith("$BUILD_LOGIC_DIR_NAME${File.separator}") }
            .flatMap { buildFile ->
                buildFile.readModuleDependencies(
                    rootDir = rootDir,
                    modulePathPrefix = ""
                )
            }
            .toSortedSet()

    fun readBuildLogic(rootDir: File): Set<ModuleDependency> =
        rootDir
            .walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }
            .flatMap { buildFile ->
                buildFile.readModuleDependencies(
                    rootDir = rootDir,
                    modulePathPrefix = BUILD_LOGIC_MODULE_PREFIX
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
            .dependencyModulePaths(modulePathPrefix)
            .map { dependencyModule ->
                ModuleDependency(
                    dependentModule = dependentModule,
                    dependencyModule = dependencyModule
                )
            }
            .toSet()
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
        const val BUILD_LOGIC_DIR_NAME = "build-logic"
        const val BUILD_LOGIC_MODULE_PREFIX = ":build-logic"
        const val ROOT_MODULE = ":"

        val ProjectCallRegex = Regex("project\\s*\\(\\s*(?:path\\s*=\\s*)?\"(:[^\"]+)\"")
        val ProjectAccessorRegex = Regex("""\bprojects\.([A-Za-z0-9_.]+)\b""")
    }
}
