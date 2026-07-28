@file:Suppress("AvoidDuplicateDependencies")

import java.util.Properties

plugins {
    base
}

val versions =
    Properties().apply {
        file("versions.properties").inputStream().use(::load)
    }
val publicationVersion =
    providers.gradleProperty("dependencyCatalogVersion").getOrElse(
        versions.getProperty("dependencyCatalogVersion"),
    )
val stagingPublicationRepository =
    providers.gradleProperty("dependencyCatalogPublicationRepository").orNull
        ?: layout.buildDirectory
            .dir("publication-repository")
            .get()
            .asFile.absolutePath

allprojects {
    group = "com.marmatsan.repo"
    version = publicationVersion
}

val checkDependencyCatalogArchitecture =
    tasks.register("checkDependencyCatalogArchitecture") {
        group = "verification"
        description = "Verifies the portable catalog API, core, and Gradle adapter."
        dependsOn(
            ":catalog-api:check",
            ":catalog-core:check",
            ":catalog-gradle-plugin:check",
        )
    }

tasks.named("check") {
    dependsOn(checkDependencyCatalogArchitecture)
}

tasks.register("publishPortablePublicationToStagingRepository") {
    group = "publishing"
    description = "Publishes the reusable catalog API, core, and Gradle plugin to the staging repository."
    dependsOn(
        ":catalog-api:publishAllPublicationsToStagingRepository",
        ":catalog-core:publishAllPublicationsToStagingRepository",
        ":catalog-gradle-plugin:publishAllPublicationsToStagingRepository",
    )
}

tasks.register<Exec>("verifyStagedPublication") {
    group = "verification"
    description = "Applies the staged catalog plugin from a source-independent consumer."
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
        "verifyCatalogs",
        "-PdependencyCatalogVersion=$publicationVersion",
        "-PdependencyCatalogPublicationRepository=$stagingPublicationRepository",
        "--stacktrace",
    )
}
