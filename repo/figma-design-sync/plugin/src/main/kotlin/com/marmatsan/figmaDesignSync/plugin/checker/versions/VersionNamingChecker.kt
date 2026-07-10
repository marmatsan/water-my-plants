package com.marmatsan.figmaDesignSync.plugin.checker.versions

import com.marmatsan.figmaDesignSync.domain.model.versions.RepositoryVersionSection
import com.marmatsan.figmaDesignSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDesignSync.domain.port.versions.VersionsFileSource
import me.tatarka.inject.annotations.Inject

/**
 * Verifies the version naming contract rendered by the Figma versions section.
 */
@Inject
internal class VersionNamingChecker(
    private val repositoryVersionsPort: RepositoryVersionsPort
) {
    fun check(request: VersionNamingCheckRequest): VersionNamingCheckResult {
        val sections = repositoryVersionsPort.readVersionSections(
            VersionsFileSource(request.versionsFile.absolutePath)
        )
        val violations = mutableListOf<VersionNamingViolation>()

        violations += checkSectionOrder(sections)
        violations += checkMainProjectDependencies(sections)
        violations += checkSuffixes(
            sections = sections,
            sectionName = LIBRARIES_SECTION,
            suffix = LIBRARY_VERSION_SUFFIX
        )
        violations += checkSuffixes(
            sections = sections,
            sectionName = PLUGINS_SECTION,
            suffix = PLUGIN_VERSION_SUFFIX
        )

        return VersionNamingCheckResult(violations = violations)
    }

    private fun checkSectionOrder(sections: List<RepositoryVersionSection>): List<VersionNamingViolation> {
        val actualSectionNames = sections.map(RepositoryVersionSection::name)
        return if (actualSectionNames == EXPECTED_SECTION_NAMES) {
            emptyList()
        } else {
            listOf(
                VersionNamingViolation(
                    "Expected version sections in order: ${EXPECTED_SECTION_NAMES.joinToString()}. " +
                        "Found: ${actualSectionNames.joinToString()}."
                )
            )
        }
    }

    private fun checkMainProjectDependencies(
        sections: List<RepositoryVersionSection>
    ): List<VersionNamingViolation> {
        val keys = sections
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
                    "$MAIN_PROJECT_DEPENDENCIES_SECTION must declare only " +
                        "${MAIN_PROJECT_DEPENDENCIES_KEYS.joinToString()}." +
                        " Found: ${keys.sorted().joinToString()}."
                )
            )
        }
    }

    private fun checkSuffixes(
        sections: List<RepositoryVersionSection>,
        sectionName: String,
        suffix: String
    ): List<VersionNamingViolation> =
        sections
            .firstOrNull { section -> section.name == sectionName }
            ?.versions
            ?.keys
            .orEmpty()
            .filterNot { key -> key.endsWith(suffix) }
            .map { key ->
                VersionNamingViolation(
                    "$sectionName version key '$key' must end with '$suffix'."
                )
            }

    private companion object {
        const val MAIN_PROJECT_DEPENDENCIES_SECTION = "Main project dependencies"
        const val LIBRARIES_SECTION = "Libraries"
        const val PLUGINS_SECTION = "Plugins"
        const val LIBRARY_VERSION_SUFFIX = "LibraryVersion"
        const val PLUGIN_VERSION_SUFFIX = "PluginVersion"

        val EXPECTED_SECTION_NAMES = listOf(
            MAIN_PROJECT_DEPENDENCIES_SECTION,
            LIBRARIES_SECTION,
            PLUGINS_SECTION
        )
        val MAIN_PROJECT_DEPENDENCIES_KEYS = listOf(
            "androidGradlePlugin",
            "kotlinVersion"
        )
    }
}
