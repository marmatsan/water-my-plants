package com.marmatsan.figmaCatalogChecks.data.figma.common

import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode

internal fun Iterable<FigmaNode>.parentByChildId(
    treeNodes: Map<String, FigmaNode>
): Map<String, String> =
    filter { node -> node.type == "CONNECTOR" }
        .mapNotNull { connector -> connector.toChildParentPair(treeNodes) }
        .toMap()

internal fun Map<String, String>.childIdsByParentId(): Map<String, List<String>> =
    entries
        .groupBy(
            keySelector = { (_, parentId) -> parentId },
            valueTransform = { (childId, _) -> childId }
        )

private fun FigmaNode.toChildParentPair(treeNodes: Map<String, FigmaNode>): Pair<String, String>? {
    val startId = connectorStart?.endpointNodeId
    val endId = connectorEnd?.endpointNodeId

    if (startId !in treeNodes || endId !in treeNodes || startId == null || endId == null) {
        return null
    }

    return endId to startId
}
