package com.marmatsan.dependencies.catalog.version

import java.io.File
import java.util.Properties

/**
 * Resolves dependency versions from one properties file supplied by [source].
 *
 * The source is evaluated lazily so Gradle adapters may finish configuring their file property
 * before the first version lookup. Once a value is resolved, the canonical file is fixed for the
 * lifetime of this resolver.
 *
 * @property source Supplies the consumer-owned properties file.
 */
class PropertiesDependencyVersionResolver(
    private val source: () -> File
) : DependencyVersionResolver {
    private var loadedVersions: LoadedVersions? = null

    /** Creates a resolver backed by one fixed [source] file. */
    constructor(
        source: File
    ) : this(
        source = { source }
    )

    /**
     * Returns the value associated with [key] in the configured properties file.
     *
     * The first resolution fixes the source file for the current settings evaluation so a caller
     * cannot accidentally combine independently owned registries in one catalog.
     */
    override fun resolve(
        key: String
    ): String {
        val file = source()
        val versions =
            loadedVersions
                ?: load(
                    file = file
                ).also { loadedVersions = it }
        require(versions.file == file.canonicalFile) {
            "Dependency versions file cannot change after its first value is resolved"
        }
        return versions.properties.getProperty(key)
            ?: error("Missing version property '$key' in ${versions.file.path}")
    }

    private fun load(
        file: File
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
                }
        )
    }
}

/** Canonical versions source and its properties cached for one settings evaluation. */
private data class LoadedVersions(
    val file: File,
    val properties: Properties
)
