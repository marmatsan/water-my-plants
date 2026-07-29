@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import java.util.Properties

plugins {
    base
    `kotlin-dsl` apply false
}

val versions: Properties =
    Properties().apply {
        file("versions.properties").inputStream().use(::load)
    }
val publicationVersion: String =
    providers.gradleProperty("gradlePluginsVersion").getOrElse(
        versions.getProperty("gradlePluginsVersion"),
    )
val stagingPublicationRepository: String =
    providers.gradleProperty("gradlePluginsPublicationRepository").orNull
        ?: layout.buildDirectory
            .dir("publication-repository")
            .get()
            .asFile.absolutePath
val dependencyCatalogSourceBuild: String? =
    providers.gradleProperty("dependencyCatalogSourceBuild").orNull
val dependencyCatalogPublicationRepository: String? =
    providers.gradleProperty("dependencyCatalogPublicationRepository").orNull
        ?: dependencyCatalogSourceBuild?.let {
            layout.buildDirectory
                .dir("dependency-catalog-publication-repository")
                .get()
                .asFile.absolutePath
        }

allprojects {
    group = "com.marmatsan.gradle-plugins"
    version = publicationVersion
}

subprojects {
    pluginManager.withPlugin("java") {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }

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
        ":android:check",
        ":bdd-test:check",
        ":compose:check",
        ":dependencies:check",
        ":dokka-documentation:check",
        ":protobuf:check",
        ":unit-test:check",
    )
}

tasks.register("publishPortablePublicationToStagingRepository") {
    group = "publishing"
    description = "Publishes every convention plugin and its shared implementation library."
    dependsOn(
        ":android:publishAllPublicationsToStagingRepository",
        ":bdd-test:publishAllPublicationsToStagingRepository",
        ":compose:publishAllPublicationsToStagingRepository",
        ":dependencies:publishAllPublicationsToStagingRepository",
        ":dokka-documentation:publishAllPublicationsToStagingRepository",
        ":protobuf:publishAllPublicationsToStagingRepository",
        ":unit-test:publishAllPublicationsToStagingRepository",
    )
}

tasks.register<Exec>("verifyStagedPublication") {
    group = "verification"
    description = "Resolves staged convention plugin markers from a source-independent consumer."
    dependsOn("publishPortablePublicationToStagingRepository")
    if (dependencyCatalogSourceBuild != null) {
        dependsOn(
            gradle
                .includedBuild("dependency-catalog")
                .task(":catalog-api:publishAllPublicationsToStagingRepository"),
        )
    }

    requireNotNull(dependencyCatalogPublicationRepository) {
        "verifyStagedPublication requires dependencyCatalogPublicationRepository or dependencyCatalogSourceBuild"
    }

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
        "verifyPluginConsumption",
        "-PgradlePluginsVersion=$publicationVersion",
        "-PgradlePluginsPublicationRepository=$stagingPublicationRepository",
        "-PdependencyCatalogPublicationRepository=$dependencyCatalogPublicationRepository",
        "-PunitTestDslVersion=${versions.getProperty("unitTestDslLibraryVersion")}",
        "-PkotlinVersion=${versions.getProperty("kotlinVersion")}",
        "-PkotestVersion=${versions.getProperty("kotestLibraryVersion")}",
        "-PmockkVersion=${versions.getProperty("mockkLibraryVersion")}",
        "--stacktrace",
    )
}
