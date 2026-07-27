package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

/**
 * One convention-plugin configuration assignment of a library coordinate.
 *
 * @property pluginModule convention-plugin module containing the assignment.
 * @property target dependency configuration receiving the coordinate.
 */
data class LibraryConfigurationUsage(
    val pluginModule: String,
    val target: String,
) : Comparable<LibraryConfigurationUsage> {
    /** Orders usages deterministically by module and configuration target. */
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
