package com.marmatsan.figmaDocumentationSync.plugin.checker.versions

import com.marmatsan.figmaDocumentationSync.domain.model.versions.RepositoryVersionSection
import com.marmatsan.figmaDocumentationSync.domain.port.versions.RepositoryVersionsPort
import com.marmatsan.figmaDocumentationSync.domain.port.versions.VersionsFileSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File

internal class VersionNamingCheckerTest :
    FunSpec(
        {

            test("check accepts the expected version section and suffix contract") {
                // GIVEN
                val checker =
                    VersionNamingChecker(
                        repositoryVersionsPort =
                            FakeRepositoryVersionsPort(
                                sections =
                                    listOf(
                                        RepositoryVersionSection(
                                            name = "Main project dependencies",
                                            versions =
                                                mapOf(
                                                    "androidGradlePluginVersion" to "9.2.1",
                                                    "kotlinVersion" to "2.4.0",
                                                ),
                                        ),
                                        RepositoryVersionSection(
                                            name = "Libraries",
                                            versions =
                                                mapOf(
                                                    "activityComposeLibraryVersion" to "1.13.0",
                                                    "protobufLibraryVersion" to "4.35.1",
                                                ),
                                        ),
                                        RepositoryVersionSection(
                                            name = "Plugins",
                                            versions =
                                                mapOf(
                                                    "dokkaPluginVersion" to "2.2.0",
                                                    "kspPluginVersion" to "2.3.9",
                                                ),
                                        ),
                                    ),
                            ),
                    )

                // WHEN
                val result =
                    checker.check(
                        VersionNamingCheckRequest(
                            versionsFile = File("versions.properties"),
                        ),
                    )

                // THEN
                result.isSuccessful shouldBe true
                result.violations shouldBe emptyList()
            }

            test("check reports invalid version sections and suffixes") {
                // GIVEN
                val checker =
                    VersionNamingChecker(
                        repositoryVersionsPort =
                            FakeRepositoryVersionsPort(
                                sections =
                                    listOf(
                                        RepositoryVersionSection(
                                            name = "Libraries",
                                            versions = mapOf("activityComposeVersion" to "1.13.0"),
                                        ),
                                        RepositoryVersionSection(
                                            name = "Main project dependencies",
                                            versions =
                                                mapOf(
                                                    "androidGradlePluginVersion" to "9.2.1",
                                                    "kotlinVersion" to "2.4.0",
                                                    "kspPluginVersion" to "2.3.9",
                                                ),
                                        ),
                                        RepositoryVersionSection(
                                            name = "Plugins",
                                            versions = mapOf("dokkaVersion" to "2.2.0"),
                                        ),
                                    ),
                            ),
                    )

                // WHEN
                val result =
                    checker.check(
                        VersionNamingCheckRequest(
                            versionsFile = File("versions.properties"),
                        ),
                    )

                // THEN
                result.violations.map(
                    transform = VersionNamingViolation::message,
                ) shouldBe
                    listOf(
                        "Expected version sections in order: Main project dependencies, Libraries, Plugins. " +
                            "Found: Libraries, Main project dependencies, Plugins.",
                        "Main project dependencies must declare only androidGradlePluginVersion, kotlinVersion. " +
                            "Found: androidGradlePluginVersion, kotlinVersion, kspPluginVersion.",
                        "Libraries version key 'activityComposeVersion' must end with 'LibraryVersion'.",
                        "Plugins version key 'dokkaVersion' must end with 'PluginVersion'.",
                    )
            }
        },
    )

private class FakeRepositoryVersionsPort(
    private val sections: List<RepositoryVersionSection>,
) : RepositoryVersionsPort {
    override fun readVersions(
        source: VersionsFileSource,
    ): Map<String, String> =
        sections
            .flatMap { section -> section.versions.entries }
            .associate { entry -> entry.key to entry.value }

    override fun readVersionSections(
        source: VersionsFileSource,
    ): List<RepositoryVersionSection> =
        sections
}
