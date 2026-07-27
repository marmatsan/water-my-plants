package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

/** One convention-plugin configuration assignment of a library coordinate. */
data class LibraryConfigurationUsage(
    val pluginModule: String,
    val target: String,
) : Comparable<LibraryConfigurationUsage> {
    override fun compareTo(
        other: LibraryConfigurationUsage,
    ): Int =
        compareValuesBy(
            this,
            other,
            LibraryConfigurationUsage::pluginModule,
            LibraryConfigurationUsage::target,
        )
}
