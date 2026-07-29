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
        rootDir: File
    ): LibraryUsages =
        scanner.mainBuildFiles(rootDir).fold(LibraryUsages()) { usages, buildFile ->
            usages +
                parser.mainLibraryAliases(
                    buildFile,
                    rootDir
                )
        }

    /** Reads plugin catalog aliases consumed by main-build modules. */
    fun readPluginUsages(
        rootDir: File
    ): Map<String, Set<String>> =
        scanner.mainBuildFiles(rootDir).fold(emptyMap()) { usages, buildFile ->
            usages.mergeUsageSets(
                parser.mainPluginAliases(
                    buildFile,
                    rootDir
                )
            )
        }

    /**
     * Reads every plugin actually applied by main-build modules, combining
     * type-safe catalog aliases and literal plugin ids.
     */
    fun readAppliedPluginUsages(
        rootDir: File
    ): Map<String, Set<String>> =
        scanner.mainBuildFiles(rootDir).fold(emptyMap()) { usages, buildFile ->
            usages
                .mergeUsageSets(
                    parser.mainAppliedPluginAliases(
                        buildFile,
                        rootDir
                    )
                ).mergeUsageSets(
                    parser.mainAppliedLiteralPluginUsages(
                        buildFile,
                        rootDir
                    )
                )
        }
}
