package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.adapter

import com.marmatsan.figmaDocumentationSync.data.mcp.McpRunnerExecutor
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model.CanonicalFigmaRunnerInspection
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.port.CanonicalFigmaRunnerInspector
import java.nio.file.Path

internal class DefaultCanonicalFigmaRunnerInspector(
    private val executor: McpRunnerExecutor = McpRunnerExecutor(),
) : CanonicalFigmaRunnerInspector {
    override fun inspect(
        manifestPath: Path,
        planPath: Path,
    ): CanonicalFigmaRunnerInspection {
        val inspection =
            executor.inspect(
                McpRunnerExecutor.Request(
                    manifestPath = manifestPath.toString(),
                    planPath = planPath.toString(),
                ),
            )
        return CanonicalFigmaRunnerInspection(
            manifestHash = inspection.manifestHash,
            statePath = inspection.statePath,
            reuseStaging = inspection.reuseStaging,
            decision = inspection.decision,
            executionFiles = inspection.executionFiles,
        )
    }
}
