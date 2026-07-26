import com.marmatsan.verificationPlatform.plugin.VerificationPlatformExtension

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
    toolingPathPrefixes.set(
        listOf(
            "repo/figma-documentation-sync/",
        ),
    )
    buildInfrastructurePathPrefixes.set(
        listOf(
            "repo/dependency-catalog/",
            "repo/gradle-plugins/",
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
            "repo/figma-documentation-sync/domain/",
            "repo/figma-documentation-sync/data/",
            "repo/figma-documentation-sync/plugin/",
            "repo/figma-documentation-sync/teamcity-adapter/",
            "repo/figma-documentation-sync/tools/",
            "repo/figma-documentation-sync/samples/standalone-consumer/",
        ),
    )
    portableDistributionPaths.set(
        listOf(
            "repo/dependency-catalog/build.gradle.kts",
            "repo/dependency-catalog/settings.gradle.kts",
            "repo/figma-documentation-sync/build.gradle.kts",
            "repo/figma-documentation-sync/gradle.properties",
            "repo/figma-documentation-sync/settings.gradle.kts",
            "repo/figma-documentation-sync/versions.properties",
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

    listOf(
        "repo/dependency-catalog",
        "repo/gradle-plugins",
        "repo/figma-documentation-sync",
        "repo/verification-platform",
        "repo/water-my-plants-project-config",
    ).forEach(::versionedBuild)

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
        "repo/verification-platform",
        "repo/dependency-catalog",
        "repo/figma-documentation-sync",
        "repo/gradle-plugins",
        "repo/water-my-plants-project-config",
    )

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
    infrastructureHealthBuildTypeId.set("WaterMyPlants_WaterMyPlantsInfrastructureHealth")
    teamCityPom.set(layout.projectDirectory.file(".teamcity/pom.xml"))
    teamCityGeneratedConfigurationDirectory.set(
        layout.projectDirectory.dir(".teamcity/target/generated-configs"),
    )
    teamCityPipelineBuildTypeId.set("WaterMyPlantsCi")
    teamCityGateBuildTypeId.set("WaterMyPlantsCiGate")
    authoritativeStatusName.set("TeamCity CI")
}

tasks.register("verifyPortableDistribution") {
    group = "verification"
    description = "Verifies every staged reusable artifact through source-independent consumers."
    dependsOn(
        "verifyDependencyCatalogDistribution",
        "verifyFigmaDocumentationSyncDistribution",
    )
}

tasks.named("check") {
    dependsOn(gradle.includedBuild("verification-platform").task(":check"))
}
