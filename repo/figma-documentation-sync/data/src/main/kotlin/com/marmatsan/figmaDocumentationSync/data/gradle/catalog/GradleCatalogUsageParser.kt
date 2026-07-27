package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import java.io.File

/** Parses catalog and plugin usage declarations from individual Gradle or Kotlin source files. */
internal class GradleCatalogUsageParser(
    private val scanner: GradleCatalogSourceScanner,
) {
    fun conventionLibraryUsages(
        kotlinFile: File,
        rootDir: File,
        modulePathPrefix: String,
        moduleDir: File,
    ): LibraryUsages {
        val modulePath =
            scanner.includedBuildModulePath(
                moduleDir,
                rootDir,
                modulePathPrefix,
            )
        val content = kotlinFile.readText()
        return LibraryUsages(
            coordinates =
                libraryCoordinateUsageRegex
                    .findAll(content)
                    .associateToUsageMap(modulePath) { match ->
                        "${match.groupValues[1]}:${match.groupValues[2]}"
                    },
            bundles =
                libraryBundleUsageRegex
                    .findAll(content)
                    .associateToUsageMap(modulePath) { match -> match.groupValues[1] },
        )
    }

    fun conventionLibraryConfigurationUsages(
        kotlinFile: File,
        rootDir: File,
        modulePathPrefix: String,
        moduleDir: File,
    ): LibraryConfigurationUsages {
        val modulePath =
            scanner.includedBuildModulePath(
                moduleDir,
                rootDir,
                modulePathPrefix,
            )
        val content = kotlinFile.readText()
        val coordinateUsages =
            libraryConfigurationUsageRegex
                .findAll(content)
                .fold(emptyMap<String, Set<LibraryConfigurationUsage>>()) { usages, match ->
                    val coordinate = "${match.groupValues[2]}:${match.groupValues[3]}"
                    val usage =
                        LibraryConfigurationUsage(
                            pluginModule = modulePath,
                            target = content.configurationTarget(match),
                        )
                    usages + (coordinate to (usages[coordinate].orEmpty() + usage))
                }
        return LibraryConfigurationUsages(
            coordinates = coordinateUsages,
        )
    }

    fun appliedPlugins(
        kotlinFile: File,
        rootDir: File,
        modulePathPrefix: String,
        moduleDir: File,
    ): Map<String, Set<String>> =
        appliedPluginRegex
            .findAll(kotlinFile.readText())
            .associateToUsageMap(
                scanner.includedBuildModulePath(
                    moduleDir,
                    rootDir,
                    modulePathPrefix,
                ),
            ) { match -> match.groupValues[1] }

    fun includedBuildLibraryAliases(
        buildFile: File,
        rootDir: File,
        modulePathPrefix: String,
    ): Map<String, Set<String>> =
        includedBuildLibraryAliasRegex
            .findAll(buildFile.readText())
            .associateToUsageMap(
                scanner.includedBuildModulePath(
                    buildFile,
                    rootDir,
                    modulePathPrefix,
                ),
            ) { match -> match.groupValues[1] }

    fun includedBuildPluginAliases(
        buildFile: File,
        rootDir: File,
        modulePathPrefix: String,
    ): Map<String, Set<String>> =
        includedBuildPluginAliasRegex
            .findAll(buildFile.readText())
            .associateToUsageMap(
                scanner.includedBuildModulePath(
                    buildFile,
                    rootDir,
                    modulePathPrefix,
                ),
            ) { match -> match.groupValues[1] }

    fun mainLibraryAliases(
        buildFile: File,
        rootDir: File,
    ): LibraryUsages {
        val modulePath =
            scanner.mainModulePath(
                buildFile.parentFile,
                rootDir,
            )
        if (modulePath == GradleCatalogSourceScanner.ROOT_MODULE) return LibraryUsages()
        val content = buildFile.readText()
        return LibraryUsages(
            aliases =
                mainLibraryAliasRegex
                    .findAll(content)
                    .associateToUsageMap(modulePath) { match -> match.groupValues[1] },
            bundles =
                mainLibraryBundleAliasRegex
                    .findAll(content)
                    .associateToUsageMap(modulePath) { match -> match.groupValues[1] },
        )
    }

    fun mainPluginAliases(
        buildFile: File,
        rootDir: File,
    ): Map<String, Set<String>> =
        mainModuleUsages(
            buildFile = buildFile,
            rootDir = rootDir,
            regex = mainPluginAliasRegex,
        )

    fun mainLiteralPluginUsages(
        buildFile: File,
        rootDir: File,
    ): Map<String, Set<String>> =
        mainModuleUsages(
            buildFile = buildFile,
            rootDir = rootDir,
            regex = literalPluginIdRegex,
        )

    fun mainAppliedLiteralPluginUsages(
        buildFile: File,
        rootDir: File,
    ): Map<String, Set<String>> {
        val modulePath =
            scanner.mainModulePath(
                buildFile.parentFile,
                rootDir,
            )
        if (modulePath == GradleCatalogSourceScanner.ROOT_MODULE) return emptyMap()
        val content = buildFile.readText()
        return appliedLiteralPluginMatches(
            content = content,
        ).associateToUsageMap(modulePath) { match ->
            match.groupValues[1]
        }
    }

    fun appliedLiteralPluginIds(
        buildFile: File,
    ): Sequence<String> =
        appliedLiteralPluginMatches(
            content = buildFile.readText(),
        ).map { match -> match.groupValues[1] }

    fun isConventionPluginBuildFile(
        buildFile: File,
    ): Boolean = gradleConventionPluginImplementationRegex.containsMatchIn(buildFile.readText())

    fun conventionPluginIds(
        buildFile: File,
    ): Set<String> {
        val content = buildFile.readText()
        return sequenceOf(
            pluginNameRegex,
            pluginIdRegex,
        ).flatMap { regex -> regex.findAll(content) }
            .map { match -> match.groupValues[1] }
            .toSet()
    }

    private fun mainModuleUsages(
        buildFile: File,
        rootDir: File,
        regex: Regex,
    ): Map<String, Set<String>> {
        val modulePath =
            scanner.mainModulePath(
                buildFile.parentFile,
                rootDir,
            )
        if (modulePath == GradleCatalogSourceScanner.ROOT_MODULE) return emptyMap()
        return regex
            .findAll(buildFile.readText())
            .associateToUsageMap(modulePath) { match -> match.groupValues[1] }
    }

    private fun appliedLiteralPluginMatches(
        content: String,
    ): Sequence<MatchResult> =
        literalPluginIdRegex
            .findAll(content)
            .filterNot { match -> content.lineSuffixAfter(match).contains(applyFalseRegex) }

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
        map(key).fold(emptyMap()) { usages, usageKey ->
            usages + (usageKey to (usages[usageKey].orEmpty() + modulePath))
        }

    private fun String.configurationTarget(
        match: MatchResult,
    ): String {
        val assignmentName = match.groupValues[1]
        val blockNames =
            activeBlockNames(
                contentBeforeMatch =
                    substring(
                        0,
                        match.range.first,
                    ),
            ).filterNot { name -> name in IGNORED_CONFIGURATION_TARGET_SEGMENTS }
        return (blockNames + assignmentName)
            .filter(String::isNotBlank)
            .joinToString(".")
    }

    private fun activeBlockNames(
        contentBeforeMatch: String,
    ): List<String> {
        var depth = 0
        val stack = mutableListOf<BlockName>()
        configurationBlockTokenRegex.findAll(contentBeforeMatch).forEach { token ->
            when (token.value) {
                "}" -> {
                    depth = (depth - 1).coerceAtLeast(0)
                    stack.removeAll { block -> block.depth >= depth }
                }

                "{" -> {
                    depth += 1
                }

                else -> {
                    val blockName = token.groupValues[1].ifBlank { token.groupValues[2] }
                    stack +=
                        BlockName(
                            depth = depth,
                            name = blockName,
                        )
                    depth += 1
                }
            }
        }
        return stack.map(BlockName::name)
    }

    private data class BlockName(
        val depth: Int,
        val name: String,
    )

    private companion object {
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
            Regex("""configure<[^>]+>\s*\(\s*"([^"]+)"\s*\)\s*\{|([A-Za-z_][A-Za-z0-9_]*)\s*\{|[{}]""")
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
        val gradleConventionPluginImplementationRegex =
            Regex("implementationClass\\s*=\\s*\"[^\"]*GradleConventionPlugin\"")
        val IGNORED_CONFIGURATION_TARGET_SEGMENTS =
            setOf(
                "apply",
                "dependencies",
                "project",
                "tasks",
            )
    }
}
