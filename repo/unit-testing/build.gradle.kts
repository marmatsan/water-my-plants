@file:Suppress("AvoidDuplicateDependencies")

import java.util.Properties

plugins {
    base
}

val versions: Properties =
    Properties().apply {
        file("versions.properties").inputStream().use(::load)
    }
val publicationVersion: String =
    providers.gradleProperty("unitTestDslVersion").getOrElse(
        versions.getProperty("unitTestDslLibraryVersion")
    )
val stagingPublicationRepository: String =
    providers.gradleProperty("unitTestingPublicationRepository").orNull
        ?: layout.buildDirectory
            .dir("publication-repository")
            .get()
            .asFile.absolutePath

allprojects {
    group = "com.marmatsan.repo"
    version = publicationVersion
}

tasks.named("check") {
    dependsOn(":unit-test-dsl:check")
}

tasks.register("dokkaGenerate") {
    group = "documentation"
    description = "Generates the typed unit-test DSL API reference."
    dependsOn(":unit-test-dsl:dokkaGenerate")
}

tasks.register("publishPortablePublicationToStagingRepository") {
    group = "publishing"
    description = "Publishes the reusable unit-test DSL to the staging repository."
    dependsOn(":unit-test-dsl:publishAllPublicationsToStagingRepository")
}

tasks.register<Exec>("verifyStagedPublication") {
    group = "verification"
    description = "Compiles a standalone Kotlin consumer against the staged unit-test DSL."
    dependsOn("publishPortablePublicationToStagingRepository")

    val sampleDirectory = layout.projectDirectory.dir("samples/standalone-consumer")
    val wrapper =
        layout.projectDirectory.file(
            if (System.getProperty("os.name").startsWith(
                    "Windows",
                    ignoreCase = true
                )
            ) {
                "../../gradlew.bat"
            } else {
                "../../gradlew"
            }
        )

    workingDir(sampleDirectory)
    commandLine(
        wrapper.asFile.absolutePath,
        "--no-daemon",
        "verifyDslConsumption",
        "-PunitTestDslVersion=$publicationVersion",
        "-PunitTestingPublicationRepository=$stagingPublicationRepository",
        "-PkotlinVersion=${versions.getProperty("kotlinVersion")}",
        "--stacktrace"
    )
}
