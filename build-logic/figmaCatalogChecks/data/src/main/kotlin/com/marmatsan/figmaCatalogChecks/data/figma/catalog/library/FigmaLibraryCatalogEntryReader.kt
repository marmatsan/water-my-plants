package com.marmatsan.figmaCatalogChecks.data.figma.catalog.library

import com.marmatsan.figmaCatalogChecks.data.figma.common.booleanProperty
import com.marmatsan.figmaCatalogChecks.data.figma.common.renderedTextValues
import com.marmatsan.figmaCatalogChecks.data.figma.common.requiredTextProperty
import com.marmatsan.figmaCatalogChecks.data.figma.common.visibleDescendants
import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogEntry
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaLibraryCatalogEntryReader {
    fun readEntries(node: FigmaNode): List<LibraryCatalogEntry> {
        val artifactsContainer = node.children.firstOrNull { child -> child.name == ARTIFACTS_CONTAINER_NAME }

        return artifactsContainer
            ?.children
            .orEmpty()
            .filter { child -> child.visible && child.type == "INSTANCE" }
            .mapNotNull { child ->
                when {
                    child.isArtifactsBundle() -> child.toArtifactsBundleEntry()
                    child.isArtifact() -> child.toArtifactEntryOrNull()
                    else -> null
                }
            }
    }

    private fun FigmaNode.toArtifactEntryOrNull(): LibraryCatalogEntry.Artifact? {
        val artifactName = artifactName()

        if (artifactName == ARTIFACT_NAME_PLACEHOLDER) {
            return null
        }

        return LibraryCatalogEntry.Artifact(
            artifact = artifactName,
            version = catalogVersion(
                visiblePropertyName = ARTIFACT_SHOW_VERSION_PROPERTY,
                valuePropertyName = ARTIFACT_VERSION_PROPERTY
            ),
            requiredByModules = readRequiredByModules()
        )
    }

    private fun FigmaNode.toArtifactsBundleEntry(): LibraryCatalogEntry.ArtifactsBundle =
        LibraryCatalogEntry.ArtifactsBundle(
            alias = requiredTextProperty(BUNDLE_ALIAS_PROPERTY),
            artifacts = artifactChildren().map { node -> node.artifactName() },
            version = catalogVersion(
                visiblePropertyName = BUNDLE_WITH_VERSION_PROPERTY,
                valuePropertyName = BUNDLE_VERSION_PROPERTY
            ),
            requiredByModules = readRequiredByModules()
        )

    private fun FigmaNode.artifactChildren(): List<FigmaNode> =
        children
            .firstOrNull { node -> node.name == ARTIFACTS_CONTAINER_NAME }
            ?.children
            .orEmpty()
            .filter { node -> node.visible && node.isArtifact() }
            .filter { node -> node.artifactName() != ARTIFACT_NAME_PLACEHOLDER }

    private fun FigmaNode.catalogVersion(
        visiblePropertyName: String,
        valuePropertyName: String
    ): CatalogVersion {
        val visible = booleanProperty(visiblePropertyName, default = true)

        if (!visible) {
            return CatalogVersion(null)
        }

        return CatalogVersion(
            renderedVersionText()
                ?: requiredTextProperty(valuePropertyName)
        )
    }

    private fun FigmaNode.readRequiredByModules(): List<String> =
        visibleDescendants()
            .filter { node -> node.visible && node.isModule() }
            .mapNotNull { module ->
                module.renderedTextValues().firstOrNull { text ->
                    text.startsWith(":") || text.contains("\\")
                }
            }
            .distinct()
            .sorted()
            .toList()

    private fun FigmaNode.isArtifact(): Boolean =
        type == "INSTANCE" &&
            (componentId == ARTIFACT_COMPONENT_NODE_ID || name == ARTIFACT_COMPONENT_NAME)

    private fun FigmaNode.isArtifactsBundle(): Boolean =
        type == "INSTANCE" &&
            (componentId == ARTIFACTS_BUNDLE_COMPONENT_NODE_ID || name == ARTIFACTS_BUNDLE_COMPONENT_NAME)

    private fun FigmaNode.isModule(): Boolean =
        type == "INSTANCE" && name == MODULE_COMPONENT_NAME

    private fun FigmaNode.artifactName(): String =
        requiredTextProperty(ARTIFACT_NAME_PROPERTY)

    private fun FigmaNode.renderedVersionText(): String? {
        val contentTexts = children
            .firstOrNull { node -> node.name == CONTENT_FRAME_NAME }
            ?.renderedTextValues()
            .orEmpty()
            .toList()

        val separatorIndex = contentTexts.indexOf(":")

        return if (separatorIndex >= 0) {
            contentTexts
                .drop(separatorIndex + 1)
                .firstOrNull { text -> text != ARTIFACT_VERSION_PLACEHOLDER }
        } else {
            contentTexts.firstOrNull { text ->
                text != ARTIFACT_NAME_PLACEHOLDER &&
                    text != ARTIFACT_VERSION_PLACEHOLDER &&
                    text != "artifact"
            }
        }
    }

    private companion object {
        const val ARTIFACT_NAME_PROPERTY = "Artifact name"
        const val ARTIFACT_VERSION_PROPERTY = "Artifact version"
        const val ARTIFACT_SHOW_VERSION_PROPERTY = "Show version"
        const val BUNDLE_ALIAS_PROPERTY = "Alias"
        const val BUNDLE_VERSION_PROPERTY = "Version"
        const val BUNDLE_WITH_VERSION_PROPERTY = "With version"
        const val ARTIFACTS_CONTAINER_NAME = "artifacts"
        const val CONTENT_FRAME_NAME = "content"
        const val ARTIFACT_COMPONENT_NAME = ".artifact"
        const val ARTIFACT_NAME_PLACEHOLDER = "artifact name"
        const val ARTIFACT_VERSION_PLACEHOLDER = "artifact version"
        const val ARTIFACTS_BUNDLE_COMPONENT_NAME = ".artifacts bundle"
        const val MODULE_COMPONENT_NAME = ".module"
        const val ARTIFACT_COMPONENT_NODE_ID = "63069:700"
        const val ARTIFACTS_BUNDLE_COMPONENT_NODE_ID = "63069:714"
    }
}
