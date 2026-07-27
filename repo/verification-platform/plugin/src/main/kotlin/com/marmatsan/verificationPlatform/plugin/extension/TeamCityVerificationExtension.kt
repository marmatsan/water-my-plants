package com.marmatsan.verificationPlatform.plugin.extension

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/** Configures the optional TeamCity adapter without embedding consuming-repository identities. */
class TeamCityVerificationExtension internal constructor(
    project: Project,
) {
    /** TeamCity build type queued by the optional infrastructure-health task. */
    val infrastructureHealthBuildTypeId: Property<String> = project.objects.property(String::class.java)

    /** Maven project that generates the consuming repository's TeamCity configuration. */
    val pom: RegularFileProperty = project.objects.fileProperty()

    /** Directory receiving generated TeamCity configuration. */
    val generatedConfigurationDirectory: DirectoryProperty = project.objects.directoryProperty()

    /** TeamCity build type id of the generated pipeline head. */
    val pipelineBuildTypeId: Property<String> = project.objects.property(String::class.java)

    /** TeamCity build type id of the authoritative composite gate. */
    val gateBuildTypeId: Property<String> = project.objects.property(String::class.java)

    /** GitHub status name published by the authoritative CI gate. */
    val authoritativeStatusName: Property<String> = project.objects.property(String::class.java)
}
