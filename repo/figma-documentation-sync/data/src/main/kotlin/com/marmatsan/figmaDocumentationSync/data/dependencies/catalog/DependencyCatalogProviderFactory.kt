package com.marmatsan.figmaDocumentationSync.data.dependencies.catalog

/** Creates the project adapter selected by the Gradle project configuration. */
object DependencyCatalogProviderFactory {
    fun create(
        providerClassName: String
    ): DependencyCatalogProvider {
        require(providerClassName.isNotBlank()) {
            "figmaDocumentationSync.dependencyCatalogProviderClassName must not be blank"
        }

        val providerClass = runCatching {
            Thread.currentThread().contextClassLoader.loadClass(providerClassName)
        }.getOrElse { error ->
            throw IllegalArgumentException(
                "Could not load dependency catalog provider '$providerClassName'. " +
                    "Apply a project-config plugin that places the provider on the plugin classpath.",
                error
            )
        }

        require(DependencyCatalogProvider::class.java.isAssignableFrom(providerClass)) {
            "Dependency catalog provider '$providerClassName' must implement " +
                DependencyCatalogProvider::class.java.name
        }

        return runCatching {
            providerClass.getDeclaredConstructor().newInstance() as DependencyCatalogProvider
        }.getOrElse { error ->
            throw IllegalArgumentException(
                "Dependency catalog provider '$providerClassName' must expose a public no-argument constructor.",
                error
            )
        }
    }
}
