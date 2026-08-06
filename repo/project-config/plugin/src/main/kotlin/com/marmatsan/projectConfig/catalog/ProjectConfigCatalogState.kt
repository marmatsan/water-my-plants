package com.marmatsan.projectConfig.catalog

import com.marmatsan.dependencies.catalog.api.DependencyCatalogProvider
import org.gradle.api.GradleException
import org.gradle.api.invocation.Gradle

/** Shares the catalog provider between the Settings and Project composition phases. */
internal object ProjectConfigCatalogState {
    private const val PROVIDER_KEY = "com.marmatsan.projectConfig.catalogProvider"

    /** Stores the provider built from the consumer's Settings DSL. */
    fun store(
        gradle: Gradle,
        provider: DependencyCatalogProvider
    ) {
        gradle.extensions.extraProperties.set(
            PROVIDER_KEY,
            provider
        )
    }

    /** Returns the provider captured during Settings evaluation. */
    fun require(
        gradle: Gradle
    ): DependencyCatalogProvider {
        val extras = gradle.extensions.extraProperties
        if (!extras.has(PROVIDER_KEY)) {
            throw GradleException(
                "Apply com.marmatsan.projectConfig.settings and declare projectConfig before " +
                    "applying com.marmatsan.projectConfig to the root project."
            )
        }
        return extras.get(PROVIDER_KEY) as DependencyCatalogProvider
    }
}
