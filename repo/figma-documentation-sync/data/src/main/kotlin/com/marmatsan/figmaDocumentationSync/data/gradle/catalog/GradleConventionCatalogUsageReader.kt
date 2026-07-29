package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import me.tatarka.inject.annotations.Inject
import java.io.File

/** Reads catalog and plugin usage contributed by convention-plugin modules in one included build. */
@Inject
class GradleConventionCatalogUsageReader {
    private val scanner = GradleCatalogSourceScanner()
    private val parser = GradleCatalogUsageParser(scanner)

    /** Reads direct coordinates and bundles used by convention-plugin implementations. */
    fun readLibraryUsages(
        rootDir: File,
        modulePathPrefix: String
    ): LibraryUsages =
        conventionPluginSources(
            rootDir = rootDir
        ).fold(LibraryUsages()) { usages, (moduleDir, kotlinFile) ->
            usages +
                parser.conventionLibraryUsages(
                    kotlinFile = kotlinFile,
                    rootDir = rootDir,
                    modulePathPrefix = modulePathPrefix,
                    moduleDir = moduleDir
                )
        }

    /** Reads configuration assignments contributed by convention-plugin implementations. */
    fun readLibraryConfigurationUsages(
        rootDir: File,
        modulePathPrefix: String
    ): LibraryConfigurationUsages =
        conventionPluginSources(
            rootDir = rootDir
        ).fold(LibraryConfigurationUsages()) { usages, (moduleDir, kotlinFile) ->
            usages +
                parser.conventionLibraryConfigurationUsages(
                    kotlinFile = kotlinFile,
                    rootDir = rootDir,
                    modulePathPrefix = modulePathPrefix,
                    moduleDir = moduleDir
                )
        }

    /** Reads plugins applied programmatically by convention-plugin implementations. */
    fun readPluginUsages(
        rootDir: File,
        modulePathPrefix: String
    ): Map<String, Set<String>> =
        conventionPluginSources(
            rootDir = rootDir
        ).fold(emptyMap()) { usages, (moduleDir, kotlinFile) ->
            usages.mergeUsageSets(
                parser.appliedPlugins(
                    kotlinFile = kotlinFile,
                    rootDir = rootDir,
                    modulePathPrefix = modulePathPrefix,
                    moduleDir = moduleDir
                )
            )
        }

    /** Reads published plugin ids keyed by convention-plugin module path. */
    fun readPluginIdsByModule(
        rootDir: File,
        modulePathPrefix: String
    ): Map<String, Set<String>> =
        conventionPluginModuleDirs(
            rootDir = rootDir
        ).associate { moduleDir ->
            scanner.includedBuildModulePath(
                moduleDir,
                rootDir,
                modulePathPrefix
            ) to
                parser.conventionPluginIds(moduleDir.resolve(BUILD_FILE_NAME))
        }

    private fun conventionPluginSources(
        rootDir: File
    ): Sequence<Pair<File, File>> =
        conventionPluginModuleDirs(
            rootDir = rootDir
        ).asSequence().flatMap { moduleDir ->
            scanner.kotlinFiles(moduleDir).map { kotlinFile -> moduleDir to kotlinFile }
        }

    private fun conventionPluginModuleDirs(
        rootDir: File
    ): Set<File> =
        scanner
            .buildFiles(rootDir)
            .filter(parser::isConventionPluginBuildFile)
            .map(File::getParentFile)
            .toSet()

    private companion object {
        const val BUILD_FILE_NAME = "build.gradle.kts"
    }
}
