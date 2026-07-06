package com.marmatsan.figmaDesignSync.plugin.generator

import com.marmatsan.figmaDesignSync.domain.port.gradle.IncludedBuildSource
import java.io.File

/**
 * Included-build input used by the design-model generator.
 *
 * [modelName] is the JSON key used by the Figma sync contract, while
 * [modulePathPrefix] is the Gradle path prefix used for modules and dependency
 * edges from that included build.
 */
internal data class FigmaDesignModelIncludedBuildSource(
    val modelName: String,
    val settingsFile: File,
    val rootDirectory: File,
    val modulePathPrefix: String,
    val publishesConventionPlugins: Boolean
) {
    fun toDomainSource(): IncludedBuildSource =
        IncludedBuildSource(
            settingsFilePath = settingsFile.absolutePath,
            rootDirPath = rootDirectory.absolutePath,
            modulePathPrefix = modulePathPrefix,
            publishesConventionPlugins = publishesConventionPlugins
        )
}
