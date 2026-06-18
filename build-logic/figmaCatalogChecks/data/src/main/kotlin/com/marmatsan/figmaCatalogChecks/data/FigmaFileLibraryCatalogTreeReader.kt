package com.marmatsan.figmaCatalogChecks.data

import com.marmatsan.figmaCatalogChecks.domain.CatalogVersion
import com.marmatsan.figmaCatalogChecks.domain.LibraryCatalogEntry
import com.marmatsan.figmaCatalogChecks.domain.LibraryCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.LibraryCatalogTree
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaFileLibraryCatalogTreeReader {
    fun readSection(
        section: FigmaNode,
        sectionNodeId: String
    ): LibraryCatalogTree {
        val rootSections = section.children.filter { node -> node.type == "SECTION" }

        if (rootSections.isEmpty()) {
            error("Figma library tree section '$sectionNodeId' contains no root sections")
        }

        return LibraryCatalogTree(
            roots = rootSections.flatMap { rootSection -> rootSection.readLibraryRoots() }
        )
    }

    private fun FigmaNode.readLibraryRoots(): List<LibraryCatalogNode> {
        val treeNodes = children
            .filter { node -> node.visible && node.isLibraryTreeNode() }
            .associateBy(FigmaNode::id)

        if (treeNodes.isEmpty()) {
            error("Figma library root section '$id' contains no library tree nodes")
        }

        val parentByChildId = children
            .filter { node -> node.type == "CONNECTOR" }
            .mapNotNull { connector -> connector.toChildParentPair(treeNodes) }
            .toMap()

        val childIdsByParentId = parentByChildId
            .entries
            .groupBy(
                keySelector = { (_, parentId) -> parentId },
                valueTransform = { (childId, _) -> childId }
            )

        return treeNodes
            .values
            .filter { node -> node.id !in parentByChildId }
            .sortedByPosition()
            .map { rootNode ->
                rootNode.toLibraryCatalogNode(
                    treeNodes = treeNodes,
                    childIdsByParentId = childIdsByParentId,
                    visitedNodeIds = emptySet()
                )
            }
    }

    private fun FigmaNode.toLibraryCatalogNode(
        treeNodes: Map<String, FigmaNode>,
        childIdsByParentId: Map<String, List<String>>,
        visitedNodeIds: Set<String>
    ): LibraryCatalogNode {
        check(id !in visitedNodeIds) {
            "Cycle found in Figma library tree at node '$id'"
        }

        val entries = readLibraryEntries()
        val nextVisitedNodeIds = visitedNodeIds + id

        return LibraryCatalogNode(
            group = libraryGroup(),
            entries = entries,
            artifactsVisible = showArtifacts(default = entries.isNotEmpty()),
            children = childIdsByParentId
                .getValueOrEmpty(id)
                .mapNotNull(treeNodes::get)
                .sortedByPosition()
                .map { child ->
                    child.toLibraryCatalogNode(
                        treeNodes = treeNodes,
                        childIdsByParentId = childIdsByParentId,
                        visitedNodeIds = nextVisitedNodeIds
                    )
                }
        )
    }

    private fun FigmaNode.readLibraryEntries(): List<LibraryCatalogEntry> {
        val artifactsContainer = children.firstOrNull { node -> node.name == ARTIFACTS_CONTAINER_NAME }

        return artifactsContainer
            ?.children
            .orEmpty()
            .filter { node -> node.visible && node.type == "INSTANCE" }
            .mapNotNull { node ->
                when {
                    node.isArtifactsBundle() -> node.toArtifactsBundleEntry()
                    node.isArtifact() -> node.toArtifactEntryOrNull()
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
        descendants()
            .filter { node -> node.visible && node.isModule() }
            .mapNotNull { module ->
                module.renderedTextValues().firstOrNull { text ->
                    text.startsWith(":") || text.contains("\\")
                }
            }
            .distinct()
            .sorted()
            .toList()

    private fun FigmaNode.isLibraryTreeNode(): Boolean =
        type == "INSTANCE" &&
            name.equals(".tree node", ignoreCase = true) &&
            textProperty(TYPE_PROPERTY) == LIBRARY_TYPE

    private fun FigmaNode.isArtifact(): Boolean =
        type == "INSTANCE" &&
            (componentId == ARTIFACT_COMPONENT_NODE_ID || name == ARTIFACT_COMPONENT_NAME)

    private fun FigmaNode.isArtifactsBundle(): Boolean =
        type == "INSTANCE" &&
            (componentId == ARTIFACTS_BUNDLE_COMPONENT_NODE_ID || name == ARTIFACTS_BUNDLE_COMPONENT_NAME)

    private fun FigmaNode.isModule(): Boolean =
        type == "INSTANCE" && name == MODULE_COMPONENT_NAME

    private fun FigmaNode.libraryGroup(): String =
        requiredTextProperty(LIBRARY_GROUP_PROPERTY)

    private fun FigmaNode.showArtifacts(default: Boolean): Boolean =
        booleanProperty(SHOW_ARTIFACTS_PROPERTY, default)

    private fun FigmaNode.artifactName(): String =
        requiredTextProperty(ARTIFACT_NAME_PROPERTY)

    private fun FigmaNode.requiredTextProperty(name: String): String =
        textProperty(name)
            ?.takeIf(String::isNotBlank)
            ?: error("Figma node '$id' is missing '$name'")

    private fun FigmaNode.textProperty(name: String): String? =
        componentProperties
            .findPropertyEntry(name)
            ?.let { (propertyKey, property) ->
                renderedTextPropertyValue(propertyKey)
                    ?: property.value.toPropertyValue().trim()
            }

    private fun FigmaNode.booleanProperty(
        name: String,
        default: Boolean
    ): Boolean =
        componentProperties
            .findPropertyEntry(name)
            ?.value
            ?.value
            ?.toBooleanPropertyValue()
            ?: default

    private fun FigmaNode.renderedTextValues(): Sequence<String> =
        descendants()
            .filter { node -> node.type == "TEXT" }
            .map { node -> node.characters.orEmpty().trim() }
            .filter(String::isNotBlank)

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

    private fun FigmaNode.descendants(): Sequence<FigmaNode> = sequence {
        if (visible) {
            yield(this@descendants)
            children.forEach { child ->
                yieldAll(child.descendants())
            }
        }
    }

    private fun Iterable<FigmaNode>.sortedByPosition(): List<FigmaNode> =
        sortedWith(
            compareBy<FigmaNode>(
                { node -> node.absoluteBoundingBox?.y ?: 0.0 },
                { node -> node.absoluteBoundingBox?.x ?: 0.0 },
                FigmaNode::id
            )
        )

    private fun Map<String, List<String>>.getValueOrEmpty(key: String): List<String> =
        this[key].orEmpty()

    private fun FigmaNode.toChildParentPair(treeNodes: Map<String, FigmaNode>): Pair<String, String>? {
        val startId = connectorStart?.endpointNodeId
        val endId = connectorEnd?.endpointNodeId

        if (startId !in treeNodes || endId !in treeNodes || startId == null || endId == null) {
            return null
        }

        return endId to startId
    }

    private fun FigmaNode.renderedTextPropertyValue(propertyKey: String): String? =
        descendants()
            .filter { node -> node.type == "TEXT" }
            .firstOrNull { node -> node.componentPropertyReferences["characters"] == propertyKey }
            ?.characters
            ?.trim()
            ?.takeIf(String::isNotBlank)

    private fun Map<String, FigmaComponentProperty>.findPropertyEntry(
        name: String
    ): Map.Entry<String, FigmaComponentProperty>? =
        entries
            .firstOrNull { (key, _) -> key == name || key.startsWith("$name#") }

    private fun JsonElement.toPropertyValue(): String =
        when (this) {
            is JsonPrimitive -> when {
                isString -> content
                booleanOrNull != null -> booleanOrNull.toString()
                else -> content
            }

            else -> toString()
        }

    private fun JsonElement.toBooleanPropertyValue(): Boolean? =
        when (this) {
            is JsonPrimitive -> booleanOrNull ?: content.toBooleanStrictOrNull()
            else -> null
        }

    private companion object {
        const val TYPE_PROPERTY = "Type"
        const val LIBRARY_TYPE = "Library"
        const val LIBRARY_GROUP_PROPERTY = "Library group"
        const val SHOW_ARTIFACTS_PROPERTY = "Show artifacts"
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
