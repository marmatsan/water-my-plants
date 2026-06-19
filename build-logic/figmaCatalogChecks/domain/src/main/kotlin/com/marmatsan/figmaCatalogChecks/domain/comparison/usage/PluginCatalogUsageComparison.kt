package com.marmatsan.figmaCatalogChecks.domain.comparison.usage

import com.marmatsan.figmaCatalogChecks.domain.comparison.catalog.buildPath
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.usage.ProjectCatalogUsage
import me.tatarka.inject.annotations.Inject

@Inject
class PluginCatalogUsageComparison {
    fun compare(
        figmaTree: PluginCatalogTree,
        projectUsage: ProjectCatalogUsage
    ): CatalogUsageComparisonResult {
        val expectedModulesByPluginId = figmaTree.expectedModulesByPluginId()
        val actualModulesByPluginId = projectUsage.actualPluginModulesById(expectedModulesByPluginId.keys)

        return compareUsages(
            subject = "plugin",
            expectedModulesByKey = expectedModulesByPluginId.mapKeys { (pluginId, _) -> "plugin $pluginId" },
            actualModulesByKey = actualModulesByPluginId.mapKeys { (pluginId, _) -> "plugin $pluginId" }
        )
    }

    private fun PluginCatalogTree.expectedModulesByPluginId(): Map<String, List<String>> =
        roots
            .flatMap { root -> root.expectedModulesByPluginId(parentPath = null) }
            .toMap()

    private fun PluginCatalogNode.expectedModulesByPluginId(
        parentPath: String?
    ): List<Pair<String, List<String>>> {
        val path = buildPath(parentPath, id)
        val nodeUsage = if (version != null || appliedToModules.isNotEmpty()) {
            listOf(path to appliedToModules)
        } else {
            emptyList()
        }
        val childUsages = children.flatMap { child -> child.expectedModulesByPluginId(path) }

        return nodeUsage + childUsages
    }

    private fun ProjectCatalogUsage.actualPluginModulesById(
        expectedPluginIds: Set<String>
    ): Map<String, List<String>> =
        expectedPluginIds.associateWith { expectedPluginId ->
            pluginIdsByModule
                .filterValues { pluginIds -> expectedPluginId in pluginIds }
                .keys
                .sorted()
        }
}
