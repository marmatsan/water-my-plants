package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan

/** Plans the versioned Windows services that host the local CI runtime. */
internal class WindowsRuntimeCiVisualSectionPlanner(
    private val environments: CiVisualEnvironmentResolver,
) : CiVisualSectionPlanner {
    /** Builds the Windows CI runtime services section. */
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
