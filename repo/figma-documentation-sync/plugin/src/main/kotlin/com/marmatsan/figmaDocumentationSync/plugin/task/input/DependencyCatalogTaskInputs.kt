package com.marmatsan.figmaDocumentationSync.plugin.task.input

import com.marmatsan.figmaDocumentationSync.data.json.catalog.DependencyCatalogTreesJson
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.io.File

/** Mutually exclusive Gradle inputs for the primary dependency catalog source. */
interface DependencyCatalogTaskInputs {
    /** Legacy no-argument provider class retained during product migration. */
    @get:Input
    @get:Optional
    val dependencyCatalogProviderClassName: Property<String>

    /** Catalog trees materialized by a reusable configuration adapter. */
    @get:Input
    @get:Optional
    val dependencyCatalogTreesJson: Property<String>
}

/** Resolves exactly one configured primary catalog representation. */
fun DependencyCatalogTaskInputs.resolveDependencyCatalogTreeSource(
    projectRootDirectory: File,
    conventionPluginIncludedBuilds: List<IncludedBuildSource>
): ProjectCatalogTreeSource {
    val providerClassName = dependencyCatalogProviderClassName.orNull?.takeIf(String::isNotBlank)
    val treesJson = dependencyCatalogTreesJson.orNull?.takeIf(String::isNotBlank)
    if ((providerClassName == null) == (treesJson == null)) {
        throw GradleException(
            "Configure exactly one of figmaDocumentationSync.dependencyCatalogTreesJson or " +
                "figmaDocumentationSync.dependencyCatalogProviderClassName."
        )
    }

    return if (treesJson != null) {
        ProjectCatalogTreeSource.PreconfiguredVersionAliases(
            trees = DependencyCatalogTreesJson.decode(treesJson)
        )
    } else {
        ProjectCatalogTreeSource.DependenciesDslVersionAliases(
            rootDirPath = projectRootDirectory.absolutePath,
            providerClassName = requireNotNull(providerClassName),
            conventionPluginIncludedBuilds = conventionPluginIncludedBuilds
        )
    }
}
