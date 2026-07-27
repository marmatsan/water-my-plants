package com.marmatsan.figmaDocumentationSync.plugin.generator

import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import java.io.File

/**
 * Included-build input used by the design-model generator.
 *
 * [modelName] is the JSON key used by the Figma sync contract, while
 * [modulePathPrefix] is the Gradle path prefix used for modules and dependency
 * edges from that included build.
 *
 * @property modelName stable design-model identity for the included build.
 * @property settingsFile settings script used to discover projects and catalogs.
 * @property rootDirectory root used to resolve included-build project paths.
 * @property modulePathPrefix prefix that makes module paths unique across builds.
 * @property publishesCatalogs whether dependency catalog trees are part of the production model.
 * @property publishesConventionPlugins whether the build contributes convention-plugin usage.
 */
internal data class FigmaDesignModelIncludedBuildSource(
    val modelName: String,
    val settingsFile: File,
    val rootDirectory: File,
    val modulePathPrefix: String,
    val publishesCatalogs: Boolean,
    val publishesConventionPlugins: Boolean,
) {
    /** Projects this plugin-layer input onto the domain-owned included-build contract. */
    fun toDomainSource(): IncludedBuildSource =
        IncludedBuildSource(
            settingsFilePath = settingsFile.absolutePath,
            rootDirPath = rootDirectory.absolutePath,
            modulePathPrefix = modulePathPrefix,
            publishesCatalogs = publishesCatalogs,
            publishesConventionPlugins = publishesConventionPlugins,
        )
}
