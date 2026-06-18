package com.marmatsan.figmaCatalogChecks.data

import me.tatarka.inject.annotations.Inject

@Inject
class FigmaModuleComponentReader {
    fun readComponent(
        component: FigmaNode,
        componentNodeId: String
    ): Set<String> {
        val modules = component.children
            .filter { node -> node.visible && node.type == COMPONENT_TYPE }
            .mapNotNull { node -> node.moduleName() }
            .toSortedSet()

        check(modules.isNotEmpty()) {
            "Figma .module component '$componentNodeId' contains no module variants"
        }

        return modules
    }

    private fun FigmaNode.moduleName(): String? =
        name
            .split(",")
            .map(String::trim)
            .firstOrNull { part -> part.startsWith("$MODULE_NAME_VARIANT=") }
            ?.substringAfter("=")
            ?.trim()
            ?.takeIf(String::isNotBlank)

    private companion object {
        const val COMPONENT_TYPE = "COMPONENT"
        const val MODULE_NAME_VARIANT = "name"
    }
}
