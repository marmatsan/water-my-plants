package com.marmatsan.figmaDesignSync.data.gradle.catalog

import com.marmatsan.figmaDesignSync.data.gradle.isInsideNestedGradleBuild
import java.io.File
import me.tatarka.inject.annotations.Inject

/**
 * Reads where catalog aliases and plugin ids are used across Gradle files.
 *
 * Usage data enriches catalog trees with the modules that require each
 * dependency or apply each plugin. This makes the generated Figma documentation
 * navigable from catalog entry to repository module.
 */
@Inject
class GradleCatalogUsageReader {
    /**
     * Reads dependency DSL library usages from convention plugin modules in an
     * included build.
     */
    fun readConventionLibraryUsages(
        rootDir: File,
        modulePathPrefix: String
    ): LibraryUsages {
        val conventionPluginModuleDirs = rootDir.conventionPluginModuleDirs()

        return conventionPluginModuleDirs
            .asSequence()
            .flatMap { moduleDir ->
                moduleDir.kotlinFiles().map { kotlinFile -> moduleDir to kotlinFile }
            }
            .fold(LibraryUsages()) { usages, (moduleDir, kotlinFile) ->
                usages + kotlinFile.readConventionLibraryUsageDeclarations(
                    includedBuildRootDir = rootDir,
                    modulePathPrefix = modulePathPrefix,
                    moduleDir = moduleDir
                )
            }
    }

    fun readConventionPluginUsages(
        rootDir: File,
        modulePathPrefix: String
    ): Map<String, Set<String>> {
        val conventionPluginModuleDirs = rootDir.conventionPluginModuleDirs()

        return conventionPluginModuleDirs
            .asSequence()
            .flatMap { moduleDir ->
                moduleDir.kotlinFiles().map { kotlinFile -> moduleDir to kotlinFile }
            }
            .fold(emptyMap()) { usages, (moduleDir, kotlinFile) ->
                usages.merge(
                    kotlinFile.readAppliedPlugins(
                        includedBuildRootDir = rootDir,
                        modulePathPrefix = modulePathPrefix,
                        moduleDir = moduleDir
                    )
                )
            }
    }

    fun readIncludedBuildLibraryUsages(
        rootDir: File,
        modulePathPrefix: String
    ): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(
                    buildFile.readIncludedBuildLibraryAliases(
                        includedBuildRootDir = rootDir,
                        modulePathPrefix = modulePathPrefix
                    )
                )
            }

    fun readIncludedBuildPluginUsages(
        rootDir: File,
        modulePathPrefix: String
    ): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(
                    buildFile.readIncludedBuildPluginAliases(
                        includedBuildRootDir = rootDir,
                        modulePathPrefix = modulePathPrefix
                    )
                )
            }

    fun readMainPluginUsages(rootDir: File): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .filterNot { file -> file.isInsideNestedGradleBuild(rootDir) }
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(buildFile.readMainPluginAliases(rootDir))
            }

    fun readMainLiteralPluginUsages(rootDir: File): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .filterNot { file -> file.isInsideNestedGradleBuild(rootDir) }
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(buildFile.readMainLiteralPluginIds(rootDir))
            }

    fun readMainAppliedLiteralPluginUsages(rootDir: File): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .filterNot { file -> file.isInsideNestedGradleBuild(rootDir) }
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(buildFile.readMainAppliedLiteralPluginIds(rootDir))
            }

    fun readMainAppliedLiteralPluginIds(rootDir: File): Set<String> =
        rootDir
            .buildFiles()
            .filterNot { file -> file.isInsideNestedGradleBuild(rootDir) }
            .flatMap { buildFile -> buildFile.readAppliedLiteralPluginIds() }
            .toSet()

    private fun File.conventionPluginModuleDirs(): Set<File> =
        buildFiles()
            .filter { buildFile -> buildFile.readText().hasGradleConventionPluginImplementation() }
            .map { buildFile -> buildFile.parentFile }
            .toSet()

    private fun File.kotlinFiles(): Sequence<File> =
        walkTopDown()
            .filter { file -> file.isFile && file.extension == KOTLIN_FILE_EXTENSION }

    private fun File.buildFiles(): Sequence<File> =
        walkTopDown()
            .filter { file -> file.isFile && file.name == BUILD_FILE_NAME }

    private fun File.readConventionLibraryUsageDeclarations(
        includedBuildRootDir: File,
        modulePathPrefix: String,
        moduleDir: File
    ): LibraryUsages {
        val modulePath = moduleDir.toIncludedBuildModulePath(includedBuildRootDir, modulePathPrefix)
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

    private fun File.readAppliedPlugins(
        includedBuildRootDir: File,
        modulePathPrefix: String,
        moduleDir: File
    ): Map<String, Set<String>> {
        val modulePath = moduleDir.toIncludedBuildModulePath(includedBuildRootDir, modulePathPrefix)

        return appliedPluginRegex
            .findAll(readText())
            .associateToUsageMap(modulePath) { match -> match.groupValues[1] }
    }

    private fun File.readIncludedBuildLibraryAliases(
        includedBuildRootDir: File,
        modulePathPrefix: String
    ): Map<String, Set<String>> {
        val modulePath = toIncludedBuildModulePath(includedBuildRootDir, modulePathPrefix)

        return includedBuildLibraryAliasRegex
            .findAll(readText())
            .associateToUsageMap(modulePath) { match -> match.groupValues[1] }
    }

    private fun File.readIncludedBuildPluginAliases(
        includedBuildRootDir: File,
        modulePathPrefix: String
    ): Map<String, Set<String>> {
        val modulePath = toIncludedBuildModulePath(includedBuildRootDir, modulePathPrefix)

        return includedBuildPluginAliasRegex
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

    private fun File.readMainAppliedLiteralPluginIds(rootDir: File): Map<String, Set<String>> {
        val modulePath = parentFile.toModulePath(rootDir)
        if (modulePath == ROOT_MODULE) return emptyMap()
        val content = readText()

        return literalPluginIdRegex
            .findAll(content)
            .filterNot { match -> content.lineSuffixAfter(match).contains(applyFalseRegex) }
            .associateToUsageMap(modulePath) { match -> match.groupValues[1] }
    }

    private fun File.readAppliedLiteralPluginIds(): Sequence<String> {
        val content = readText()

        return literalPluginIdRegex
            .findAll(content)
            .filterNot { match -> content.lineSuffixAfter(match).contains(applyFalseRegex) }
            .map { match -> match.groupValues[1] }
    }

    private fun String.lineSuffixAfter(match: MatchResult): String {
        val lineEnd = indexOf('\n', startIndex = match.range.last + 1)
            .takeIf { index -> index >= 0 }
            ?: length

        return substring(match.range.last + 1, lineEnd)
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

    private fun File.toIncludedBuildModulePath(
        includedBuildRootDir: File,
        modulePathPrefix: String
    ): String {
        val moduleDir = if (isDirectory) this else parentFile
        val relativePath = includedBuildRootDir.toPath().relativize(moduleDir.toPath()).toString()

        if (relativePath.isEmpty()) return modulePathPrefix

        return "$modulePathPrefix:${relativePath.toModuleSegments()}"
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

    private fun String.hasGradleConventionPluginImplementation(): Boolean =
        GradleConventionPluginImplementationRegex.containsMatchIn(this)

    /**
     * Usage index for libraries declared as direct coordinates or bundles.
     *
     * Values are Gradle module paths that reference each key.
     */
    data class LibraryUsages(
        val coordinates: Map<String, Set<String>> = emptyMap(),
        val bundles: Map<String, Set<String>> = emptyMap()
    )

    private companion object {
        const val BUILD_FILE_NAME = "build.gradle.kts"
        const val KOTLIN_FILE_EXTENSION = "kt"
        const val ROOT_MODULE = ":"

        val libraryCoordinateUsageRegex = Regex(
            """(?:\blibs\.)?(?:implementation|implementationPlatform|testImplementation|testImplementationPlatform|testRuntimeOnly|ksp)\s*\(\s*(?:libs\s*=\s*libs\s*,\s*)?libraryGroup\s*=\s*"([^"]+)"\s*,\s*artifact\s*=\s*"([^"]+)"""",
            RegexOption.DOT_MATCHES_ALL
        )
        val libraryBundleUsageRegex = Regex(
            """(?:\blibs\.)?implementationBundle\s*\(\s*(?:libs\s*=\s*libs\s*,\s*)?bundle\s*=\s*"([^"]+)"""",
            RegexOption.DOT_MATCHES_ALL
        )
        val appliedPluginRegex = Regex("""pluginManager\.apply\s*\(\s*"([^"]+)"""")
        val includedBuildLibraryAliasRegex = Regex("""\blibs\.([A-Za-z0-9_.]+)\b""")
        val includedBuildPluginAliasRegex = Regex("""alias\s*\(\s*plugins\.plugins\.([A-Za-z0-9_.]+)\s*\)""")
        val mainPluginAliasRegex = Regex("""alias\s*\(\s*plugins\.plugins\.([A-Za-z0-9_.]+)\s*\)""")
        val literalPluginIdRegex = Regex("""\bid\s*\(\s*"([^"]+)"\s*\)""")
        val applyFalseRegex = Regex("""\bapply\s+false\b""")
        val GradleConventionPluginImplementationRegex = Regex(
            "implementationClass\\s*=\\s*\"[^\"]*GradleConventionPlugin\""
        )
    }
}
