package com.marmatsan.figmaDocumentationSync.data.json.writer.config

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put

/** Writes header, version, artifact, usage, catalog, and aggregate target fields. */
internal object WriterCatalogConfigJsonSection : FigmaWriterProjectConfigJsonSection {
    override fun write(
        context: FigmaWriterProjectConfigJsonContext,
        json: JsonObjectBuilder,
    ) {
        val config = context.config
        json.put(
            "HEADER_SECTION_TARGETS",
            config.headerSectionTargets.toHeaderTargetsJson(),
        )
        json.put(
            "VERSION_SECTION_TARGETS",
            config.versionSectionTargets.toVersionTargetsJson(),
        )
        json.put(
            "TREE_NODE_PROPS",
            config.treeNodeProps.toJsonObject(),
        )
        json.put(
            "ARTIFACT_PROPS",
            config.artifactProps.toJsonObject(),
        )
        json.put(
            "ARTIFACTS_BUNDLE_PROPS",
            config.artifactsBundleProps.toJsonObject(),
        )
        json.put(
            "ARTIFACT_INSTANCE_NAME",
            config.artifactInstanceName,
        )
        json.put(
            "ARTIFACTS_BUNDLE_INSTANCE_NAME",
            config.artifactsBundleInstanceName,
        )
        json.put(
            "USAGE_CHIP_COMPONENT_SET_ID",
            config.usageChipComponentSetId,
        )
        json.put(
            "USAGE_CHIP_INSTANCE_NAME",
            config.usageChipInstanceName,
        )
        json.put(
            "TOOL_ARTIFACT_USAGE_INSTANCE_NAME",
            config.toolArtifactUsageInstanceName,
        )
        json.put(
            "TOOL_ARTIFACT_USAGE_PROPS",
            config.toolArtifactUsageProps.toJsonObject(),
        )
        json.put(
            "USAGE_CHIP_PROPS",
            config.usageChipProps.toJsonObject(),
        )
        json.put(
            "USAGE_CHIP_KINDS",
            config.usageChipKinds.toJsonObject(),
        )
        json.put(
            "CATALOG_TREE_TARGETS",
            config.catalogTreeTargets.toCatalogTargetsJson(),
        )
        json.put(
            "CI_VISUAL_TARGET_NAMES",
            config.ciVisualTargetNames.toJsonArray(),
        )
        json.put(
            "CATALOG_TARGET_NAMES",
            context.catalogTargetNames.toJsonArray(),
        )
        json.put(
            "WRITER_TARGET_NAMES",
            (
                listOf(
                    "preflight",
                    "headers",
                    "versions",
                ) +
                    context.catalogTargetNames + config.ciVisualTargetNames + "metadata"
            ).toJsonArray(),
        )
        json.put(
            "DEFAULT_FIXTURE_TARGETS",
            config.defaultFixtureTargets.toJsonObject(),
        )
    }
}
