package com.marmatsan.figmaDocumentationSync.data.dependencies.catalog

import com.marmatsan.figmaDocumentationSync.domain.port.catalog.DependencyDslCatalogProvider

/** Creates the project adapter selected by the Gradle project configuration. */
object DependencyDslCatalogProviderFactory {
    /** Instantiates the no-argument catalog provider identified by [providerClassName]. */
    fun create(
        providerClassName: String
    ): DependencyDslCatalogProvider {
        require(providerClassName.isNotBlank()) {
            "figmaDocumentationSync.dependencyCatalogProviderClassName must not be blank"
        }

        val providerClass =
            runCatching {
                Thread.currentThread().contextClassLoader.loadClass(providerClassName)
            }.getOrElse { error ->
                throw IllegalArgumentException(
                    "Could not load dependency catalog provider '$providerClassName'. " +
                        "Apply a project-config plugin that places the provider on the plugin classpath.",
                    error
                )
            }

        require(DependencyDslCatalogProvider::class.java.isAssignableFrom(providerClass)) {
            "Dependency catalog provider '$providerClassName' must implement " +
                DependencyDslCatalogProvider::class.java.name
        }

        return runCatching {
            providerClass.getDeclaredConstructor().newInstance() as DependencyDslCatalogProvider
        }.getOrElse { error ->
            throw IllegalArgumentException(
                "Dependency catalog provider '$providerClassName' must expose a public no-argument constructor.",
                error
            )
        }
    }
}
