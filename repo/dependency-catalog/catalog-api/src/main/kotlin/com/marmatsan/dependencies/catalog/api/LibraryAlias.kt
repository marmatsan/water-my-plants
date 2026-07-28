package com.marmatsan.dependencies.catalog.api

/**
 * Builds the stable Gradle version-catalog alias for a Maven coordinate.
 *
 * The alias starts with [libraryGroup]. When [artifact] starts with the longest suffix already
 * represented by that group, the repeated prefix is removed. Hyphens in the remaining artifact
 * segment become dots.
 *
 * Examples:
 *
 * - `androidx.compose` + `compose-bom` becomes `androidx.compose.bom`.
 * - `androidx.activity` + `activity-compose` becomes `androidx.activity.compose`.
 * - `org.junit.jupiter` + `junit-jupiter-api` becomes `org.junit.jupiter.api`.
 * - `com.google.protobuf` + `protoc` becomes `com.google.protobuf.protoc`.
 *
 * This function is the canonical alias policy shared by catalog producers and consumers. Keeping
 * it in the API module prevents adapters from reimplementing catalog identity rules.
 *
 * @param libraryGroup Maven group identifier.
 * @param artifact Maven artifact identifier.
 * @return Stable version-catalog alias for the coordinate.
 */
fun libraryAlias(
    libraryGroup: String,
    artifact: String,
): String {
    val groupSegments = libraryGroup.split(".")
    var groupSuffix = ""
    var artifactAliasSegment: String? = null

    for (index in groupSegments.lastIndex downTo 0) {
        groupSuffix =
            if (groupSuffix.isEmpty()) {
                groupSegments[index]
            } else {
                "${groupSegments[index]}-$groupSuffix"
            }

        artifactAliasSegment =
            when {
                artifact == groupSuffix -> ""
                artifact.startsWith("$groupSuffix-") -> artifact.removePrefix("$groupSuffix-")
                else -> null
            }

        if (artifactAliasSegment != null) {
            break
        }
    }

    val normalizedArtifactAliasSegment =
        (artifactAliasSegment ?: artifact).replace(
            "-",
            ".",
        )

    return if (artifactAliasSegment?.isEmpty() == true) {
        libraryGroup
    } else {
        "$libraryGroup.$normalizedArtifactAliasSegment"
    }
}
