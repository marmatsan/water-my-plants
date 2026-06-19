package com.marmatsan.figmaCatalogChecks.data.figma.modules

import com.marmatsan.figmaCatalogChecks.data.figma.common.renderedTextValues
import com.marmatsan.figmaCatalogChecks.data.figma.common.visibleDescendants
import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode
import com.marmatsan.figmaCatalogChecks.domain.model.modules.ModuleDependency
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaModuleDependenciesReader {
    fun readSection(section: FigmaNode): Set<ModuleDependency> {
        val moduleNamesByNodeId = section.moduleNamesByNodeId()
        val connectors = section.visibleDescendants()
            .filter { node -> node.type == CONNECTOR_TYPE }

        check(moduleNamesByNodeId.isNotEmpty()) {
            "Figma module dependency section '${section.id}' contains no .module instances"
        }

        return connectors
            .mapNotNull { connector ->
                val dependentModule = moduleNamesByNodeId[connector.connectorStart?.endpointNodeId]
                val dependencyModule = moduleNamesByNodeId[connector.connectorEnd?.endpointNodeId]

                if (dependentModule == null || dependencyModule == null) {
                    null
                } else {
                    ModuleDependency(
                        dependentModule = dependentModule,
                        dependencyModule = dependencyModule
                    )
                }
            }
            .toSortedSet()
    }

    private fun FigmaNode.moduleNamesByNodeId(): Map<String, String> =
        visibleDescendants()
            .filter { node -> node.isModule() }
            .mapNotNull { node ->
                node.renderedTextValues()
                    .firstOrNull()
                    ?.let { moduleName -> node.id to moduleName }
            }
            .toMap()

    private fun FigmaNode.isModule(): Boolean =
        type == INSTANCE_TYPE && name == MODULE_COMPONENT_NAME

    private companion object {
        const val CONNECTOR_TYPE = "CONNECTOR"
        const val INSTANCE_TYPE = "INSTANCE"
        const val MODULE_COMPONENT_NAME = ".module"
    }
}
