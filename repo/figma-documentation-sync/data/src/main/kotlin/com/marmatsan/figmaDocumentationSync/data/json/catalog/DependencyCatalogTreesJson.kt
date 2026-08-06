package com.marmatsan.figmaDocumentationSync.data.json.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.DependencyCatalogTrees
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Language-neutral serialization boundary for preconfigured dependency catalog trees. */
object DependencyCatalogTreesJson {
    private val json =
        Json {
            encodeDefaults = true
            explicitNulls = true
        }

    /** Encodes [trees] as a deterministic Gradle task input. */
    fun encode(
        trees: DependencyCatalogTrees
    ): String = json.encodeToString(DependencyCatalogTreesDto.from(trees))

    /** Decodes a previously encoded dependency catalog [value]. */
    fun decode(
        value: String
    ): DependencyCatalogTrees = json.decodeFromString<DependencyCatalogTreesDto>(value).toDomain()

    @Serializable
    private data class DependencyCatalogTreesDto(
        val libraries: LibraryCatalogTreeDto,
        val plugins: PluginCatalogTreeDto
    ) {
        fun toDomain(): DependencyCatalogTrees =
            DependencyCatalogTrees(
                libraries = libraries.toDomain(),
                plugins = plugins.toDomain()
            )

        companion object {
            fun from(
                trees: DependencyCatalogTrees
            ): DependencyCatalogTreesDto =
                DependencyCatalogTreesDto(
                    libraries = LibraryCatalogTreeDto.from(trees.libraries),
                    plugins = PluginCatalogTreeDto.from(trees.plugins)
                )
        }
    }

    @Serializable
    private data class LibraryCatalogTreeDto(
        val roots: List<LibraryCatalogNodeDto>
    ) {
        fun toDomain(): LibraryCatalogTree = LibraryCatalogTree(roots.map(LibraryCatalogNodeDto::toDomain))

        companion object {
            fun from(
                tree: LibraryCatalogTree
            ): LibraryCatalogTreeDto =
                LibraryCatalogTreeDto(
                    roots = tree.roots.map(LibraryCatalogNodeDto::from)
                )
        }
    }

    @Serializable
    private data class LibraryCatalogNodeDto(
        val group: String,
        val entries: List<LibraryCatalogEntryDto>,
        val artifactsVisible: Boolean,
        val children: List<LibraryCatalogNodeDto>
    ) {
        fun toDomain(): LibraryCatalogNode =
            LibraryCatalogNode(
                group = group,
                entries = entries.map(LibraryCatalogEntryDto::toDomain),
                artifactsVisible = artifactsVisible,
                children = children.map(LibraryCatalogNodeDto::toDomain)
            )

        companion object {
            fun from(
                node: LibraryCatalogNode
            ): LibraryCatalogNodeDto =
                LibraryCatalogNodeDto(
                    group = node.group,
                    entries = node.entries.map(LibraryCatalogEntryDto::from),
                    artifactsVisible = node.artifactsVisible,
                    children = node.children.map(LibraryCatalogNodeDto::from)
                )
        }
    }

    @Serializable
    private data class LibraryCatalogEntryDto(
        val kind: String,
        val artifact: String? = null,
        val alias: String? = null,
        val artifacts: List<String> = emptyList(),
        val version: CatalogVersionDto,
        val requiredByModules: List<String>,
        val providedByConventionPlugins: List<LibraryConventionPluginUsageDto>,
        val configuredByConventionPlugins: List<LibraryConventionPluginConfigurationUsageDto> = emptyList()
    ) {
        fun toDomain(): LibraryCatalogEntry =
            when (kind) {
                ARTIFACT_KIND -> {
                    LibraryCatalogEntry.Artifact(
                        artifact = requireNotNull(artifact),
                        version = version.toDomain(),
                        requiredByModules = requiredByModules,
                        providedByConventionPlugins =
                            providedByConventionPlugins.map(LibraryConventionPluginUsageDto::toDomain),
                        configuredByConventionPlugins =
                            configuredByConventionPlugins.map(
                                LibraryConventionPluginConfigurationUsageDto::toDomain
                            )
                    )
                }

                BUNDLE_KIND -> {
                    LibraryCatalogEntry.ArtifactsBundle(
                        alias = requireNotNull(alias),
                        artifacts = artifacts,
                        version = version.toDomain(),
                        requiredByModules = requiredByModules,
                        providedByConventionPlugins =
                            providedByConventionPlugins.map(LibraryConventionPluginUsageDto::toDomain)
                    )
                }

                else -> {
                    error("Unsupported dependency catalog entry kind '$kind'.")
                }
            }

        companion object {
            private const val ARTIFACT_KIND = "artifact"
            private const val BUNDLE_KIND = "bundle"

            fun from(
                entry: LibraryCatalogEntry
            ): LibraryCatalogEntryDto =
                when (entry) {
                    is LibraryCatalogEntry.Artifact -> {
                        LibraryCatalogEntryDto(
                            kind = ARTIFACT_KIND,
                            artifact = entry.artifact,
                            version = CatalogVersionDto.from(entry.version),
                            requiredByModules = entry.requiredByModules,
                            providedByConventionPlugins =
                                entry.providedByConventionPlugins.map(LibraryConventionPluginUsageDto::from),
                            configuredByConventionPlugins =
                                entry.configuredByConventionPlugins.map(
                                    LibraryConventionPluginConfigurationUsageDto::from
                                )
                        )
                    }

                    is LibraryCatalogEntry.ArtifactsBundle -> {
                        LibraryCatalogEntryDto(
                            kind = BUNDLE_KIND,
                            alias = entry.alias,
                            artifacts = entry.artifacts,
                            version = CatalogVersionDto.from(entry.version),
                            requiredByModules = entry.requiredByModules,
                            providedByConventionPlugins =
                                entry.providedByConventionPlugins.map(LibraryConventionPluginUsageDto::from)
                        )
                    }
                }
        }
    }

    @Serializable
    private data class LibraryConventionPluginUsageDto(
        val pluginId: String,
        val pluginModule: String,
        val requiredByModules: List<String>
    ) {
        fun toDomain(): LibraryCatalogEntry.ConventionPluginUsage =
            LibraryCatalogEntry.ConventionPluginUsage(
                pluginId = pluginId,
                pluginModule = pluginModule,
                requiredByModules = requiredByModules
            )

        companion object {
            fun from(
                usage: LibraryCatalogEntry.ConventionPluginUsage
            ): LibraryConventionPluginUsageDto =
                LibraryConventionPluginUsageDto(
                    pluginId = usage.pluginId,
                    pluginModule = usage.pluginModule,
                    requiredByModules = usage.requiredByModules
                )
        }
    }

    @Serializable
    private data class LibraryConventionPluginConfigurationUsageDto(
        val pluginId: String,
        val pluginModule: String,
        val target: String
    ) {
        fun toDomain(): LibraryCatalogEntry.ConventionPluginConfigurationUsage =
            LibraryCatalogEntry.ConventionPluginConfigurationUsage(
                pluginId = pluginId,
                pluginModule = pluginModule,
                target = target
            )

        companion object {
            fun from(
                usage: LibraryCatalogEntry.ConventionPluginConfigurationUsage
            ): LibraryConventionPluginConfigurationUsageDto =
                LibraryConventionPluginConfigurationUsageDto(
                    pluginId = usage.pluginId,
                    pluginModule = usage.pluginModule,
                    target = usage.target
                )
        }
    }

    @Serializable
    private data class PluginCatalogTreeDto(
        val roots: List<PluginCatalogNodeDto>
    ) {
        fun toDomain(): PluginCatalogTree = PluginCatalogTree(roots.map(PluginCatalogNodeDto::toDomain))

        companion object {
            fun from(
                tree: PluginCatalogTree
            ): PluginCatalogTreeDto =
                PluginCatalogTreeDto(
                    roots = tree.roots.map(PluginCatalogNodeDto::from)
                )
        }
    }

    @Serializable
    private data class PluginCatalogNodeDto(
        val id: String,
        val version: CatalogVersionDto?,
        val appliedToModules: List<String>,
        val providedByConventionPlugins: List<PluginConventionPluginUsageDto>,
        val children: List<PluginCatalogNodeDto>
    ) {
        fun toDomain(): PluginCatalogNode =
            PluginCatalogNode(
                id = id,
                version = version?.toDomain(),
                appliedToModules = appliedToModules,
                providedByConventionPlugins = providedByConventionPlugins.map(PluginConventionPluginUsageDto::toDomain),
                children = children.map(PluginCatalogNodeDto::toDomain)
            )

        companion object {
            fun from(
                node: PluginCatalogNode
            ): PluginCatalogNodeDto =
                PluginCatalogNodeDto(
                    id = node.id,
                    version = node.version?.let(CatalogVersionDto::from),
                    appliedToModules = node.appliedToModules,
                    providedByConventionPlugins =
                        node.providedByConventionPlugins.map(PluginConventionPluginUsageDto::from),
                    children = node.children.map(PluginCatalogNodeDto::from)
                )
        }
    }

    @Serializable
    private data class PluginConventionPluginUsageDto(
        val pluginId: String,
        val pluginModule: String,
        val requiredByModules: List<String>
    ) {
        fun toDomain(): PluginCatalogNode.ConventionPluginUsage =
            PluginCatalogNode.ConventionPluginUsage(
                pluginId = pluginId,
                pluginModule = pluginModule,
                requiredByModules = requiredByModules
            )

        companion object {
            fun from(
                usage: PluginCatalogNode.ConventionPluginUsage
            ): PluginConventionPluginUsageDto =
                PluginConventionPluginUsageDto(
                    pluginId = usage.pluginId,
                    pluginModule = usage.pluginModule,
                    requiredByModules = usage.requiredByModules
                )
        }
    }

    @Serializable
    private data class CatalogVersionDto(
        val value: String?,
        val visible: Boolean
    ) {
        fun toDomain(): CatalogVersion =
            CatalogVersion(
                value = value,
                visible = visible
            )

        companion object {
            fun from(
                version: CatalogVersion
            ): CatalogVersionDto =
                CatalogVersionDto(
                    value = version.value,
                    visible = version.visible
                )
        }
    }
}
