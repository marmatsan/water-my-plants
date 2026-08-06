package com.marmatsan.figmaDocumentationSync.plugin.checker.catalog

import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import java.io.File

/**
 * Input snapshot for checking whether declared dependency catalogs contain
 * entries that are not consumed by the repository.
 *
 * @property projectRootDirectory repository root used to resolve catalog consumers.
 * @property primaryCatalogModelName design-model name of the main dependency catalog.
 * @property primaryCatalogTreeSource source that exposes the main catalog contract.
 * @property includedBuilds included-build catalog and convention-plugin sources to inspect.
 */
internal data class CatalogUsageCheckRequest(
    val projectRootDirectory: File,
    val primaryCatalogModelName: String,
    val primaryCatalogTreeSource: ProjectCatalogTreeSource,
    val includedBuilds: List<FigmaDesignModelIncludedBuildSource>
)
