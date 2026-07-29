package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

/** Parses plugin ids and implementation kinds from a Kotlin Gradle build script. */
internal object GradlePluginDeclarationParser {
    /** Returns every plugin id declared by [content]. */
    fun pluginIds(
        content: String,
    ): Sequence<String> =
        sequenceOf(
            PLUGIN_NAME_REGEX,
            PLUGIN_ID_REGEX,
        ).flatMap { regex ->
            regex.findAll(
                input = content,
            )
        }.map { match -> match.groupValues[1] }

    /** Returns whether [content] implements at least one non-convention Gradle plugin. */
    fun hasRegularPluginImplementation(
        content: String,
    ): Boolean =
        IMPLEMENTATION_CLASS_REGEX
            .findAll(
                input = content,
            ).map { match -> match.groupValues[1] }
            .any { implementationClass -> !implementationClass.endsWith(CONVENTION_PLUGIN_SUFFIX) }

    /** Returns whether [content] implements at least one Gradle convention plugin. */
    fun hasConventionPluginImplementation(
        content: String,
    ): Boolean = CONVENTION_PLUGIN_IMPLEMENTATION_REGEX.containsMatchIn(content)

    private const val CONVENTION_PLUGIN_SUFFIX = "GradleConventionPlugin"
    private val PLUGIN_NAME_REGEX = Regex("val\\s+pluginName\\s*=\\s*\"([^\"]+)\"")
    private val PLUGIN_ID_REGEX = Regex("id\\s*=\\s*\"([^\"]+)\"")
    private val IMPLEMENTATION_CLASS_REGEX = Regex("implementationClass\\s*=\\s*\"([^\"]+)\"")
    private val CONVENTION_PLUGIN_IMPLEMENTATION_REGEX =
        Regex(
            "implementationClass\\s*=\\s*\"[^\"]*GradleConventionPlugin\"",
        )
}
