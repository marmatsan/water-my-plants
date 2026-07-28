package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import java.io.File

/** Builds the Water My Plants dependency catalog from resolved or symbolic versions. */
object WaterMyPlantsCatalog {
    /** Resolves version values from the repository rooted at [rootDir]. */
    fun resolved(
        rootDir: File,
    ): DependencyCatalogTrees =
        catalogTrees(
            versions = Versions.load(rootDir),
        )

    /** Builds the same catalog with property names as version aliases for documentation scanning. */
    fun withVersionAliases(): DependencyCatalogTrees =
        catalogTrees(
            versions = versionAliases,
        )

    private fun catalogTrees(
        versions: Versions,
    ): DependencyCatalogTrees =
        DependencyCatalogTrees(
            libraries =
                libraryTrees(
                    versions = versions,
                ),
            plugins =
                pluginTrees(
                    versions = versions,
                ),
        )
}

private val versionAliases =
    Versions(
        activityComposeLibraryVersion = Versions::activityComposeLibraryVersion.name,
        androidCoroutinesLibraryVersion = Versions::androidCoroutinesLibraryVersion.name,
        androidGradlePluginVersion = Versions::androidGradlePluginVersion.name,
        composeBomLibraryVersion = Versions::composeBomLibraryVersion.name,
        coreKtxLibraryVersion = Versions::coreKtxLibraryVersion.name,
        cucumberLibraryVersion = Versions::cucumberLibraryVersion.name,
        dokkaPluginVersion = Versions::dokkaPluginVersion.name,
        figmaCodeConnectLibraryVersion = Versions::figmaCodeConnectLibraryVersion.name,
        figmaCodeConnectPluginVersion = Versions::figmaCodeConnectPluginVersion.name,
        gradleConventionPluginVersion = Versions::gradleConventionPluginVersion.name,
        junit5PluginVersion = Versions::junit5PluginVersion.name,
        kotestLibraryVersion = Versions::kotestLibraryVersion.name,
        kotlinInjectLibraryVersion = Versions::kotlinInjectLibraryVersion.name,
        kotlinVersion = Versions::kotlinVersion.name,
        kspPluginVersion = Versions::kspPluginVersion.name,
        lifecycleLibraryVersion = Versions::lifecycleLibraryVersion.name,
        mockkLibraryVersion = Versions::mockkLibraryVersion.name,
        navigationComposeLibraryVersion = Versions::navigationComposeLibraryVersion.name,
        protobufLibraryVersion = Versions::protobufLibraryVersion.name,
        protobufPluginVersion = Versions::protobufPluginVersion.name,
    )
