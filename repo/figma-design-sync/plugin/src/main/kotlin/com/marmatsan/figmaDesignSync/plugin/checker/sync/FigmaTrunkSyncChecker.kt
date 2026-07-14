package com.marmatsan.figmaDesignSync.plugin.checker.sync

import com.marmatsan.figmaDesignSync.data.figma.client.FigmaFileContentClient
import com.marmatsan.figmaDesignSync.data.figma.common.FigmaNodeUrl
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerationRequest
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerator
import me.tatarka.inject.annotations.Inject
import org.gradle.api.GradleException

/**
 * Compares the locally generated design model hash with the hash stored in the
 * Figma document.
 *
 * This checker is the CI guard for the Figma documentation workflow: if the
 * repository can generate a different `design-model.json` than the one Figma
 * records in shared plugin data, the build fails and the Figma MCP sync step
 * must be run again.
 */
@Inject
internal class FigmaTrunkSyncChecker(
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaDesignModelGenerator: FigmaDesignModelGenerator
) {
    /**
     * Generates the expected model and compares it with Figma metadata.
     *
     * @throws GradleException when metadata is missing or the model hash is out
     * of sync.
     */
    fun check(request: FigmaTrunkSyncCheckRequest): FigmaTrunkSyncCheckResult {
        val expected = figmaDesignModelGenerator.generate(
            FigmaDesignModelGenerationRequest(
                branch = request.branch,
                gitSha = request.gitSha,
                generatedAt = request.generatedAt,
                versionsFile = request.versionsFile,
                rootSettingsFile = request.rootSettingsFile,
                ciExternalTopologyFile = request.ciExternalTopologyFile,
                teamCityGeneratedConfigurationDirectory = request.teamCityGeneratedConfigurationDirectory,
                projectRootDirectory = request.projectRootDirectory,
                includedBuilds = request.includedBuilds
            )
        )
        val metadataNode = FigmaNodeUrl.parse(request.metadataNodeUrl)
        val figmaMetadata = figmaFileContentClient
            .getNodeContent(
                fileKey = metadataNode.fileKey,
                token = request.token,
                nodeId = metadataNode.nodeId,
                pluginData = "shared"
            )
            .sharedPluginData[NAMESPACE]
            ?: throw GradleException("Figma sync metadata namespace '$NAMESPACE' was not found.")

        val figmaModelHash = figmaMetadata[MODEL_HASH_KEY]
            ?: throw GradleException("Figma sync metadata key '$MODEL_HASH_KEY' was not found.")
        val figmaGitSha = figmaMetadata[GIT_SHA_KEY]
            ?: throw GradleException("Figma sync metadata key '$GIT_SHA_KEY' was not found.")

        if (figmaModelHash != expected.modelHash) {
            throw GradleException(
                "Figma is out of sync with ${request.branch}. " +
                    "Expected modelHash ${expected.modelHash}, found $figmaModelHash. " +
                    "Run the Figma MCP sync step with the latest design-model.json."
            )
        }

        return FigmaTrunkSyncCheckResult(
            modelHash = figmaModelHash,
            gitSha = figmaGitSha
        )
    }

    private companion object {
        const val NAMESPACE = "water_my_plants_sync"
        const val MODEL_HASH_KEY = "modelHash"
        const val GIT_SHA_KEY = "gitSha"
    }
}
