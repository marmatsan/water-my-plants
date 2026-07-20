package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.figmaDocumentationSync.data.gradle.isInsideNestedGradleBuild
import me.tatarka.inject.annotations.Inject
import java.io.File

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
        modulePathPrefix: String,
    ): LibraryUsages {
        val conventionPluginModuleDirs = rootDir.conventionPluginModuleDirs()

        return conventionPluginModuleDirs
            .asSequence()
            .flatMap { moduleDir ->
                moduleDir.kotlinFiles().map { kotlinFile -> moduleDir to kotlinFile }
            }.fold(LibraryUsages()) { usages, (moduleDir, kotlinFile) ->
                usages +
                    kotlinFile.readConventionLibraryUsageDeclarations(
                        includedBuildRootDir = rootDir,
                        modulePathPrefix = modulePathPrefix,
                        moduleDir = moduleDir,
                    )
            }
    }

    fun readConventionLibraryConfigurationUsages(
        rootDir: File,
        modulePathPrefix: String,
    ): LibraryConfigurationUsages {
        val conventionPluginModuleDirs = rootDir.conventionPluginModuleDirs()

        return conventionPluginModuleDirs
            .asSequence()
            .flatMap { moduleDir ->
                moduleDir.kotlinFiles().map { kotlinFile -> moduleDir to kotlinFile }
            }.fold(LibraryConfigurationUsages()) { usages, (moduleDir, kotlinFile) ->
                usages +
                    kotlinFile.readConventionLibraryConfigurationUsageDeclarations(
                        includedBuildRootDir = rootDir,
                        modulePathPrefix = modulePathPrefix,
                        moduleDir = moduleDir,
                    )
            }
    }

    fun readConventionPluginUsages(
        rootDir: File,
        modulePathPrefix: String,
    ): Map<String, Set<String>> {
        val conventionPluginModuleDirs = rootDir.conventionPluginModuleDirs()

        return conventionPluginModuleDirs
            .asSequence()
            .flatMap { moduleDir ->
                moduleDir.kotlinFiles().map { kotlinFile -> moduleDir to kotlinFile }
            }.fold(emptyMap()) { usages, (moduleDir, kotlinFile) ->
                usages.merge(
                    other =
                        kotlinFile.readAppliedPlugins(
                            includedBuildRootDir = rootDir,
                            modulePathPrefix = modulePathPrefix,
                            moduleDir = moduleDir,
                        ),
                )
            }
    }

    fun readConventionPluginIdsByModule(
        rootDir: File,
        modulePathPrefix: String,
    ): Map<String, Set<String>> =
        rootDir
            .conventionPluginModuleDirs()
            .associate { moduleDir ->
                val modulePath =
                    moduleDir.toIncludedBuildModulePath(
                        includedBuildRootDir = rootDir,
                        modulePathPrefix = modulePathPrefix,
                    )
                val pluginIds =
                    moduleDir
                        .resolve(
                            relative = BUILD_FILE_NAME,
                        ).readText()
                        .pluginIds()
                        .toSet()

                modulePath to pluginIds
            }

    fun readIncludedBuildLibraryUsages(
        rootDir: File,
        modulePathPrefix: String,
    ): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(
                    other =
                        buildFile.readIncludedBuildLibraryAliases(
                            includedBuildRootDir = rootDir,
                            modulePathPrefix = modulePathPrefix,
                        ),
                )
            }

    fun readIncludedBuildPluginUsages(
        rootDir: File,
        modulePathPrefix: String,
    ): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .fold(emptyMap()) { usages, buildFile ->
                usages.merge(
                    other =
                        buildFile.readIncludedBuildPluginAliases(
                            includedBuildRootDir = rootDir,
                            modulePathPrefix = modulePathPrefix,
                        ),
                )
            }

    fun readMainLibraryUsages(
        rootDir: File,
    ): LibraryUsages =
        rootDir
            .buildFiles()
            .filterNot { file ->
                file.isInsideNestedGradleBuild(
                    rootDir = rootDir,
                )
            }.fold(LibraryUsages()) { usages, buildFile ->
                usages +
                    buildFile.readMainLibraryAliases(
                        rootDir = rootDir,
                    )
            }

    fun readMainPluginUsages(
        rootDir: File,
    ): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .filterNot { file ->
                file.isInsideNestedGradleBuild(
                    rootDir = rootDir,
                )
            }.fold(emptyMap()) { usages, buildFile ->
                usages.merge(
                    other =
                        buildFile.readMainPluginAliases(
                            rootDir = rootDir,
                        ),
                )
            }

    fun readMainLiteralPluginUsages(
        rootDir: File,
    ): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .filterNot { file ->
                file.isInsideNestedGradleBuild(
                    rootDir = rootDir,
                )
            }.fold(emptyMap()) { usages, buildFile ->
                usages.merge(
                    other =
                        buildFile.readMainLiteralPluginIds(
                            rootDir = rootDir,
                        ),
                )
            }

    fun readMainAppliedLiteralPluginUsages(
        rootDir: File,
    ): Map<String, Set<String>> =
        rootDir
            .buildFiles()
            .filterNot { file ->
                file.isInsideNestedGradleBuild(
                    rootDir = rootDir,
                )
            }.fold(emptyMap()) { usages, buildFile ->
                usages.merge(
                    other =
                        buildFile.readMainAppliedLiteralPluginIds(
                            rootDir = rootDir,
                        ),
                )
            }

    fun readMainAppliedLiteralPluginIds(
        rootDir: File,
    ): Set<String> =
        rootDir
            .buildFiles()
            .filterNot { file ->
                file.isInsideNestedGradleBuild(
                    rootDir = rootDir,
                )
            }.flatMap { buildFile -> buildFile.readAppliedLiteralPluginIds() }
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
        moduleDir: File,
    ): LibraryUsages {
        val modulePath =
            moduleDir.toIncludedBuildModulePath(
                includedBuildRootDir = includedBuildRootDir,
                modulePathPrefix = modulePathPrefix,
            )
        val content = readText()
        val coordinateUsages =
            libraryCoordinateUsageRegex
                .findAll(
                    input = content,
                ).associateToUsageMap(
                    modulePath = modulePath,
                ) { match ->
                    "${match.groupValues[1]}:${match.groupValues[2]}"
                }
        val bundleUsages =
            libraryBundleUsageRegex
                .findAll(
                    input = content,
                ).associateToUsageMap(
                    modulePath = modulePath,
                ) { match -> match.groupValues[1] }

        return LibraryUsages(
            coordinates = coordinateUsages,
            bundles = bundleUsages,
        )
    }

    private fun File.readConventionLibraryConfigurationUsageDeclarations(
        includedBuildRootDir: File,
        modulePathPrefix: String,
        moduleDir: File,
    ): LibraryConfigurationUsages {
        val modulePath =
            moduleDir.toIncludedBuildModulePath(
                includedBuildRootDir = includedBuildRootDir,
                modulePathPrefix = modulePathPrefix,
            )
        val content = readText()
        val coordinateUsages =
            libraryConfigurationUsageRegex
                .findAll(
                    input = content,
                ).fold(emptyMap<String, Set<LibraryConfigurationUsage>>()) { usages, match ->
                    val coordinate = "${match.groupValues[2]}:${match.groupValues[3]}"
                    val usage =
                        LibraryConfigurationUsage(
                            pluginModule = modulePath,
                            target =
                                content.configurationTarget(
                                    match = match,
                                ),
                        )

                    usages + (coordinate to (usages[coordinate].orEmpty() + usage))
                }

        return LibraryConfigurationUsages(
            coordinates = coordinateUsages,
        )
    }

    private fun File.readMainLibraryAliases(
        rootDir: File,
    ): LibraryUsages {
        val modulePath = parentFile.toModulePath(rootDir)
        if (modulePath == ROOT_MODULE) return LibraryUsages()
        val content = readText()
        val libraryUsages =
            mainLibraryAliasRegex
                .findAll(
                    input = content,
                ).associateToUsageMap(
                    modulePath = modulePath,
                ) { match -> match.groupValues[1] }
        val bundleUsages =
            mainLibraryBundleAliasRegex
                .findAll(
                    input = content,
                ).associateToUsageMap(
                    modulePath = modulePath,
                ) { match -> match.groupValues[1] }

        return LibraryUsages(
            aliases = libraryUsages,
            bundles = bundleUsages,
        )
    }

    private fun File.readAppliedPlugins(
        includedBuildRootDir: File,
        modulePathPrefix: String,
        moduleDir: File,
    ): Map<String, Set<String>> {
        val modulePath =
            moduleDir.toIncludedBuildModulePath(
                includedBuildRootDir = includedBuildRootDir,
                modulePathPrefix = modulePathPrefix,
            )

        return appliedPluginRegex
            .findAll(
                input = readText(),
            ).associateToUsageMap(
                modulePath = modulePath,
            ) { match -> match.groupValues[1] }
    }

    private fun File.readIncludedBuildLibraryAliases(
        includedBuildRootDir: File,
        modulePathPrefix: String,
    ): Map<String, Set<String>> {
        val modulePath =
            toIncludedBuildModulePath(
                includedBuildRootDir = includedBuildRootDir,
                modulePathPrefix = modulePathPrefix,
            )

        return includedBuildLibraryAliasRegex
            .findAll(
                input = readText(),
            ).associateToUsageMap(
                modulePath = modulePath,
            ) { match -> match.groupValues[1] }
    }

    private fun File.readIncludedBuildPluginAliases(
        includedBuildRootDir: File,
        modulePathPrefix: String,
    ): Map<String, Set<String>> {
        val modulePath =
            toIncludedBuildModulePath(
                includedBuildRootDir = includedBuildRootDir,
                modulePathPrefix = modulePathPrefix,
            )

        return includedBuildPluginAliasRegex
            .findAll(
                input = readText(),
            ).associateToUsageMap(
                modulePath = modulePath,
            ) { match -> match.groupValues[1] }
    }

    private fun File.readMainPluginAliases(
        rootDir: File,
    ): Map<String, Set<String>> {
        val modulePath = parentFile.toModulePath(rootDir)
        if (modulePath == ROOT_MODULE) return emptyMap()

        return mainPluginAliasRegex
            .findAll(
                input = readText(),
            ).associateToUsageMap(
                modulePath = modulePath,
            ) { match -> match.groupValues[1] }
    }

    private fun File.readMainLiteralPluginIds(
        rootDir: File,
    ): Map<String, Set<String>> {
        val modulePath = parentFile.toModulePath(rootDir)
        if (modulePath == ROOT_MODULE) return emptyMap()

        return literalPluginIdRegex
            .findAll(
                input = readText(),
            ).associateToUsageMap(
                modulePath = modulePath,
            ) { match -> match.groupValues[1] }
    }

    private fun File.readMainAppliedLiteralPluginIds(
        rootDir: File,
    ): Map<String, Set<String>> {
        val modulePath = parentFile.toModulePath(rootDir)
        if (modulePath == ROOT_MODULE) return emptyMap()
        val content = readText()

        return literalPluginIdRegex
            .findAll(
                input = content,
            ).filterNot { match ->
                content
                    .lineSuffixAfter(
                        match = match,
                    ).contains(applyFalseRegex)
            }.associateToUsageMap(
                modulePath = modulePath,
            ) { match -> match.groupValues[1] }
    }

    private fun File.readAppliedLiteralPluginIds(): Sequence<String> {
        val content = readText()

        return literalPluginIdRegex
            .findAll(
                input = content,
            ).filterNot { match ->
                content
                    .lineSuffixAfter(
                        match = match,
                    ).contains(applyFalseRegex)
            }.map { match -> match.groupValues[1] }
    }

    private fun String.lineSuffixAfter(
        match: MatchResult,
    ): String {
        val lineEnd =
            indexOf(
                '\n',
                startIndex = match.range.last + 1,
            ).takeIf { index -> index >= 0 }
                ?: length

        return substring(
            match.range.last + 1,
            lineEnd,
        )
    }

    private fun Sequence<MatchResult>.associateToUsageMap(
        modulePath: String,
        key: (MatchResult) -> String,
    ): Map<String, Set<String>> =
        map(
            transform = key,
        ).fold(emptyMap()) { usages, usageKey ->
            usages + (usageKey to (usages[usageKey].orEmpty() + modulePath))
        }

    private fun Map<String, Set<String>>.merge(
        other: Map<String, Set<String>>,
    ): Map<String, Set<String>> =
        (keys + other.keys).associateWith { key ->
            (this[key].orEmpty() + other[key].orEmpty()).toSortedSet()
        }

    private fun Map<String, Set<LibraryConfigurationUsage>>.mergeConfigurationUsages(
        other: Map<String, Set<LibraryConfigurationUsage>>,
    ): Map<String, Set<LibraryConfigurationUsage>> =
        (keys + other.keys).associateWith { key ->
            (this[key].orEmpty() + other[key].orEmpty()).toSortedSet()
        }

    private operator fun LibraryUsages.plus(
        other: LibraryUsages,
    ): LibraryUsages =
        LibraryUsages(
            coordinates =
                coordinates.merge(
                    other = other.coordinates,
                ),
            bundles =
                bundles.merge(
                    other = other.bundles,
                ),
            aliases =
                aliases.merge(
                    other = other.aliases,
                ),
        )

    private operator fun LibraryConfigurationUsages.plus(
        other: LibraryConfigurationUsages,
    ): LibraryConfigurationUsages =
        LibraryConfigurationUsages(
            coordinates =
                coordinates.mergeConfigurationUsages(
                    other = other.coordinates,
                ),
        )

    private fun String.configurationTarget(
        match: MatchResult,
    ): String {
        val assignmentName = match.groupValues[1]
        val prefix =
            substring(
                0,
                match.range.first,
            )
        val blockNames =
            activeBlockNames(
                contentBeforeMatch = prefix,
            ).filterNot { name -> name in IgnoredConfigurationTargetSegments }

        return (blockNames + assignmentName)
            .filter(String::isNotBlank)
            .joinToString(".")
    }

    private fun activeBlockNames(
        contentBeforeMatch: String,
    ): List<String> {
        var depth = 0
        val stack = mutableListOf<BlockName>()

        configurationBlockTokenRegex
            .findAll(
                input = contentBeforeMatch,
            ).forEach { token ->
                val text = token.value
                when {
                    text == "}" -> {
                        depth = (depth - 1).coerceAtLeast(0)
                        stack.removeAll { block -> block.depth >= depth }
                    }

                    text == "{" -> {
                        depth += 1
                    }

                    else -> {
                        val blockName =
                            token.groupValues[1].ifBlank {
                                token.groupValues[2]
                            }
                        stack +=
                            BlockName(
                                depth = depth,
                                name = blockName,
                            )
                        depth += 1
                    }
                }
            }

        return stack.map(
            transform = BlockName::name,
        )
    }

    private fun File.toIncludedBuildModulePath(
        includedBuildRootDir: File,
        modulePathPrefix: String,
    ): String {
        val moduleDir = if (isDirectory) this else parentFile
        val relativePath = includedBuildRootDir.toPath().relativize(moduleDir.toPath()).toString()

        if (relativePath.isEmpty()) return modulePathPrefix

        return "$modulePathPrefix:${relativePath.toModuleSegments()}"
    }

    private fun File.toModulePath(
        rootDir: File,
    ): String {
        val relativePath = rootDir.toPath().relativize(toPath()).toString()

        if (relativePath.isEmpty()) return ROOT_MODULE

        return ":${relativePath.toModuleSegments()}"
    }

    private fun String.toModuleSegments(): String =
        replace(
            File.separatorChar,
            ':',
        ).replace(
            '/',
            ':',
        ).replace(
            '\\',
            ':',
        )

    private fun String.hasGradleConventionPluginImplementation(): Boolean =
        GradleConventionPluginImplementationRegex.containsMatchIn(this)

    private fun String.pluginIds(): Sequence<String> =
        sequenceOf(
            pluginNameRegex,
            pluginIdRegex,
        ).flatMap { regex ->
            regex.findAll(
                input = this,
            )
        }.map { match -> match.groupValues[1] }

    /**
     * Usage index for libraries declared as direct coordinates or bundles.
     *
     * Values are Gradle module paths that reference each key.
     */
    data class LibraryUsages(
        val coordinates: Map<String, Set<String>> = emptyMap(),
        val bundles: Map<String, Set<String>> = emptyMap(),
        val aliases: Map<String, Set<String>> = emptyMap(),
    )

    data class LibraryConfigurationUsages(
        val coordinates: Map<String, Set<LibraryConfigurationUsage>> = emptyMap(),
    )

    data class LibraryConfigurationUsage(
        val pluginModule: String,
        val target: String,
    ) : Comparable<LibraryConfigurationUsage> {
        override fun compareTo(
            other: LibraryConfigurationUsage,
        ): Int =
            compareValuesBy(
                this,
                other,
                LibraryConfigurationUsage::pluginModule,
                LibraryConfigurationUsage::target,
            )
    }

    private data class BlockName(
        val depth: Int,
        val name: String,
    )

    private companion object {
        const val BUILD_FILE_NAME = "build.gradle.kts"
        const val KOTLIN_FILE_EXTENSION = "kt"
        const val ROOT_MODULE = ":"

        val libraryCoordinateUsageRegex =
            Regex(
                """(?:\blibs\.)?(?:implementation|implementationPlatform|testImplementation|testImplementationPlatform|testRuntimeOnly|ksp)\s*\(\s*(?:libs\s*=\s*libs\s*,\s*)?libraryGroup\s*=\s*"([^"]+)"\s*,\s*artifact\s*=\s*"([^"]+)"""",
                RegexOption.DOT_MATCHES_ALL,
            )
        val libraryBundleUsageRegex =
            Regex(
                """(?:\blibs\.)?implementationBundle\s*\(\s*(?:libs\s*=\s*libs\s*,\s*)?bundle\s*=\s*"([^"]+)"""",
                RegexOption.DOT_MATCHES_ALL,
            )
        val libraryConfigurationUsageRegex =
            Regex(
                """([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(?:[A-Za-z_][A-Za-z0-9_]*\.)?requireDependencyNotation\s*\(\s*(?:libs\s*=\s*libs\s*,\s*)?libraryGroup\s*=\s*"([^"]+)"\s*,\s*artifact\s*=\s*"([^"]+)"""",
                RegexOption.DOT_MATCHES_ALL,
            )
        val configurationBlockTokenRegex =
            Regex(
                """configure<[^>]+>\s*\(\s*"([^"]+)"\s*\)\s*\{|([A-Za-z_][A-Za-z0-9_]*)\s*\{|[{}]""",
            )
        val IgnoredConfigurationTargetSegments =
            setOf(
                "apply",
                "dependencies",
                "project",
                "tasks",
            )
        val mainLibraryBundleAliasRegex = Regex("""\blibs\.bundles\.([A-Za-z0-9_.]+)\b""")
        val mainLibraryAliasRegex = Regex("""\blibs\.(?!bundles\.)([A-Za-z0-9_.]+)\b""")
        val appliedPluginRegex = Regex("""pluginManager\.apply\s*\(\s*"([^"]+)"""")
        val includedBuildLibraryAliasRegex = Regex("""\blibs\.([A-Za-z0-9_.]+)\b""")
        val includedBuildPluginAliasRegex = Regex("""alias\s*\(\s*plugins\.plugins\.([A-Za-z0-9_.]+)\s*\)""")
        val mainPluginAliasRegex = Regex("""alias\s*\(\s*plugins\.plugins\.([A-Za-z0-9_.]+)\s*\)""")
        val literalPluginIdRegex = Regex("""\bid\s*\(\s*"([^"]+)"\s*\)""")
        val applyFalseRegex = Regex("""\bapply\s+false\b""")
        val pluginNameRegex = Regex("val\\s+pluginName\\s*=\\s*\"([^\"]+)\"")
        val pluginIdRegex = Regex("id\\s*=\\s*\"([^\"]+)\"")
        val GradleConventionPluginImplementationRegex =
            Regex(
                "implementationClass\\s*=\\s*\"[^\"]*GradleConventionPlugin\"",
            )
    }
}
