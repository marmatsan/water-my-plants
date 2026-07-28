package com.marmatsan.figmaDocumentationSync.data.json.writer.config

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put

/** Writes repository sources, model identities, and portable filesystem paths. */
internal object WriterRepositoryConfigJsonSection : FigmaWriterProjectConfigJsonSection {
    override fun write(
        context: FigmaWriterProjectConfigJsonContext,
        json: JsonObjectBuilder,
    ) {
        val config = context.config
        json.put(
            "GITHUB_MAIN_BLOB_URL",
            config.githubMainBlobUrl,
        )
        json.put(
            "GITHUB_MAIN_TREE_URL",
            config.githubMainTreeUrl,
        )
        json.put(
            "CI_CONFIGURATION_MODEL_NAME",
            config.ciConfigurationModelName,
        )
        json.put(
            "CI_PIPELINE_NAME",
            config.ciPipelineName,
        )
        json.put(
            "FIGMA_PIPELINE_NAME",
            config.figmaPipelineName,
        )
        json.put(
            "TEAMCITY_SOURCE",
            config.teamCitySource,
        )
        json.put(
            "TOPOLOGY_SOURCE",
            config.topologySource,
        )
        json.put(
            "WINDOWS_RUNTIME_SOURCE",
            config.windowsRuntimeSource,
        )
        json.put(
            "WINDOWS_RUNTIME_RUNBOOK_SOURCE",
            config.windowsRuntimeRunbookSource,
        )
        json.put(
            "VISUAL_CONTRACT_SOURCE",
            config.visualContractSource,
        )
        json.put(
            "BRANCH_PROTECTION_SOURCE",
            config.branchProtectionSource,
        )
        json.put(
            "CANONICAL_SYNC_SOURCE",
            config.canonicalSyncSource,
        )
        json.put(
            "CANONICAL_DESIGN_MODEL_PATH",
            config.canonicalDesignModelPath,
        )
        json.put(
            "REPOSITORY_ROOT_RELATIVE_TO_TOOLS",
            config.repositoryRootRelativeToTools,
        )
        json.put(
            "CHANGE_IMPACT_POLICY_RELATIVE_TO_REPOSITORY",
            config.changeImpactPolicyRelativeToRepository,
        )
    }
}
