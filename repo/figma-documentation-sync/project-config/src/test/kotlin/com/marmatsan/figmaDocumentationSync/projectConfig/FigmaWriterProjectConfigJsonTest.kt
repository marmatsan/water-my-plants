package com.marmatsan.figmaDocumentationSync.projectConfig

import com.marmatsan.figmaDocumentationSync.data.json.writer.FigmaWriterProjectConfigJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class FigmaWriterProjectConfigJsonTest :
    FunSpec(
        {
            test("projects the typed Water My Plants writer config to the versioned JSON schema") {
                val root =
                    Json
                        .parseToJsonElement(
                            FigmaWriterProjectConfigJson.encode(WaterMyPlantsFigmaWriterProjectConfig.value),
                        ).jsonObject

                root.keys shouldBe expectedKeys
                root.getValue("schemaVersion").jsonPrimitive.content shouldBe "3"
                root.getValue("METADATA_NAMESPACE").jsonPrimitive.content shouldBe "water_my_plants_sync"
                root.getValue("CANONICAL_STAGING_NAMESPACE").jsonPrimitive.content shouldBe
                    "water_my_plants_sync_staging"
                root.getValue("PROJECT_VERSION_COMPONENT_ID").jsonPrimitive.content shouldBe "63075:591"
                root.getValue("CI_CONFIGURATION_MODEL_NAME").jsonPrimitive.content shouldBe "teamCity"
                root.getValue("CI_CONNECTOR_TEMPLATE_NAME").jsonPrimitive.content shouldBe
                    "simple-line_arrow"
                root.getValue("CI_CONNECTOR_TEMPLATE_NODE_ID").jsonPrimitive.content shouldBe
                    "64800:262"
                root.getValue("CI_NODE_PHASE_CONTAINER_NAME").jsonPrimitive.content shouldBe "execution plan"
                root.getValue("CI_NODE_OUTCOME_CONTAINER_NAME").jsonPrimitive.content shouldBe "outcome"
                root.getValue("CI_PHASE_STEP_CONTAINER_NAME").jsonPrimitive.content shouldBe "steps"
                root
                    .getValue("CI_NODE_PROPS")
                    .jsonObject
                    .also { properties ->
                        properties.keys shouldBe
                            setOf(
                                "name",
                                "description",
                                "executionPlanHeading",
                                "source",
                                "runtimePlatform",
                                "runtimeService",
                                "runtimeStartup",
                                "runtimeIdentity",
                                "showExecutionPlan",
                                "showOutcome",
                                "showSource",
                                "showRuntime",
                                "showOptionalDetails",
                            )
                        properties
                            .getValue("executionPlanHeading")
                            .jsonPrimitive.content shouldBe "execution plan heading"
                        properties
                            .getValue("showExecutionPlan")
                            .jsonPrimitive.content shouldBe "show execution plan"
                        properties
                            .getValue("showOutcome")
                            .jsonPrimitive.content shouldBe "show outcome"
                    }
                root
                    .getValue("CI_PHASE_PROPS")
                    .jsonObject
                    .also { properties ->
                        properties.keys shouldBe
                            setOf(
                                "order",
                                "title",
                                "technicalId",
                                "description",
                                "showTechnicalId",
                                "showDescription",
                                "showSteps",
                            )
                        properties
                            .getValue("showSteps")
                            .jsonPrimitive.content shouldBe "show steps"
                    }
                root
                    .getValue("CI_STEP_PROPS")
                    .jsonObject
                    .also { properties ->
                        properties.keys shouldBe
                            setOf(
                                "order",
                                "title",
                                "technicalId",
                                "tasks",
                                "description",
                                "condition",
                                "showTechnicalId",
                                "showDescription",
                                "showCondition",
                                "role",
                            )
                        properties
                            .getValue("tasks")
                            .jsonPrimitive.content shouldBe "tasks"
                    }
                root.getValue("CI_STEP_SLOT_NAME_PREFIX").jsonPrimitive.content shouldBe "step"
                root.getValue("CI_STEP_SLOT_COUNT").jsonPrimitive.content shouldBe "8"
                root.getValue("CI_PHASE_SLOT_COUNT").jsonPrimitive.content shouldBe "8"
                root.getValue("CI_OUTCOME_SLOT_COUNT").jsonPrimitive.content shouldBe "4"
                root
                    .getValue("CI_ICON_ENVIRONMENTS")
                    .jsonArray
                    .map { it.jsonPrimitive.content }
                    .shouldContainExactly(
                        "github",
                        "teamcity",
                        "cloudflare",
                        "figma",
                        "codex",
                        "browser",
                        "terminal",
                        "operator",
                        "json",
                        "gradle",
                    )
                root
                    .getValue("CATALOG_TARGET_NAMES")
                    .jsonArray
                    .map { it.jsonPrimitive.content }
                    .shouldContainExactly(
                        "waterMyPlants.libraries",
                        "waterMyPlants.plugins",
                        "waterMyPlants.customGradleConventionPlugins",
                        "waterMyPlants.customGradlePlugins",
                        "gradlePlugins.libraries",
                        "gradlePlugins.plugins",
                        "figmaDocumentationSync.libraries",
                        "figmaDocumentationSync.plugins",
                    )

                val firstCatalogTarget =
                    root
                        .getValue("CATALOG_TREE_TARGETS")
                        .jsonArray
                        .first()
                        .jsonObject
                firstCatalogTarget
                    .getValue("nodesPath")
                    .jsonArray
                    .map { it.jsonPrimitive.content }
                    .shouldContainExactly(
                        "content",
                        "catalogs",
                        "waterMyPlants",
                        "libraries",
                    )
            }
        },
    ) {
    companion object {
        private val expectedKeys =
            setOf(
                "schemaVersion",
                "METADATA_PAGE_ID",
                "METADATA_NAMESPACE",
                "FIGMA_FILE_KEY",
                "PROJECT_DISPLAY_NAME",
                "MCP_CLIENT_NAME",
                "CANONICAL_STAGING_NAMESPACE",
                "PREVIEW_STAGING_NAMESPACE",
                "CI_DOCUMENTATION_PAGE_ID",
                "CI_NODE_COMPONENT_ID",
                "CI_ICON_COMPONENT_SET_ID",
                "CI_VARIABLE_COLLECTION_NAME",
                "CI_VARIABLE_MODE_NAMES",
                "CI_NODE_INSTANCE_NAME",
                "CI_ICON_INSTANCE_NAME",
                "CI_ICON_ENVIRONMENT_PROPERTY",
                "CI_ICON_ENVIRONMENTS",
                "CI_CONNECTOR_NAME",
                "CI_CONNECTOR_TEMPLATE_NAME",
                "CI_CONNECTOR_TEMPLATE_NODE_ID",
                "CI_NODE_PROPS",
                "CI_NODE_PHASE_CONTAINER_NAME",
                "CI_NODE_OUTCOME_CONTAINER_NAME",
                "CI_PHASE_COMPONENT_ID",
                "CI_PHASE_SLOT_NAME_PREFIX",
                "CI_PHASE_SLOT_COUNT",
                "CI_PHASE_PROPS",
                "CI_PHASE_STEP_CONTAINER_NAME",
                "CI_STEP_COMPONENT_SET_ID",
                "CI_STEP_SLOT_NAME_PREFIX",
                "CI_STEP_SLOT_COUNT",
                "CI_STEP_ROLES",
                "CI_STEP_PROPS",
                "CI_OUTCOME_COMPONENT_SET_ID",
                "CI_OUTCOME_SLOT_NAME_PREFIX",
                "CI_OUTCOME_SLOT_COUNT",
                "CI_OUTCOME_KINDS",
                "CI_OUTCOME_PROPS",
                "VERSIONS_COLLECTION_NAME",
                "VERSIONS_COLLECTION_NAMES",
                "VERSION_ALIAS_MODE_NAME",
                "VERSION_NUMBER_MODE_NAME",
                "OUTLINE_COLOR_VARIABLE_NAME",
                "DEPENDENCY_VERSION_COMPONENT_ID",
                "PROJECT_VERSION_COMPONENT_ID",
                "DEPENDENCY_VERSION_INSTANCE_NAMES",
                "DEPENDENCY_VERSION_PROPS",
                "PARENT_SECTION_SIBLING_GAP",
                "PARENT_SECTION_NODE_IDS",
                "SECTION_SIBLING_GAP",
                "TREE_NODE_COMPONENT_IDS",
                "CONNECTOR_TEMPLATE_NAME",
                "HEADER_INSTANCE_NAME",
                "HEADER_LINK_PROPERTY_NAME",
                "GITHUB_MAIN_BLOB_URL",
                "GITHUB_MAIN_TREE_URL",
                "CI_CONFIGURATION_MODEL_NAME",
                "CI_PIPELINE_NAME",
                "FIGMA_PIPELINE_NAME",
                "TEAMCITY_SOURCE",
                "TOPOLOGY_SOURCE",
                "WINDOWS_RUNTIME_SOURCE",
                "WINDOWS_RUNTIME_RUNBOOK_SOURCE",
                "VISUAL_CONTRACT_SOURCE",
                "BRANCH_PROTECTION_SOURCE",
                "CANONICAL_SYNC_SOURCE",
                "CANONICAL_DESIGN_MODEL_PATH",
                "REPOSITORY_ROOT_RELATIVE_TO_TOOLS",
                "CHANGE_IMPACT_POLICY_RELATIVE_TO_REPOSITORY",
                "HEADER_SECTION_TARGETS",
                "VERSION_SECTION_TARGETS",
                "TREE_NODE_PROPS",
                "ARTIFACT_PROPS",
                "ARTIFACTS_BUNDLE_PROPS",
                "ARTIFACT_INSTANCE_NAME",
                "ARTIFACTS_BUNDLE_INSTANCE_NAME",
                "USAGE_CHIP_COMPONENT_SET_ID",
                "USAGE_CHIP_INSTANCE_NAME",
                "TOOL_ARTIFACT_USAGE_INSTANCE_NAME",
                "TOOL_ARTIFACT_USAGE_PROPS",
                "USAGE_CHIP_PROPS",
                "USAGE_CHIP_KINDS",
                "CATALOG_TREE_TARGETS",
                "CI_VISUAL_TARGET_NAMES",
                "CATALOG_TARGET_NAMES",
                "WRITER_TARGET_NAMES",
                "DEFAULT_FIXTURE_TARGETS",
            )
    }
}
