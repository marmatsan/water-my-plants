package com.marmatsan.dependencies.gradle.tree.version

import java.io.File
import java.util.Properties

/** Resolves dependency versions from one settings-owned properties file. */
internal class PropertiesDependencyVersionResolver {
    private var loadedVersions: LoadedVersions? = null

    /**
     * Returns the value associated with [key] in [file].
     *
     * The first resolution fixes the source file for the current settings evaluation so a caller
     * cannot accidentally combine independently owned registries in one catalog.
     */
    fun resolve(
        file: File,
        key: String,
    ): String {
        val versions =
            loadedVersions
                ?: load(
                    file = file,
                ).also { loadedVersions = it }
        require(versions.file == file.canonicalFile) {
            "Dependency versions file cannot change after its first value is resolved"
        }
        return versions.properties.getProperty(key)
            ?: error("Missing version property '$key' in ${versions.file.path}")
    }

    private fun load(
        file: File,
    ): LoadedVersions {
        val canonicalFile = file.canonicalFile
        require(canonicalFile.isFile) {
            "Dependency versions file does not exist: ${canonicalFile.path}"
        }
        return LoadedVersions(
            file = canonicalFile,
            properties =
                Properties().apply {
                    canonicalFile.inputStream().use(::load)
                },
        )
    }
}

/** Canonical versions source and its properties cached for one settings evaluation. */
private data class LoadedVersions(
    val file: File,
    val properties: Properties,
)
