@file:Suppress("AvoidDuplicateDependencies")

import com.marmatsan.figmaDocumentationSync.data.json.writer.FigmaWriterProjectConfigJson
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaCatalogTreeTargetConfig
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaCatalogTreeTargetLifecycle
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaCatalogTreeTargetType
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaHeaderSectionTarget
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaProjectLink
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaVersionSectionTarget
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaWriterProjectConfig
import com.marmatsan.figmaDocumentationSync.plugin.gradle.figmaDocumentationSyncExtension
import com.marmatsan.figmaDocumentationSync.plugin.gradle.platform.HostOperatingSystem
import com.marmatsan.figmaDocumentationSync.teamcity.operations.gradle.FigmaTeamCityOperationsExtension
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityCiConfigurationProvider
import com.marmatsan.verificationPlatform.plugin.extension.VerificationPlatformExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(plugins.plugins.com.android.application) apply false
    alias(plugins.plugins.com.android.library) apply false
    alias(plugins.plugins.com.figma.code.connect) apply false
    alias(plugins.plugins.com.google.devtools.ksp) apply false
    alias(plugins.plugins.com.google.protobuf) apply false
    alias(toolPlugins.plugins.com.marmatsan.verificationPlatform) apply true
    alias(plugins.plugins.de.mannodermaus.android.junit5) apply false
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.compose) apply false
    alias(toolPlugins.plugins.com.marmatsan.projectConfig.figma) apply true
    alias(toolPlugins.plugins.com.marmatsan.figmaDocumentationSync.teamcityOperations) apply true
}

extensions.configure<VerificationPlatformExtension> {
    ciPolicy {
        toolingPathPrefixes.set(
            listOf(
                "repo/figma-documentation-sync/"
            )
        )
        buildInfrastructurePathPrefixes.set(
            listOf(
                "repo/dependency-catalog/",
                "repo/gradle-plugins/",
                "repo/project-config/",
                "repo/unit-testing/",
                "repo/verification-platform/",
                "config/figma/"
            )
        )
        buildInfrastructurePaths.set(
            listOf(
                "settings.gradle.kts",
                "build.gradle.kts",
                "gradle.properties"
            )
        )
        portableDistributionPathPrefixes.set(
            listOf(
                "repo/dependency-catalog/catalog-api/",
                "repo/dependency-catalog/catalog-core/",
                "repo/dependency-catalog/catalog-gradle-plugin/",
                "repo/dependency-catalog/catalog-tree-gradle-plugin/",
                "repo/dependency-catalog/samples/standalone-consumer/",
                "repo/dependency-catalog/samples/standalone-tree-consumer/",
                "repo/figma-documentation-sync/domain/",
                "repo/figma-documentation-sync/data/",
                "repo/figma-documentation-sync/plugin/",
                "repo/figma-documentation-sync/teamcity-adapter/",
                "repo/figma-documentation-sync/teamcity-operations/",
                "repo/figma-documentation-sync/tools/",
                "repo/figma-documentation-sync/samples/standalone-consumer/",
                "repo/gradle-plugins/",
                "repo/project-config/",
                "repo/unit-testing/",
                "repo/verification-platform/"
            )
        )
        portableDistributionPaths.set(
            listOf(
                "repo/dependency-catalog/build.gradle.kts",
                "repo/dependency-catalog/settings.gradle.kts",
                "repo/dependency-catalog/versions.properties",
                "repo/figma-documentation-sync/build.gradle.kts",
                "repo/figma-documentation-sync/gradle.properties",
                "repo/figma-documentation-sync/settings.gradle.kts",
                "repo/figma-documentation-sync/versions.properties",
                "repo/gradle-plugins/build.gradle.kts",
                "repo/gradle-plugins/settings.gradle.kts",
                "repo/gradle-plugins/versions.properties",
                "repo/project-config/build.gradle.kts",
                "repo/project-config/settings.gradle.kts",
                "repo/project-config/versions.properties",
                "repo/unit-testing/build.gradle.kts",
                "repo/unit-testing/settings.gradle.kts",
                "repo/unit-testing/versions.properties",
                "repo/verification-platform/build.gradle.kts",
                "repo/verification-platform/settings.gradle.kts",
                "repo/verification-platform/versions.properties"
            )
        )
        toolingCapabilities.set(
            listOf(
                "java",
                "android-sdk",
                "node"
            )
        )
        buildInfrastructureCapabilities.set(
            listOf(
                "java",
                "android-sdk"
            )
        )
        portableDistributionCapabilities.set(
            listOf(
                "java",
                "android-sdk",
                "node"
            )
        )
        buildInfrastructureVerificationTasks.set(
            listOf(
                "checkDependencyCatalogArchitecture",
                "checkIncludedBuildVersions",
                "checkModuleBoundaries"
            )
        )
        portableDistributionVerificationTasks.set(
            listOf(
                "verifyPortableDistribution"
            )
        )
        targetedModuleSupplementalTasks.set(
            listOf(
                "checkFigmaCatalogUsage"
            )
        )
    }

    boundaries {
        listOf(
            "repo/dependency-catalog",
            "repo/gradle-plugins",
            "repo/figma-documentation-sync",
            "repo/project-config",
            "repo/verification-platform",
            "repo/unit-testing"
        ).forEach(::versionedBuild)

        alignedVersion("kotlinResultLibraryVersion")
        alignedVersion("dependencyCatalogVersion")
        alignedVersion("gradlePluginsVersion")

        reusableScope(
            "repo/dependency-catalog/catalog-api",
            "com.marmatsan.dependencies.catalog.DependencyCatalogTrees",
            "com.marmatsan.dependencies.gradle",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform"
        )
        reusableScope(
            "repo/dependency-catalog/catalog-core",
            "com.marmatsan.dependencies.gradle",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform"
        )
        reusableScope(
            "repo/dependency-catalog/catalog-gradle-plugin",
            "com.marmatsan.dependencies.catalog.DependencyCatalogTrees",
            "com.marmatsan.dependencies.tree",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform"
        )
        reusableScope(
            "repo/dependency-catalog/catalog-tree-gradle-plugin",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform"
        )
        reusableScope(
            "repo/figma-documentation-sync",
            "com.marmatsan.dependencies"
        )
        reusableScope(
            "repo/gradle-plugins",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform"
        )
        reusableScope("repo/project-config")
        reusableScope(
            "repo/unit-testing",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform"
        )
        reusableScope(
            "repo/verification-platform",
            "repo/dependency-catalog",
            "repo/figma-documentation-sync",
            "repo/gradle-plugins"
        )
    }

    typedErrorHandling {
        standardResult(
            qualifiedName = "com.github.michaelbull.result.Result"
        )
        listOf(
            "app",
            "core",
            "onboarding",
            "repo"
        ).forEach(::productionSourceScope)
    }

    taskBindings {
        includedBuildTask(
            name = "checkDependencyCatalogArchitecture",
            buildName = "dependency-catalog",
            taskPath = ":checkDependencyCatalogArchitecture",
            description = "Verifies the portable dependency catalog architecture.",
            requiredByCheck = true
        )
        includedBuildTask(
            name = "verifyDependencyCatalogDistribution",
            buildName = "dependency-catalog",
            taskPath = ":verifyStagedPublication",
            description = "Verifies the staged dependency catalog through a standalone consumer."
        )
        includedBuildTask(
            name = "verifyFigmaDocumentationSyncDistribution",
            buildName = "figma-documentation-sync",
            taskPath = ":verifyStagedPublication",
            description = "Verifies the staged Figma plugin through a standalone consumer."
        )
        isolatedGradleBuildTask(
            name = "verifyGradlePluginsDistribution",
            buildDirectory = file("repo/gradle-plugins"),
            taskPath = ":verifyStagedPublication",
            projectProperties =
                mapOf(
                    "dependencyCatalogSourceBuild" to
                        file("repo/dependency-catalog").absolutePath
                ),
            description = "Verifies staged convention plugins through a standalone consumer."
        )
        includedBuildTask(
            name = "verifyUnitTestingDistribution",
            buildName = "unit-testing",
            taskPath = ":verifyStagedPublication",
            description = "Verifies the staged unit-test DSL through a standalone consumer."
        )
        includedBuildTask(
            name = "verifyVerificationPlatformDistribution",
            buildName = "verification-platform",
            taskPath = ":verifyStagedPublication",
            description = "Verifies the staged verification plugin through a standalone consumer."
        )
        includedBuildTask(
            name = "checkKotlinStyle",
            buildName = "verification-platform",
            taskPath = ":data:checkRepositoryKotlinStyle",
            description = "Checks repository Kotlin sources with the canonical KtLint rules.",
            requiredByCheck = true
        )
        includedBuildTask(
            name = "formatKotlinStyle",
            buildName = "verification-platform",
            taskPath = ":data:formatRepositoryKotlinStyle",
            description = "Formats repository Kotlin sources with the canonical KtLint rules.",
            group = "formatting"
        )
    }

    teamCity {
        infrastructureHealthBuildTypeId.set("WaterMyPlants_WaterMyPlantsInfrastructureHealth")
        pom.set(layout.projectDirectory.file(".teamcity/pom.xml"))
        generatedConfigurationDirectory.set(
            layout.projectDirectory.dir(".teamcity/target/generated-configs")
        )
        pipelineBuildTypeId.set("WaterMyPlantsCi")
        gateBuildTypeId.set("WaterMyPlantsCiGate")
        authoritativeStatusName.set("TeamCity CI")
    }
}

tasks.register("verifyPortableDistribution") {
    group = "verification"
    description = "Verifies every staged reusable artifact through source-independent consumers."
    dependsOn(
        "verifyDependencyCatalogDistribution",
        "verifyFigmaDocumentationSyncDistribution",
        "verifyGradlePluginsDistribution",
        "verifyProjectConfigDistribution",
        "verifyUnitTestingDistribution",
        "verifyVerificationPlatformDistribution"
    )
}

val projectConfigVersion: String =
    java.util.Properties().run {
        file("repo/project-config/versions.properties").inputStream().use(::load)
        getProperty("projectConfigVersion")
    }
val projectConfigConsumerWorkingDirectory =
    layout.projectDirectory.dir("repo/project-config/samples/health-consumer")
val projectConfigConsumerCommand: List<String> =
    listOf(
        layout.projectDirectory
            .file(
                if (
                    System
                        .getProperty("os.name")
                        .startsWith(
                            "Windows",
                            ignoreCase = true
                        )
                ) {
                    "gradlew.bat"
                } else {
                    "gradlew"
                }
            ).asFile.absolutePath,
        "--no-daemon",
        "--configuration-cache",
        "verifyProjectConfig",
        "-PprojectConfigVersion=$projectConfigVersion",
        "-PprojectConfigPublicationRepository=" +
            layout.projectDirectory
                .dir("repo/project-config/build/publication-repository")
                .asFile.absolutePath,
        "-PdependencyCatalogPublicationRepository=" +
            layout.projectDirectory
                .dir("repo/dependency-catalog/build/publication-repository")
                .asFile.absolutePath,
        "-PfigmaDocumentationSyncPublicationRepository=" +
            layout.projectDirectory
                .dir("repo/figma-documentation-sync/build/publication-repository")
                .asFile.absolutePath,
        "--stacktrace"
    )
val verifyProjectConfigPublishedConsumer =
    tasks.register<Exec>("verifyProjectConfigPublishedConsumer") {
        group = "verification"
        description = "Verifies project-config from the staged publications."
        dependsOn(
            gradle
                .includedBuild("dependency-catalog")
                .task(":publishPortablePublicationToStagingRepository"),
            gradle
                .includedBuild("figma-documentation-sync")
                .task(":publishPortablePublicationToStagingRepository"),
            gradle
                .includedBuild("project-config")
                .task(":publishPortablePublicationToStagingRepository")
        )
        workingDir(projectConfigConsumerWorkingDirectory)
        commandLine(projectConfigConsumerCommand)
    }
val verifyProjectConfigConsumerConfigurationCache =
    tasks.register<Exec>("verifyProjectConfigConsumerConfigurationCache") {
        group = "verification"
        description = "Verifies that the published Health consumer is configuration-cache compatible."
        dependsOn(verifyProjectConfigPublishedConsumer)
        workingDir(projectConfigConsumerWorkingDirectory)
        commandLine(projectConfigConsumerCommand)
    }

tasks.register("verifyProjectConfigDistribution") {
    group = "verification"
    description = "Verifies the source-independent project-config distribution contract."
    dependsOn(verifyProjectConfigConsumerConfigurationCache)
}

val cleanTemporaryArtifacts =
    tasks.register<Delete>("cleanTemporaryArtifacts") {
        group = "build"
        description = "Deletes repository-owned temporary and generated tooling artifacts."
        delete(
            layout.projectDirectory.dir("tmp"),
            layout.projectDirectory.dir("repo/figma-documentation-sync/tools/dist")
        )
    }

tasks.named("clean") {
    dependsOn(cleanTemporaryArtifacts)
}

val reusableBuildChecks =
    listOf(
        "figma-documentation-sync",
        "gradle-plugins",
        "project-config",
        "unit-testing",
        "verification-platform"
    ).map { buildName ->
        gradle.includedBuild(buildName).task(":check")
    }

tasks.named("check") {
    dependsOn(
        reusableBuildChecks,
        "testFigmaDocumentationSyncTools"
    )
}

/** Repository-owned composition of Water My Plants Figma identities and publication targets. */
private object WaterMyPlantsFigmaWriterProjectConfig {
    private const val GITHUB_MAIN_BLOB_URL = "https://github.com/marmatsan/water-my-plants/blob/main"
    private const val GITHUB_MAIN_TREE_URL = "https://github.com/marmatsan/water-my-plants/tree/main"

    /** Complete portable project contract consumed by the reusable Figma plugin and writer. */
    val value =
        FigmaWriterProjectConfig(
            metadataPageId = "62934:908",
            metadataNamespace = "water_my_plants_sync",
            figmaFileKey = "YBZXsd8oyGLbcI2KWxJvRK",
            projectDisplayName = "Water My Plants",
            mcpClientName = "water-my-plants-figma-sync",
            ciDocumentationPageId = "63153:2876",
            ciNodeComponentId = "64670:3088",
            ciIconComponentSetId = "64361:716",
            ciVariableCollectionName = "ci/cd",
            ciVariableModeNames =
                listOf(
                    "Actor",
                    "System",
                    "Git reference",
                    "Pipeline",
                    "Job",
                    "Artifact",
                    "Check",
                    "Gate"
                ),
            ciNodeInstanceName = ".ci node",
            ciIconInstanceName = ".ci icon",
            ciIconEnvironmentProperty = "environment",
            ciIconEnvironments =
                listOf(
                    "github",
                    "teamcity",
                    "cloudflare",
                    "figma",
                    "codex",
                    "browser",
                    "terminal",
                    "operator",
                    "json",
                    "gradle"
                ),
            ciConnectorName = ".ci connector",
            ciConnectorTemplateName = "simple-line_arrow / neutral",
            ciConnectorTemplateNodeId = "64835:3289",
            ciNodeProps =
                mapOf(
                    "name" to "name",
                    "description" to "description",
                    "executionPlanHeading" to "execution plan heading",
                    "source" to "source",
                    "runtimePlatform" to "runtime platform",
                    "runtimeService" to "runtime service",
                    "runtimeStartup" to "runtime startup",
                    "runtimeIdentity" to "runtime identity",
                    "showExecutionPlan" to "show execution plan",
                    "showOutcome" to "show outcome",
                    "showSource" to "show source",
                    "showRuntime" to "show runtime",
                    "showOptionalDetails" to "show optional details"
                ),
            ciNodePhaseContainerName = "execution plan",
            ciNodeOutcomeContainerName = "outcome",
            ciPhaseComponentId = "64668:2944",
            ciPhaseSlotNamePrefix = "phase",
            ciPhaseSlotCount = 8,
            ciPhaseProps =
                mapOf(
                    "order" to "order",
                    "title" to "title",
                    "technicalId" to "technical id",
                    "description" to "description",
                    "showTechnicalId" to "show technical id",
                    "showDescription" to "show description",
                    "showSteps" to "show steps"
                ),
            ciPhaseStepContainerName = "steps",
            ciStepComponentSetId = "64665:2991",
            ciStepSlotNamePrefix = "step",
            ciStepSlotCount = 8,
            ciStepRoles =
                listOf(
                    "action",
                    "decision",
                    "group"
                ),
            ciStepProps =
                mapOf(
                    "order" to "order",
                    "title" to "title",
                    "technicalId" to "technical id",
                    "tasks" to "tasks",
                    "description" to "description",
                    "condition" to "condition",
                    "showTechnicalId" to "show technical id",
                    "showDescription" to "show description",
                    "showCondition" to "show condition",
                    "role" to "role"
                ),
            ciOutcomeComponentSetId = "64669:3118",
            ciOutcomeSlotNamePrefix = "outcome",
            ciOutcomeSlotCount = 4,
            ciOutcomeKinds =
                listOf(
                    "artifact",
                    "check",
                    "success",
                    "action"
                ),
            ciOutcomeProps =
                mapOf(
                    "order" to "order",
                    "title" to "title",
                    "technicalId" to "technical id",
                    "description" to "description",
                    "condition" to "condition",
                    "showTechnicalId" to "show technical id",
                    "showDescription" to "show description",
                    "showCondition" to "show condition",
                    "kind" to "kind"
                ),
            versionsCollectionName = "versions.properties",
            versionAliasModeName = "Version alias",
            versionNumberModeName = "Version number",
            outlineColorVariableName = "md/sys/color/outline",
            surfaceColorVariableName = "md/sys/color/surface",
            dependencyVersionComponentId = "63075:591",
            dependencyVersionInstanceNames =
                listOf(
                    ".dependency version",
                    ".project version"
                ),
            dependencyVersionProps =
                mapOf(
                    "alias" to "version alias#63075:0",
                    "number" to "version number#63075:1"
                ),
            parentSectionSiblingGap = 1139,
            parentSectionNodeIds =
                listOf(
                    "63685:108540",
                    "62936:183",
                    "63099:949",
                    "64886:247",
                    "64886:248"
                ),
            parentSectionCornerRadius = 28,
            sectionSiblingGap = 114,
            treeNodeComponentIds =
                mapOf(
                    "Library" to "63069:681",
                    "Plugin" to "63069:694"
                ),
            connectorTemplateName = "simple-solid_arrow",
            headerInstanceName = ".Header",
            headerLinkPropertyName = "Link",
            headerDefinitionPropertyName = "Definition",
            githubMainBlobUrl = GITHUB_MAIN_BLOB_URL,
            githubMainTreeUrl = GITHUB_MAIN_TREE_URL,
            ciConfigurationModelName = "teamCity",
            ciPipelineName = "CI",
            figmaPipelineName = "Figma Sync",
            teamCitySource = ".teamcity/settings.kts",
            topologySource = "docs/ci/external-topology.yaml",
            windowsRuntimeSource = "docs/ci/windows-runtime.yaml",
            windowsRuntimeRunbookSource = "docs/runbooks/teamcity-cloudflare-access.md",
            visualContractSource = "docs/ci/visual-model-contract.md",
            branchProtectionSource = "docs/ci/main-branch-protection.md",
            canonicalSyncSource = "repo/figma-documentation-sync/docs/runbooks/canonical-artifact-visual-sync.md",
            canonicalDesignModelPath = "build/reports/figma-sync/design-model.json",
            repositoryRootRelativeToTools = "../../..",
            changeImpactPolicyRelativeToRepository =
                "config/figma/change-impact-policy.json",
            headerSectionTargets =
                listOf(
                    FigmaHeaderSectionTarget(
                        sectionNodeId = "63685:108540",
                        links =
                            links(
                                "repo/dependency-catalog/catalog-core/src/main/kotlin/" +
                                    "com/marmatsan/dependencies/tree/dsl/library/LibraryTreeDsl.kt",
                                "repo/dependency-catalog/catalog-core/src/main/kotlin/" +
                                    "com/marmatsan/dependencies/tree/dsl/library/LibraryScope.kt",
                                "repo/dependency-catalog/catalog-core/src/main/kotlin/" +
                                    "com/marmatsan/dependencies/tree/dsl/plugin/PluginTreeDsl.kt"
                            )
                    ),
                    FigmaHeaderSectionTarget(
                        sectionNodeId = "62936:183",
                        links =
                            links(
                                "versions.properties"
                            ),
                        definition =
                            "Represents versions.properties, " +
                                "the repository-owned source for dependency and plugin versions " +
                                "consumed by the Gradle builds."
                    ),
                    FigmaHeaderSectionTarget(
                        sectionNodeId = "63099:949",
                        links =
                            links(
                                "settings.gradle.kts"
                            )
                    ),
                    FigmaHeaderSectionTarget(
                        sectionNodeId = "64886:247",
                        links =
                            listOf(
                                FigmaProjectLink(
                                    label = "repo/gradle-plugins",
                                    url = "$GITHUB_MAIN_TREE_URL/repo/gradle-plugins"
                                )
                            )
                    ),
                    FigmaHeaderSectionTarget(
                        sectionNodeId = "64886:248",
                        links =
                            links(
                                "repo/dependency-catalog/catalog-gradle-plugin/src/main/kotlin/" +
                                    "com/marmatsan/dependencies/gradle/DependencyCatalogSettingsPlugin.kt",
                                "repo/figma-documentation-sync/plugin/src/main/kotlin/" +
                                    "com/marmatsan/figmaDocumentationSync/plugin/gradle/" +
                                    "FigmaDocumentationSyncGradlePlugin.kt",
                                "repo/verification-platform/plugin/src/main/kotlin/" +
                                    "com/marmatsan/verificationPlatform/plugin/VerificationPlatformPlugin.kt",
                                "repo/project-config/plugin/src/main/kotlin/com/marmatsan/" +
                                    "projectConfig/project/ProjectConfigGradlePlugin.kt",
                                "repo/project-config/plugin/src/main/kotlin/com/marmatsan/" +
                                    "projectConfig/settings/ProjectConfigSettingsPlugin.kt",
                                "repo/project-config/figma-adapter/src/main/kotlin/com/marmatsan/" +
                                    "projectConfig/figma/ProjectConfigFigmaGradlePlugin.kt"
                            )
                    )
                ),
            versionSectionTargets =
                mapOf(
                    "Main project dependencies" to
                        FigmaVersionSectionTarget(
                            parentNodeId = "64247:3827",
                            variableFolder = "Main project dependencies"
                        ),
                    "Libraries" to
                        FigmaVersionSectionTarget(
                            parentNodeId = "64247:3853",
                            variableFolder = "Libraries"
                        ),
                    "Plugins" to
                        FigmaVersionSectionTarget(
                            parentNodeId = "64247:3854",
                            variableFolder = "Plugins"
                        )
                ),
            treeNodeProps =
                mapOf(
                    "libraryGroup" to "Library group#1345:12",
                    "pluginId" to "Plugin ID#1345:16",
                    "showPluginVersion" to "Show plugin version#58719:0",
                    "showArtifacts" to "Show artifacts#63079:0",
                    "pluginVersion" to "Plugin version#63081:2",
                    "showAppliedByModule" to "Show applied by module",
                    "showUsedByConventionPlugin" to "Show used by convention plugin",
                    "showUnused" to "Show unused",
                    "showIsGradlePlugin" to "Show is a gradle plugin#63112:4",
                    "type" to "Type"
                ),
            artifactProps =
                mapOf(
                    "name" to "Artifact name",
                    "version" to "Artifact version",
                    "showVersion" to "Show version",
                    "showAppliedByPlugin" to "Show applied by plugin",
                    "showUsedByModule" to "Show used by module",
                    "showConfiguredAsTool" to "Show configured as tool"
                ),
            artifactsBundleProps =
                mapOf(
                    "alias" to "Alias",
                    "version" to "Version",
                    "showVersion" to "With version",
                    "showAppliedByPlugin" to "Show applied by plugin",
                    "showUsedByModule" to "Show used by module"
                ),
            artifactInstanceName = ".artifact",
            artifactsBundleInstanceName = ".artifacts bundle",
            usageChipComponentSetId = "63085:793",
            usageChipInstanceName = ".usage chip",
            toolArtifactUsageInstanceName = ".tool artifact usage",
            toolArtifactUsageProps = mapOf("target" to "tool artifact target"),
            usageChipProps =
                mapOf(
                    "kind" to "kind",
                    "name" to "name"
                ),
            usageChipKinds =
                mapOf(
                    "module" to "module",
                    "conventionPlugin" to "convention-plugin"
                ),
            catalogTreeTargets =
                listOf(
                    catalogTarget(
                        name = "waterMyPlants.libraries",
                        sectionNodeId = "63069:629",
                        type = FigmaCatalogTreeTargetType.LIBRARY,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.STABLE_DOCUMENTATION_TARGET,
                        catalog = "waterMyPlants",
                        collection = "libraries"
                    ),
                    catalogTarget(
                        name = "waterMyPlants.plugins",
                        sectionNodeId = "63069:594",
                        type = FigmaCatalogTreeTargetType.PLUGIN,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.STABLE_DOCUMENTATION_TARGET,
                        catalog = "waterMyPlants",
                        collection = "plugins",
                        versionValuesPath =
                            listOf(
                                "content",
                                "versions"
                            ),
                        sharedVersionKeys = listOf("gradlePluginsVersion")
                    ),
                    catalogTarget(
                        name = "waterMyPlants.customGradleConventionPlugins",
                        sectionNodeId = "64886:247",
                        type = FigmaCatalogTreeTargetType.PLUGIN,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.STABLE_DOCUMENTATION_TARGET,
                        catalog = "waterMyPlants",
                        collection = "customGradleConventionPlugins",
                        gradlePluginNodes = true,
                        warnWhenUnused = true
                    ),
                    catalogTarget(
                        name = "waterMyPlants.customGradlePlugins",
                        sectionNodeId = "64886:248",
                        type = FigmaCatalogTreeTargetType.PLUGIN,
                        lifecycle = FigmaCatalogTreeTargetLifecycle.STABLE_DOCUMENTATION_TARGET,
                        catalog = "waterMyPlants",
                        collection = "customGradlePlugins",
                        gradlePluginNodes = true,
                        warnWhenUnused = true
                    )
                ),
            ciVisualTargetNames =
                listOf(
                    "ci.overview",
                    "ci.pullRequestIntegration",
                    "ci.postMergeDesignDocumentation",
                    "ci.jobTasks",
                    "ci.infrastructureAndAccess",
                    "ci.windowsRuntime"
                ),
            defaultFixtureTargets =
                mapOf(
                    "catalog-tree" to "waterMyPlants.plugins",
                    "versions" to "versions"
                )
        )

    private fun links(
        vararg paths: String
    ): List<FigmaProjectLink> =
        paths.map { path ->
            FigmaProjectLink(
                label = path,
                url = "$GITHUB_MAIN_BLOB_URL/$path"
            )
        }

    private fun catalogTarget(
        name: String,
        sectionNodeId: String,
        type: FigmaCatalogTreeTargetType,
        lifecycle: FigmaCatalogTreeTargetLifecycle,
        catalog: String,
        collection: String,
        gradlePluginNodes: Boolean = false,
        warnWhenUnused: Boolean = false,
        versionValuesPath: List<String> = emptyList(),
        sharedVersionKeys: List<String> = emptyList()
    ) = FigmaCatalogTreeTargetConfig(
        name = name,
        sectionNodeId = sectionNodeId,
        type = type,
        lifecycle = lifecycle,
        nodesPath =
            listOf(
                "content",
                "catalogs",
                catalog,
                collection
            ),
        gradlePluginNodes = gradlePluginNodes,
        warnWhenUnused = warnWhenUnused,
        versionValuesPath = versionValuesPath,
        sharedVersionKeys = sharedVersionKeys
    )
}

val writerConfig = WaterMyPlantsFigmaWriterProjectConfig.value

extensions.configure<figmaDocumentationSyncExtension> {
    designModelMetadataNodeUrl.set(
        "https://www.figma.com/design/${writerConfig.figmaFileKey}/Water-My-Plants" +
            "?node-id=${writerConfig.metadataPageId.replace(
                ':',
                '-'
            )}"
    )
    metadataNamespace.set(writerConfig.metadataNamespace)
    primaryCatalogModelName.set("waterMyPlants")
    ciDocumentationEnabled.set(true)
    ciConfigurationModelName.set("teamCity")
    ciConfigurationProviderClassName.set(TeamCityCiConfigurationProvider::class.java.name)
    ciDefaultBranchAlias.set("<default>")
    versionsFile.set(layout.projectDirectory.file("versions.properties"))
    ciExternalTopologyFile.set(layout.projectDirectory.file("docs/ci/external-topology.yaml"))
    ciWindowsRuntimeFile.set(layout.projectDirectory.file("docs/ci/windows-runtime.yaml"))
    ciGeneratedConfigurationDirectory.set(layout.projectDirectory.dir(".teamcity/target/generated-configs"))
    changeImpactPolicyFile.set(layout.projectDirectory.file("config/figma/change-impact-policy.json"))
    toolsDirectory.set(layout.projectDirectory.dir("repo/figma-documentation-sync/tools"))
    writerProjectConfigJson.set(FigmaWriterProjectConfigJson.encode(writerConfig))
    ciConfigurationCommand.set(teamCityConfigurationCommand())

    includedBuilds.register("dependency-catalog") {
        modelName.set("dependencyCatalog")
        settingsFile.set(layout.projectDirectory.file("repo/dependency-catalog/settings.gradle.kts"))
        rootDirectory.set(layout.projectDirectory.dir("repo/dependency-catalog"))
        modulePathPrefix.set(":dependency-catalog")
    }
    includedBuilds.register("figma-documentation-sync") {
        modelName.set("figmaDocumentationSync")
        settingsFile.set(layout.projectDirectory.file("repo/figma-documentation-sync/settings.gradle.kts"))
        rootDirectory.set(layout.projectDirectory.dir("repo/figma-documentation-sync"))
        modulePathPrefix.set(":figma-documentation-sync")
    }
    includedBuilds.register("gradle-plugins") {
        modelName.set("gradlePlugins")
        settingsFile.set(layout.projectDirectory.file("repo/gradle-plugins/settings.gradle.kts"))
        rootDirectory.set(layout.projectDirectory.dir("repo/gradle-plugins"))
        modulePathPrefix.set(":gradle-plugins")
        publishesConventionPlugins.set(true)
    }
    includedBuilds.register("project-config") {
        modelName.set("projectConfig")
        settingsFile.set(layout.projectDirectory.file("repo/project-config/settings.gradle.kts"))
        rootDirectory.set(layout.projectDirectory.dir("repo/project-config"))
        modulePathPrefix.set(":project-config")
    }
    includedBuilds.register("unit-testing") {
        modelName.set("unitTesting")
        settingsFile.set(layout.projectDirectory.file("repo/unit-testing/settings.gradle.kts"))
        rootDirectory.set(layout.projectDirectory.dir("repo/unit-testing"))
        modulePathPrefix.set(":unit-testing")
    }
    includedBuilds.register("verification-platform") {
        modelName.set("verificationPlatform")
        settingsFile.set(layout.projectDirectory.file("repo/verification-platform/settings.gradle.kts"))
        rootDirectory.set(layout.projectDirectory.dir("repo/verification-platform"))
        modulePathPrefix.set(":verification-platform")
    }
}

extensions.configure<FigmaTeamCityOperationsExtension> {
    buildTypeId.set("WaterMyPlants_WaterMyPlantsFigmaSync")
    branch.set("main")
    mainBranchAliases.set(
        listOf(
            "main",
            "<default>",
            "refs/heads/main"
        )
    )
    requiredBuildTypeName.set("Generate main design model")
    serverUrl.set("https://teamcity.marmatsan.dev")
}

fun teamCityConfigurationCommand(): List<String> {
    val wrapper =
        layout.projectDirectory
            .file(if (HostOperatingSystem.isWindows) "mvnw.cmd" else "mvnw")
            .asFile.absolutePath
    val arguments =
        listOf(
            wrapper,
            "-f",
            layout.projectDirectory
                .file(".teamcity/pom.xml")
                .asFile.absolutePath,
            "teamcity-configs:generate"
        )

    return if (HostOperatingSystem.isWindows) {
        listOf(
            "cmd.exe",
            "/d",
            "/c"
        ) + arguments
    } else {
        arguments
    }
}
