package com.marmatsan.figmaDesignSync.plugin.checker.sync

import com.marmatsan.figmaDesignSync.data.figma.client.FigmaFileContentClient
import com.marmatsan.figmaDesignSync.data.figma.common.FigmaNodeUrl
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerationRequest
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerator
import me.tatarka.inject.annotations.Inject
import org.gradle.api.GradleException

@Inject
internal class FigmaDevelopSyncChecker(
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaDesignModelGenerator: FigmaDesignModelGenerator
) {
    fun check(request: FigmaDevelopSyncCheckRequest): FigmaDevelopSyncCheckResult {
        val expected = figmaDesignModelGenerator.generate(
            FigmaDesignModelGenerationRequest(
                branch = request.branch,
                gitSha = request.gitSha,
                generatedAt = request.generatedAt,
                versionsFile = request.versionsFile,
                rootSettingsFile = request.rootSettingsFile,
                buildLogicSettingsFile = request.buildLogicSettingsFile,
                projectRootDirectory = request.projectRootDirectory,
                buildLogicRootDirectory = request.buildLogicRootDirectory
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

        return FigmaDevelopSyncCheckResult(
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
