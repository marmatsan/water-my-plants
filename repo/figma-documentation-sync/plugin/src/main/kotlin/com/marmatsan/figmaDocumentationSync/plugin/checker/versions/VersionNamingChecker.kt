package com.marmatsan.figmaDocumentationSync.plugin.checker.versions

import com.marmatsan.figmaDocumentationSync.domain.model.versions.RepositoryVersionSection
import com.marmatsan.figmaDocumentationSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDocumentationSync.domain.port.versions.VersionsFileSource
import me.tatarka.inject.annotations.Inject

/**
 * Verifies the version naming contract rendered by the Figma versions section.
 */
@Inject
internal class VersionNamingChecker(
    private val repositoryVersionsPort: RepositoryVersionsPort,
) {
    /** Validates the ordered version sections and suffix rules described by [request]. */
    fun check(
        request: VersionNamingCheckRequest,
    ): VersionNamingCheckResult {
        val sections =
            repositoryVersionsPort.readVersionSections(
                source =
                    VersionsFileSource(
                        path = request.versionsFile.absolutePath,
                    ),
            )
        val violations = mutableListOf<VersionNamingViolation>()

        violations +=
            checkSectionOrder(
                sections = sections,
            )
        violations +=
            checkMainProjectDependencies(
                sections = sections,
            )
        violations +=
            checkSuffixes(
                sections = sections,
                sectionName = LIBRARIES_SECTION,
                suffix = LIBRARY_VERSION_SUFFIX,
            )
        violations +=
            checkSuffixes(
                sections = sections,
                sectionName = PLUGINS_SECTION,
                suffix = PLUGIN_VERSION_SUFFIX,
            )

        return VersionNamingCheckResult(
            violations = violations,
        )
    }

    private fun checkSectionOrder(
        sections: List<RepositoryVersionSection>,
    ): List<VersionNamingViolation> {
        val actualSectionNames =
            sections.map(
                transform = RepositoryVersionSection::name,
            )
        return if (actualSectionNames == EXPECTED_SECTION_NAMES) {
            emptyList()
        } else {
            listOf(
                VersionNamingViolation(
                    message =
                        "Expected version sections in order: ${EXPECTED_SECTION_NAMES.joinToString()}. " +
                            "Found: ${actualSectionNames.joinToString()}.",
                ),
            )
        }
    }

    private fun checkMainProjectDependencies(
        sections: List<RepositoryVersionSection>,
    ): List<VersionNamingViolation> {
        val keys =
            sections
                .firstOrNull { section -> section.name == MAIN_PROJECT_DEPENDENCIES_SECTION }
                ?.versions
                ?.keys
                .orEmpty()
                .toSet()

        return if (keys == MAIN_PROJECT_DEPENDENCIES_KEYS.toSet()) {
            emptyList()
        } else {
            listOf(
                VersionNamingViolation(
                    message =
                        "$MAIN_PROJECT_DEPENDENCIES_SECTION must declare only " +
                            "${MAIN_PROJECT_DEPENDENCIES_KEYS.joinToString()}." +
                            " Found: ${keys.sorted().joinToString()}.",
                ),
            )
        }
    }

    private fun checkSuffixes(
        sections: List<RepositoryVersionSection>,
        sectionName: String,
        suffix: String,
    ): List<VersionNamingViolation> =
        sections
            .firstOrNull { section -> section.name == sectionName }
            ?.versions
            ?.keys
            .orEmpty()
            .filterNot { key -> key.endsWith(suffix) }
            .map { key ->
                VersionNamingViolation(
                    message = "$sectionName version key '$key' must end with '$suffix'.",
                )
            }

    private companion object {
        const val MAIN_PROJECT_DEPENDENCIES_SECTION = "Main project dependencies"
        const val LIBRARIES_SECTION = "Libraries"
        const val PLUGINS_SECTION = "Plugins"
        const val LIBRARY_VERSION_SUFFIX = "LibraryVersion"
        const val PLUGIN_VERSION_SUFFIX = "PluginVersion"

        val EXPECTED_SECTION_NAMES =
            listOf(
                MAIN_PROJECT_DEPENDENCIES_SECTION,
                LIBRARIES_SECTION,
                PLUGINS_SECTION,
            )
        val MAIN_PROJECT_DEPENDENCIES_KEYS =
            listOf(
                "androidGradlePluginVersion",
                "kotlinVersion",
            )
    }
}
