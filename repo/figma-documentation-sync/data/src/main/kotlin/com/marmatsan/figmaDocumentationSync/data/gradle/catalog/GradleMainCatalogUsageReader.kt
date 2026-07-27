package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import me.tatarka.inject.annotations.Inject
import java.io.File

/** Reads catalog and literal plugin usage from the consuming repository's main Gradle build. */
@Inject
class GradleMainCatalogUsageReader {
    private val scanner = GradleCatalogSourceScanner()
    private val parser = GradleCatalogUsageParser(scanner)

    /** Reads library and bundle aliases consumed by main-build modules. */
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

    /** Reads plugin catalog aliases consumed by main-build modules. */
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

    /** Reads literal plugin ids declared by main-build modules. */
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

    /** Reads literal plugin ids applied by main-build modules. */
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

    /** Returns all literal plugin ids applied in the main build. */
    fun readAppliedLiteralPluginIds(
        rootDir: File,
    ): Set<String> =
        scanner
            .mainBuildFiles(rootDir)
            .flatMap(parser::appliedLiteralPluginIds)
            .toSet()
}
