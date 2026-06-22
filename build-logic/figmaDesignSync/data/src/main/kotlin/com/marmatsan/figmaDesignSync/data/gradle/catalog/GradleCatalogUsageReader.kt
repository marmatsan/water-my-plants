package com.marmatsan.figmaDesignSync.data.gradle.catalog

import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
class GradleCatalogUsageReader {
    fun readConventionLibraryUsages(rootDir: File): LibraryUsages =
        rootDir.resolve(BUILD_LOGIC_DIR)
            .conventionModuleFiles()
            .fold(LibraryUsages()) { usages, buildFile ->
                usages + buildFile.readConventionLibraryUsageDeclarations()
            }

    fun readConventionPluginUsages(rootDir: File): Map<String, Set<String>> =
        rootDir.resolve(BUILD_LOGIC_DIR)
            .conventionModuleFiles()
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(buildFile.readAppliedPlugins())
            }

    fun readBuildLogicLibraryUsages(buildLogicRootDir: File): Map<String, Set<String>> =
        buildLogicRootDir
            .buildFiles()
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(buildFile.readBuildLogicLibraryAliases(buildLogicRootDir))
            }

    fun readBuildLogicPluginUsages(buildLogicRootDir: File): Map<String, Set<String>> =
        buildLogicRootDir
            .buildFiles()
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(buildFile.readBuildLogicPluginAliases(buildLogicRootDir))
            }

    fun readMainPluginUsages(rootDir: File): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .filterNot { file -> file.toRelativeString(rootDir).startsWith("$BUILD_LOGIC_DIR${File.separator}") }
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(buildFile.readMainPluginAliases(rootDir))
            }

    fun readMainLiteralPluginUsages(rootDir: File): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .filterNot { file -> file.toRelativeString(rootDir).startsWith("$BUILD_LOGIC_DIR${File.separator}") }
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(buildFile.readMainLiteralPluginIds(rootDir))
            }

    private fun File.conventionModuleFiles(): Sequence<File> =
        walkTopDown()
            .filter { file -> file.isFile && file.extension == KOTLIN_FILE_EXTENSION }
            .filter { file ->
                file.toRelativeString(this)
                    .split(File.separatorChar, '/', '\\')
                    .firstOrNull() in ConventionPluginModuleNames
            }

    private fun File.buildFiles(): Sequence<File> =
        walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }

    private fun File.readConventionLibraryUsageDeclarations(): LibraryUsages {
        val modulePath = toBuildLogicConventionModulePath()
        val content = readText()
        val coordinateUsages = libraryCoordinateUsageRegex
            .findAll(content)
            .associateToUsageMap(modulePath) { match ->
                "${match.groupValues[1]}:${match.groupValues[2]}"
            }
        val bundleUsages = libraryBundleUsageRegex
            .findAll(content)
            .associateToUsageMap(modulePath) { match -> match.groupValues[1] }

        return LibraryUsages(
            coordinates = coordinateUsages,
            bundles = bundleUsages
        )
    }

    private fun File.readAppliedPlugins(): Map<String, Set<String>> {
        val modulePath = toBuildLogicConventionModulePath()

        return appliedPluginRegex
            .findAll(readText())
            .associateToUsageMap(modulePath) { match -> match.groupValues[1] }
    }

    private fun File.readBuildLogicLibraryAliases(buildLogicRootDir: File): Map<String, Set<String>> {
        val modulePath = toBuildLogicModulePath(buildLogicRootDir)

        return buildLogicLibraryAliasRegex
            .findAll(readText())
            .associateToUsageMap(modulePath) { match -> match.groupValues[1] }
    }

    private fun File.readBuildLogicPluginAliases(buildLogicRootDir: File): Map<String, Set<String>> {
        val modulePath = toBuildLogicModulePath(buildLogicRootDir)

        return buildLogicPluginAliasRegex
            .findAll(readText())
            .associateToUsageMap(modulePath) { match -> match.groupValues[1] }
    }

    private fun File.readMainPluginAliases(rootDir: File): Map<String, Set<String>> {
        val modulePath = parentFile.toModulePath(rootDir)
        if (modulePath == ROOT_MODULE) return emptyMap()

        return mainPluginAliasRegex
            .findAll(readText())
            .associateToUsageMap(modulePath) { match -> match.groupValues[1] }
    }

    private fun File.readMainLiteralPluginIds(rootDir: File): Map<String, Set<String>> {
        val modulePath = parentFile.toModulePath(rootDir)
        if (modulePath == ROOT_MODULE) return emptyMap()

        return literalPluginIdRegex
            .findAll(readText())
            .associateToUsageMap(modulePath) { match -> match.groupValues[1] }
    }

    private fun Sequence<MatchResult>.associateToUsageMap(
        modulePath: String,
        key: (MatchResult) -> String
    ): Map<String, Set<String>> =
        map(key)
            .fold(emptyMap()) { usages, usageKey ->
                usages + (usageKey to (usages[usageKey].orEmpty() + modulePath))
            }

    private fun Map<String, Set<String>>.merge(other: Map<String, Set<String>>): Map<String, Set<String>> =
        (keys + other.keys).associateWith { key ->
            (this[key].orEmpty() + other[key].orEmpty()).toSortedSet()
        }

    private operator fun LibraryUsages.plus(other: LibraryUsages): LibraryUsages =
        LibraryUsages(
            coordinates = coordinates.merge(other.coordinates),
            bundles = bundles.merge(other.bundles)
        )

    private fun File.toBuildLogicModulePath(): String {
        val buildLogicRoot = generateSequence(parentFile) { file -> file.parentFile }
            .first { file -> file.name == BUILD_LOGIC_DIR }

        return toBuildLogicModulePath(buildLogicRoot)
    }

    private fun File.toBuildLogicConventionModulePath(): String {
        val buildLogicRoot = generateSequence(parentFile) { file -> file.parentFile }
            .first { file -> file.name == BUILD_LOGIC_DIR }
        val relativePath = buildLogicRoot.toPath().relativize(toPath()).toString()
        val conventionModule = relativePath
            .split(File.separatorChar, '/', '\\')
            .first()

        return "$BUILD_LOGIC_MODULE_PREFIX:$conventionModule"
    }

    private fun File.toBuildLogicModulePath(buildLogicRootDir: File): String {
        val moduleDir = if (isDirectory) this else parentFile
        val relativePath = buildLogicRootDir.toPath().relativize(moduleDir.toPath()).toString()

        if (relativePath.isEmpty()) return BUILD_LOGIC_MODULE_PREFIX

        return "$BUILD_LOGIC_MODULE_PREFIX:${relativePath.toModuleSegments()}"
    }

    private fun File.toModulePath(rootDir: File): String {
        val relativePath = rootDir.toPath().relativize(toPath()).toString()

        if (relativePath.isEmpty()) return ROOT_MODULE

        return ":${relativePath.toModuleSegments()}"
    }

    private fun String.toModuleSegments(): String =
        replace(File.separatorChar, ':')
            .replace('/', ':')
            .replace('\\', ':')

    data class LibraryUsages(
        val coordinates: Map<String, Set<String>> = emptyMap(),
        val bundles: Map<String, Set<String>> = emptyMap()
    )

    private companion object {
        const val BUILD_LOGIC_DIR = "build-logic"
        const val BUILD_FILE_NAME = "build.gradle.kts"
        const val KOTLIN_FILE_EXTENSION = "kt"
        const val BUILD_LOGIC_MODULE_PREFIX = ":build-logic"
        const val ROOT_MODULE = ":"

        val ConventionPluginModuleNames = setOf(
            "android",
            "bddTest",
            "compose",
            "protobuf",
            "unitTest"
        )

        val libraryCoordinateUsageRegex = Regex(
            """(?:implementation|implementationPlatform|testImplementation|testImplementationPlatform|testRuntimeOnly|ksp)\s*\(\s*libs\s*=\s*libs\s*,\s*libraryGroup\s*=\s*"([^"]+)"\s*,\s*artifact\s*=\s*"([^"]+)"""",
            RegexOption.DOT_MATCHES_ALL
        )
        val libraryBundleUsageRegex = Regex(
            """implementationBundle\s*\(\s*libs\s*=\s*libs\s*,\s*bundle\s*=\s*"([^"]+)"""",
            RegexOption.DOT_MATCHES_ALL
        )
        val appliedPluginRegex = Regex("""pluginManager\.apply\s*\(\s*"([^"]+)"""")
        val buildLogicLibraryAliasRegex = Regex("""\blibs\.([A-Za-z0-9_.]+)\b""")
        val buildLogicPluginAliasRegex = Regex("""alias\s*\(\s*plugins\.plugins\.([A-Za-z0-9_.]+)\s*\)""")
        val mainPluginAliasRegex = Regex("""alias\s*\(\s*plugins\.plugins\.([A-Za-z0-9_.]+)\s*\)""")
        val literalPluginIdRegex = Regex("""\bid\s*\(\s*"([^"]+)"\s*\)""")
    }
}
