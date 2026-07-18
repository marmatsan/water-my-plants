package com.marmatsan.figmaDesignSync.plugin.checker.catalog

import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import java.io.File

/**
 * Input snapshot for checking whether declared dependency catalogs contain
 * entries that are not consumed by the repository.
 */
internal data class CatalogUsageCheckRequest(
    val projectRootDirectory: File,
    val primaryCatalogModelName: String,
    val dependencyCatalogProviderClassName: String,
    val includedBuilds: List<FigmaDesignModelIncludedBuildSource>
)
