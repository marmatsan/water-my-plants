@file:Suppress("AvoidDuplicateDependencies")

import com.marmatsan.verificationPlatform.plugin.extension.VerificationPlatformExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(plugins.plugins.com.android.application) apply false
    alias(plugins.plugins.com.android.library) apply false
    alias(plugins.plugins.com.figma.code.connect) apply false
    alias(plugins.plugins.com.google.devtools.ksp) apply false
    alias(plugins.plugins.com.google.protobuf) apply false
    alias(plugins.plugins.de.mannodermaus.android.junit5) apply false
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.compose) apply false
    id("com.marmatsan.android") apply false
    id("com.marmatsan.bddTest") apply false
    id("com.marmatsan.compose") apply false
    id("com.marmatsan.verificationPlatform") apply true
    id("com.marmatsan.waterMyPlantsProjectConfig") apply true
    id("com.marmatsan.protobuf") apply false
    id("com.marmatsan.unitTest") apply false
}

extensions.configure<VerificationPlatformExtension> {
    ciPolicy {
        toolingPathPrefixes.set(
            listOf(
                "repo/figma-documentation-sync/",
            ),
        )
        buildInfrastructurePathPrefixes.set(
            listOf(
                "repo/dependency-catalog/",
                "repo/gradle-plugins/",
                "repo/unit-testing/",
                "repo/verification-platform/",
                "repo/water-my-plants-project-config/",
            ),
        )
        buildInfrastructurePaths.set(
            listOf(
                "settings.gradle.kts",
                "build.gradle.kts",
                "gradle.properties",
            ),
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
                "repo/figma-documentation-sync/tools/",
                "repo/figma-documentation-sync/samples/standalone-consumer/",
                "repo/gradle-plugins/",
                "repo/unit-testing/",
                "repo/verification-platform/",
            ),
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
                "repo/unit-testing/build.gradle.kts",
                "repo/unit-testing/settings.gradle.kts",
                "repo/unit-testing/versions.properties",
                "repo/verification-platform/build.gradle.kts",
                "repo/verification-platform/settings.gradle.kts",
                "repo/verification-platform/versions.properties",
            ),
        )
        toolingCapabilities.set(
            listOf(
                "java",
                "android-sdk",
                "node",
            ),
        )
        buildInfrastructureCapabilities.set(
            listOf(
                "java",
                "android-sdk",
            ),
        )
        portableDistributionCapabilities.set(
            listOf(
                "java",
                "android-sdk",
                "node",
            ),
        )
        buildInfrastructureVerificationTasks.set(
            listOf(
                "checkDependencyCatalogArchitecture",
                "checkIncludedBuildVersions",
                "checkModuleBoundaries",
            ),
        )
        portableDistributionVerificationTasks.set(
            listOf(
                "verifyPortableDistribution",
            ),
        )
        targetedModuleSupplementalTasks.set(
            listOf(
                "checkFigmaCatalogUsage",
            ),
        )
    }

    boundaries {
        listOf(
            "repo/dependency-catalog",
            "repo/gradle-plugins",
            "repo/figma-documentation-sync",
            "repo/verification-platform",
            "repo/unit-testing",
            "repo/water-my-plants-project-config",
        ).forEach(::versionedBuild)

        alignedVersion("kotlinResultLibraryVersion")
        alignedVersion("dependencyCatalogVersion")

        reusableScope(
            "repo/dependency-catalog/catalog-api",
            "com.marmatsan.dependencies.catalog.DependencyCatalogTrees",
            "com.marmatsan.dependencies.gradle",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform",
        )
        reusableScope(
            "repo/dependency-catalog/catalog-core",
            "com.marmatsan.dependencies.gradle",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform",
        )
        reusableScope(
            "repo/dependency-catalog/catalog-gradle-plugin",
            "com.marmatsan.dependencies.catalog.DependencyCatalogTrees",
            "com.marmatsan.dependencies.tree",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform",
        )
        reusableScope(
            "repo/dependency-catalog/catalog-tree-gradle-plugin",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform",
            "com.marmatsan.waterMyPlants",
        )
        reusableScope(
            "repo/figma-documentation-sync",
            "com.marmatsan.dependencies",
            "com.marmatsan.waterMyPlants",
        )
        reusableScope(
            "repo/gradle-plugins",
            "WaterMyPlantsCatalog",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform",
            "com.marmatsan.waterMyPlants",
        )
        reusableScope(
            "repo/unit-testing",
            "com.marmatsan.figmaDocumentationSync",
            "com.marmatsan.verificationPlatform",
        )
        reusableScope(
            "repo/verification-platform",
            "repo/dependency-catalog",
            "repo/figma-documentation-sync",
            "repo/gradle-plugins",
            "repo/water-my-plants-project-config",
        )
    }

    typedErrorHandling {
        standardResult(
            qualifiedName = "com.github.michaelbull.result.Result",
        )
        listOf(
            "app",
            "core",
            "onboarding",
            "repo",
        ).forEach(::productionSourceScope)
    }

    taskBindings {
        includedBuildTask(
            name = "checkDependencyCatalogArchitecture",
            buildName = "dependency-catalog",
            taskPath = ":checkDependencyCatalogArchitecture",
            description = "Verifies the portable dependency catalog architecture.",
            requiredByCheck = true,
        )
        includedBuildTask(
            name = "verifyDependencyCatalogDistribution",
            buildName = "dependency-catalog",
            taskPath = ":verifyStagedPublication",
            description = "Verifies the staged dependency catalog through a standalone consumer.",
        )
        includedBuildTask(
            name = "verifyFigmaDocumentationSyncDistribution",
            buildName = "figma-documentation-sync",
            taskPath = ":verifyStagedPublication",
            description = "Verifies the staged Figma plugin through a standalone consumer.",
        )
        isolatedGradleBuildTask(
            name = "verifyGradlePluginsDistribution",
            buildDirectory = file("repo/gradle-plugins"),
            taskPath = ":verifyStagedPublication",
            projectProperties =
                mapOf(
                    "dependencyCatalogSourceBuild" to
                        file("repo/dependency-catalog").absolutePath,
                ),
            description = "Verifies staged convention plugins through a standalone consumer.",
        )
        includedBuildTask(
            name = "verifyUnitTestingDistribution",
            buildName = "unit-testing",
            taskPath = ":verifyStagedPublication",
            description = "Verifies the staged unit-test DSL through a standalone consumer.",
        )
        includedBuildTask(
            name = "verifyVerificationPlatformDistribution",
            buildName = "verification-platform",
            taskPath = ":verifyStagedPublication",
            description = "Verifies the staged verification plugin through a standalone consumer.",
        )
        includedBuildTask(
            name = "checkKotlinStyle",
            buildName = "verification-platform",
            taskPath = ":data:checkRepositoryKotlinStyle",
            description = "Checks repository Kotlin sources with the canonical KtLint rules.",
            requiredByCheck = true,
        )
        includedBuildTask(
            name = "formatKotlinStyle",
            buildName = "verification-platform",
            taskPath = ":data:formatRepositoryKotlinStyle",
            description = "Formats repository Kotlin sources with the canonical KtLint rules.",
            group = "formatting",
        )
    }

    teamCity {
        infrastructureHealthBuildTypeId.set("WaterMyPlants_WaterMyPlantsInfrastructureHealth")
        pom.set(layout.projectDirectory.file(".teamcity/pom.xml"))
        generatedConfigurationDirectory.set(
            layout.projectDirectory.dir(".teamcity/target/generated-configs"),
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
        "verifyUnitTestingDistribution",
        "verifyVerificationPlatformDistribution",
    )
}

val cleanTemporaryArtifacts =
    tasks.register<Delete>("cleanTemporaryArtifacts") {
        group = "build"
        description = "Deletes repository-owned temporary and generated tooling artifacts."
        delete(
            layout.projectDirectory.dir("tmp"),
            layout.projectDirectory.dir("repo/figma-documentation-sync/tools/dist"),
        )
    }

tasks.named("clean") {
    dependsOn(cleanTemporaryArtifacts)
}

val reusableBuildChecks =
    listOf(
        "figma-documentation-sync",
        "gradle-plugins",
        "unit-testing",
        "verification-platform",
        "water-my-plants-project-config",
    ).map { buildName ->
        gradle.includedBuild(buildName).task(":check")
    }

tasks.named("check") {
    dependsOn(reusableBuildChecks)
}
