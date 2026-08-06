package com.marmatsan.figmaDocumentationSync.plugin.checker.sync

import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import java.io.File
import java.time.Instant

/**
 * Input snapshot used to verify that the Figma document is synced with the
 * current repository state.
 *
 * The source fields mirror [com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelGenerationRequest],
 * with the additional Figma metadata node URL and API token needed to read
 * shared plugin data.
 *
 * @property metadataNodeUrl Figma metadata node queried for shared plugin data.
 * @property token Figma REST token used only to read the metadata node.
 * @property metadataNamespace shared plugin-data namespace containing sync identity.
 * @property branch repository branch expected in the generated model.
 * @property gitSha repository revision expected in Figma metadata.
 * @property generatedAt generation timestamp used to reproduce the expected model.
 * @property primaryCatalogModelName main dependency catalog name in the design model.
 * @property primaryCatalogTreeSource main dependency catalog tree source.
 * @property ciDocumentationEnabled whether CI documentation contributes to the model.
 * @property ciConfigurationModelName optional design-model name for generated CI configuration.
 * @property ciConfigurationProviderClassName optional provider for generated CI configuration.
 * @property versionsFile source of repository version sections.
 * @property rootSettingsFile main Gradle settings file used to discover modules.
 * @property ciExternalTopologyFile optional external CI topology contract.
 * @property ciWindowsRuntimeFile optional Windows CI runtime contract.
 * @property ciGeneratedConfigurationDirectory optional generated TeamCity configuration root.
 * @property projectRootDirectory repository root used to resolve all relative inputs.
 * @property includedBuilds explicitly configured included-build sources.
 */
internal data class FigmaTrunkSyncCheckRequest(
    val metadataNodeUrl: String,
    val token: String,
    val metadataNamespace: String,
    val branch: String,
    val gitSha: String,
    val generatedAt: Instant,
    val primaryCatalogModelName: String,
    val primaryCatalogTreeSource: ProjectCatalogTreeSource,
    val ciDocumentationEnabled: Boolean,
    val ciConfigurationModelName: String?,
    val ciConfigurationProviderClassName: String?,
    val versionsFile: File,
    val rootSettingsFile: File,
    val ciExternalTopologyFile: File?,
    val ciWindowsRuntimeFile: File?,
    val ciGeneratedConfigurationDirectory: File?,
    val projectRootDirectory: File,
    val includedBuilds: List<FigmaDesignModelIncludedBuildSource>
)
