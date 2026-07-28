@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import java.util.Properties

plugins {
    base
    alias(plugins.plugins.org.jetbrains.kotlin.jvm) apply false
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization) apply false
    alias(plugins.plugins.org.jetbrains.dokka) apply false
}

val versions =
    Properties().apply {
        file("versions.properties").inputStream().use(::load)
    }
val publicationVersion =
    providers.gradleProperty("verificationPlatformVersion").getOrElse(
        versions.getProperty("verificationPlatformVersion"),
    )
val stagingPublicationRepository =
    providers.gradleProperty("verificationPlatformPublicationRepository").orNull
        ?: layout.buildDirectory
            .dir("publication-repository")
            .get()
            .asFile.absolutePath

allprojects {
    group = "com.marmatsan.verification-platform"
    version = publicationVersion
}

subprojects {
    pluginManager.withPlugin("maven-publish") {
        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "staging"
                    url = uri(stagingPublicationRepository)
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(
        ":domain:check",
        ":data:check",
        ":plugin:check",
    )
}

tasks.register("dokkaGenerate") {
    group = "documentation"
    description = "Generates the CI domain, data, and plugin API documentation."
    dependsOn(
        ":domain:dokkaGenerate",
        ":data:dokkaGenerate",
        ":plugin:dokkaGenerate",
    )
}

tasks.register("publishPortablePublicationToStagingRepository") {
    group = "publishing"
    description = "Publishes verification domain, adapters, plugin, and plugin marker."
    dependsOn(
        ":domain:publishAllPublicationsToStagingRepository",
        ":data:publishAllPublicationsToStagingRepository",
        ":plugin:publishAllPublicationsToStagingRepository",
    )
}

tasks.register<Exec>("verifyStagedPublication") {
    group = "verification"
    description = "Applies the staged verification plugin from a source-independent consumer."
    dependsOn("publishPortablePublicationToStagingRepository")

    val sampleDirectory = layout.projectDirectory.dir("samples/standalone-consumer")
    val wrapper =
        layout.projectDirectory.file(
            if (System.getProperty("os.name").startsWith(
                    "Windows",
                    ignoreCase = true,
                )
            ) {
                "../../gradlew.bat"
            } else {
                "../../gradlew"
            },
        )

    workingDir(sampleDirectory)
    commandLine(
        wrapper.asFile.absolutePath,
        "--no-daemon",
        "verifyPluginApplication",
        "-PverificationPlatformVersion=$publicationVersion",
        "-PverificationPlatformPublicationRepository=$stagingPublicationRepository",
        "--stacktrace",
    )
}
