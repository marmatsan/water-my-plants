package com.marmatsan.figmaDesignSync.data.datasource.impact

import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaChangeImpactPolicy
import com.marmatsan.figmaDesignSync.domain.model.impact.FigmaVisualTargetRule
import com.marmatsan.figmaDesignSync.domain.port.impact.FigmaChangeImpactPolicyPort
import java.io.File
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

/** JSON adapter for the repository-owned Figma change-impact policy. */
@Inject
class FigmaChangeImpactPolicyDataSource : FigmaChangeImpactPolicyPort {
    override fun read(sourcePath: String): FigmaChangeImpactPolicy {
        val source = File(sourcePath)
        require(source.isFile) { "Figma change-impact policy was not found: ${source.path}" }
        val dto = json.decodeFromString<FigmaChangeImpactPolicyDto>(source.readText())
        require(dto.schemaVersion == SUPPORTED_SCHEMA_VERSION) {
            "Unsupported Figma change-impact policy schema ${dto.schemaVersion}; " +
                "expected $SUPPORTED_SCHEMA_VERSION"
        }

        return FigmaChangeImpactPolicy(
            documentationOnlyPaths = dto.documentationOnlyPaths,
            transportOnlyPaths = dto.figmaTransportOnlyPaths,
            modelNeutralPaths = dto.figmaModelNeutralPaths,
            modelContentPaths = dto.figmaModelContentPaths,
            visualWriterPaths = dto.figmaVisualWriterPaths,
            visualTargetRules = dto.figmaVisualTargetRules.map { rule ->
                FigmaVisualTargetRule(paths = rule.paths, targets = rule.targets)
            }
        )
    }

    private companion object {
        const val SUPPORTED_SCHEMA_VERSION = 1

        val json = Json {
            ignoreUnknownKeys = true
        }
    }
}
