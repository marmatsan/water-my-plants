package com.marmatsan.figmaDocumentationSync.data.ci.configuration

/** Creates the CI adapter selected by the project configuration. */
object CiConfigurationProviderFactory {
    fun create(
        providerClassName: String,
    ): CiConfigurationProvider {
        require(providerClassName.isNotBlank()) {
            "figmaDocumentationSync.ciConfigurationProviderClassName must not be blank"
        }

        val providerClass =
            runCatching {
                Thread.currentThread().contextClassLoader.loadClass(providerClassName)
            }.getOrElse { error ->
                throw IllegalArgumentException(
                    "Could not load CI configuration provider '$providerClassName'. " +
                        "Apply a project-config plugin that places the provider on the plugin classpath.",
                    error,
                )
            }

        require(CiConfigurationProvider::class.java.isAssignableFrom(providerClass)) {
            "CI configuration provider '$providerClassName' must implement " +
                CiConfigurationProvider::class.java.name
        }

        return runCatching {
            providerClass.getDeclaredConstructor().newInstance() as CiConfigurationProvider
        }.getOrElse { error ->
            throw IllegalArgumentException(
                "CI configuration provider '$providerClassName' must expose a public no-argument constructor.",
                error,
            )
        }
    }
}
