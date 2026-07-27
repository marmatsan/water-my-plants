package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import me.tatarka.inject.annotations.Inject
import java.io.File

/** Reads catalog aliases consumed by modules inside one included Gradle build. */
@Inject
class GradleIncludedBuildCatalogUsageReader {
    private val scanner = GradleCatalogSourceScanner()
    private val parser = GradleCatalogUsageParser(scanner)

    fun readLibraryUsages(
        rootDir: File,
        modulePathPrefix: String,
    ): Map<String, Set<String>> =
        scanner.buildFiles(rootDir).fold(emptyMap()) { usages, buildFile ->
            usages.mergeUsageSets(
                parser.includedBuildLibraryAliases(
                    buildFile,
                    rootDir,
                    modulePathPrefix,
                ),
            )
        }

    fun readPluginUsages(
        rootDir: File,
        modulePathPrefix: String,
    ): Map<String, Set<String>> =
        scanner.buildFiles(rootDir).fold(emptyMap()) { usages, buildFile ->
            usages.mergeUsageSets(
                parser.includedBuildPluginAliases(
                    buildFile,
                    rootDir,
                    modulePathPrefix,
                ),
            )
        }
}
