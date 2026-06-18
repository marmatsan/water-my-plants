package com.marmatsan.figmaCatalogChecks.data.figma.common

import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode

internal fun FigmaNode.allDescendants(): Sequence<FigmaNode> = sequence {
    yield(this@allDescendants)
    children.forEach { child ->
        yieldAll(child.allDescendants())
    }
}

internal fun FigmaNode.visibleDescendants(): Sequence<FigmaNode> = sequence {
    if (visible) {
        yield(this@visibleDescendants)
        children.forEach { child ->
            yieldAll(child.visibleDescendants())
        }
    }
}

internal fun Map<String, List<String>>.getValueOrEmpty(key: String): List<String> =
    this[key].orEmpty()
