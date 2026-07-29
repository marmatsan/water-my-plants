package com.marmatsan.figmaDocumentationSync.plugin.generator

import java.io.File

/** Reconstructs included-build sources from configuration-cache-safe task inputs. */
internal class FigmaDesignModelIncludedBuildSourceFactory {
    /** Creates one source per aligned input index and rejects incomplete task configuration. */
    fun create(
        settingsFilePaths: List<String>,
        rootDirectoryPaths: List<String>,
        modelNames: List<String>,
        modulePathPrefixes: List<String>,
        publishesCatalogs: List<Boolean>,
        publishesConventionPlugins: List<Boolean>
    ): List<FigmaDesignModelIncludedBuildSource> {
        val inputSizes =
            mapOf(
                "settingsFilePaths" to settingsFilePaths.size,
                "rootDirectoryPaths" to rootDirectoryPaths.size,
                "modelNames" to modelNames.size,
                "modulePathPrefixes" to modulePathPrefixes.size,
                "publishesCatalogs" to publishesCatalogs.size,
                "publishesConventionPlugins" to publishesConventionPlugins.size
            )
        require(inputSizes.values.distinct().size == 1) {
            "Included-build task inputs must have matching sizes: " +
                inputSizes.entries.joinToString { (name, size) -> "$name=$size" }
        }

        return settingsFilePaths.indices.map { index ->
            FigmaDesignModelIncludedBuildSource(
                modelName = modelNames[index],
                settingsFile = File(settingsFilePaths[index]),
                rootDirectory = File(rootDirectoryPaths[index]),
                modulePathPrefix = modulePathPrefixes[index],
                publishesCatalogs = publishesCatalogs[index],
                publishesConventionPlugins = publishesConventionPlugins[index]
            )
        }
    }
}
