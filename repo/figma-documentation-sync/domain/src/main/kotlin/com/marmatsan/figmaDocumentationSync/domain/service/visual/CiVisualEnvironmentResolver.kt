package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan

/** Resolves versioned CI node identities to the visual environment icon contract. */
internal class CiVisualEnvironmentResolver {
    fun external(
        id: String,
    ): CiVisualPlan.Environment =
        externalEnvironments[id]
            ?: throw IllegalArgumentException("External CI node '$id' has no .ci icon environment mapping.")

    fun windowsRuntime(
        id: String,
    ): CiVisualPlan.Environment =
        windowsRuntimeEnvironments[id]
            ?: throw IllegalArgumentException("Windows CI runtime service '$id' has no .ci icon environment mapping.")

    private companion object {
        val externalEnvironments =
            mapOf(
                "operator" to CiVisualPlan.Environment.OPERATOR,
                "browser" to CiVisualPlan.Environment.BROWSER,
                "teamcity-cli" to CiVisualPlan.Environment.TERMINAL,
                "github-repository" to CiVisualPlan.Environment.GITHUB,
                "github-app" to CiVisualPlan.Environment.GITHUB,
                "cloudflare-access" to CiVisualPlan.Environment.CLOUDFLARE,
                "cloudflare-tunnel" to CiVisualPlan.Environment.CLOUDFLARE,
                "teamcity-server" to CiVisualPlan.Environment.TEAMCITY,
                "build-agent" to CiVisualPlan.Environment.TEAMCITY,
                "codex-mcp-client" to CiVisualPlan.Environment.CODEX,
                "figma-api" to CiVisualPlan.Environment.FIGMA,
                "figma-design-document" to CiVisualPlan.Environment.FIGMA,
            )
        val windowsRuntimeEnvironments =
            mapOf(
                "teamcity-server" to CiVisualPlan.Environment.TEAMCITY,
                "build-agent" to CiVisualPlan.Environment.TEAMCITY,
                "cloudflare-tunnel" to CiVisualPlan.Environment.CLOUDFLARE,
            )
    }
}
