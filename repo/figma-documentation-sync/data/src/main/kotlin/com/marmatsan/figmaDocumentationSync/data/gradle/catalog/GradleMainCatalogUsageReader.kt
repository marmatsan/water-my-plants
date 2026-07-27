package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import me.tatarka.inject.annotations.Inject
import java.io.File

/** Reads catalog and literal plugin usage from the consuming repository's main Gradle build. */
@Inject
class GradleMainCatalogUsageReader {
    private val scanner = GradleCatalogSourceScanner()
    private val parser = GradleCatalogUsageParser(scanner)

    fun readLibraryUsages(
        rootDir: File,
    ): LibraryUsages =
        scanner.mainBuildFiles(rootDir).fold(LibraryUsages()) { usages, buildFile ->
            usages +
                parser.mainLibraryAliases(
                    buildFile,
                    rootDir,
                )
        }

    fun readPluginUsages(
        rootDir: File,
    ): Map<String, Set<String>> =
        scanner.mainBuildFiles(rootDir).fold(emptyMap()) { usages, buildFile ->
            usages.mergeUsageSets(
                parser.mainPluginAliases(
                    buildFile,
                    rootDir,
                ),
            )
        }

    fun readLiteralPluginUsages(
        rootDir: File,
    ): Map<String, Set<String>> =
        scanner.mainBuildFiles(rootDir).fold(emptyMap()) { usages, buildFile ->
            usages.mergeUsageSets(
                parser.mainLiteralPluginUsages(
                    buildFile,
                    rootDir,
                ),
            )
        }

    fun readAppliedLiteralPluginUsages(
        rootDir: File,
    ): Map<String, Set<String>> =
        scanner.mainBuildFiles(rootDir).fold(emptyMap()) { usages, buildFile ->
            usages.mergeUsageSets(
                parser.mainAppliedLiteralPluginUsages(
                    buildFile,
                    rootDir,
                ),
            )
        }

    fun readAppliedLiteralPluginIds(
        rootDir: File,
    ): Set<String> =
        scanner
            .mainBuildFiles(rootDir)
            .flatMap(parser::appliedLiteralPluginIds)
            .toSet()
}
