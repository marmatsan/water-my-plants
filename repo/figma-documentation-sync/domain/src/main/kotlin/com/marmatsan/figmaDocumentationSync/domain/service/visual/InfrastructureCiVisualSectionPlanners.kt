package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan

/** Plans external CI systems, trust boundaries, and automation paths. */
internal class InfrastructureCiVisualSectionPlanner(
    private val environments: CiVisualEnvironmentResolver,
) : CiVisualSectionPlanner {
    override fun create(
        context: CiVisualPlanningContext,
    ): CiVisualPlan.Section {
        val config = context.config
        val topology = context.externalTopology
        val placement = infrastructurePlacement()
        val nodes =
            topology.nodes.mapIndexed { index, node ->
                val position =
                    placement[node.id] ?: Position(
                        row = index,
                        column = 0,
                    )
                externalNode(
                    id = "external-${node.id}",
                    node = node,
                    row = position.row,
                    column = position.column,
                    config = config,
                    environments = environments,
                )
            }
        val nodeIds = topology.nodes.associate { node -> node.id to "external-${node.id}" }
        val connections =
            topology.connections.map { edge ->
                visualConnection(
                    id = "external-${edge.id}",
                    source = nodeIds[edge.sourceNodeId],
                    target = nodeIds[edge.targetNodeId],
                    label = edge.label,
                    kind = externalConnectionKind(edge.id),
                )
            }
        return visualSection(
            target = "ci.infrastructureAndAccess",
            name = "Infrastructure and Access",
            description = "External systems, trust boundaries, authentication paths, and automation modes.",
            sources =
                listOf(
                    config.topologySource,
                    "docs/ci/external-topology-validation.md",
                ),
            orientation = CiVisualPlan.Orientation.GRID,
            nodes = nodes,
            connections = connections,
            config = config,
        )
    }

    private fun infrastructurePlacement(): Map<String, Position> =
        mapOf(
            "browser" to
                Position(
                    row = 0,
                    column = 0,
                ),
            "teamcity-cli" to
                Position(
                    row = 0,
                    column = 1,
                ),
            "github-app" to
                Position(
                    row = 0,
                    column = 2,
                ),
            "operator" to
                Position(
                    row = 0,
                    column = 3,
                ),
            "cloudflare-access" to
                Position(
                    row = 1,
                    column = 1,
                ),
            "cloudflare-tunnel" to
                Position(
                    row = 2,
                    column = 1,
                ),
            "teamcity-server" to
                Position(
                    row = 3,
                    column = 1,
                ),
            "github-repository" to
                Position(
                    row = 3,
                    column = 2,
                ),
            "build-agent" to
                Position(
                    row = 4,
                    column = 1,
                ),
            "codex-mcp-client" to
                Position(
                    row = 4,
                    column = 3,
                ),
            "figma-api" to
                Position(
                    row = 5,
                    column = 2,
                ),
            "figma-design-document" to
                Position(
                    row = 6,
                    column = 2,
                ),
        )

    private data class Position(
        val row: Int,
        val column: Int,
    )
}

/** Plans the versioned Windows services that host the local CI runtime. */
internal class WindowsRuntimeCiVisualSectionPlanner(
    private val environments: CiVisualEnvironmentResolver,
) : CiVisualSectionPlanner {
    override fun create(
        context: CiVisualPlanningContext,
    ): CiVisualPlan.Section {
        val config = context.config
        val runtime = context.windowsRuntime
        val nodes =
            runtime.services.mapIndexed { index, service ->
                visualNode(
                    id = "windows-runtime-${service.id}",
                    type = CiVisualPlan.Type.SYSTEM,
                    environment = environments.windowsRuntime(service.id),
                    name = service.name,
                    description = service.description,
                    source = config.windowsRuntimeSource,
                    row = 0,
                    column = index,
                    config = config,
                ).copy(
                    runtime =
                        CiVisualPlan.Runtime(
                            platform = runtime.platform,
                            service = service.service,
                            startup = service.startup,
                            identity = service.identity,
                        ),
                )
            }
        return visualSection(
            target = "ci.windowsRuntime",
            name = "Windows Service Runtime",
            description = "Versioned inventory of the Windows services that host the local CI runtime.",
            sources =
                listOf(
                    config.windowsRuntimeSource,
                    config.windowsRuntimeRunbookSource,
                ),
            orientation = CiVisualPlan.Orientation.GRID,
            nodes = nodes,
            connections = emptyList(),
            config = config,
        )
    }
}
