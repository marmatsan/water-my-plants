package com.marmatsan.figmaDocumentationSync.data.datasource.impact

import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaChangeImpactPolicy
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaVisualTargetRule
import com.marmatsan.figmaDocumentationSync.domain.port.impact.FigmaChangeImpactPolicyPort
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import java.io.File

/** JSON adapter for the repository-owned Figma change-impact policy. */
@Inject
class FigmaChangeImpactPolicyDataSource : FigmaChangeImpactPolicyPort {
    /** Reads and validates the versioned JSON impact policy at [sourcePath]. */
    override fun read(
        sourcePath: String,
    ): FigmaChangeImpactPolicy {
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
            visualTargetRules =
                dto.figmaVisualTargetRules.map { rule ->
                    FigmaVisualTargetRule(
                        paths = rule.paths,
                        targets = rule.targets,
                    )
                },
        )
    }

    private companion object {
        const val SUPPORTED_SCHEMA_VERSION = 1

        val json =
            Json {
                ignoreUnknownKeys = true
            }
    }
}
