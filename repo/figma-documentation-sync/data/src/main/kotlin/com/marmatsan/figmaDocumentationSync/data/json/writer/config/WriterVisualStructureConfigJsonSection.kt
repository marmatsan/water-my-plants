package com.marmatsan.figmaDocumentationSync.data.json.writer.config

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put

/** Writes parent-section, tree-node, connector, and header structure fields. */
internal object WriterVisualStructureConfigJsonSection : FigmaWriterProjectConfigJsonSection {
    override fun write(
        context: FigmaWriterProjectConfigJsonContext,
        json: JsonObjectBuilder
    ) {
        val config = context.config
        json.put(
            "PARENT_SECTION_SIBLING_GAP",
            config.parentSectionSiblingGap
        )
        json.put(
            "PARENT_SECTION_NODE_IDS",
            config.parentSectionNodeIds.toJsonArray()
        )
        json.put(
            "PARENT_SECTION_CORNER_RADIUS",
            config.parentSectionCornerRadius
        )
        json.put(
            "SECTION_SIBLING_GAP",
            config.sectionSiblingGap
        )
        json.put(
            "TREE_NODE_COMPONENT_IDS",
            config.treeNodeComponentIds.toJsonObject()
        )
        json.put(
            "CONNECTOR_TEMPLATE_NAME",
            config.connectorTemplateName
        )
        json.put(
            "HEADER_INSTANCE_NAME",
            config.headerInstanceName
        )
        json.put(
            "HEADER_LINK_PROPERTY_NAME",
            config.headerLinkPropertyName
        )
        json.put(
            "HEADER_DEFINITION_PROPERTY_NAME",
            config.headerDefinitionPropertyName
        )
    }
}
