plugins {
    base
    `kotlin-dsl` apply false
}

tasks.named("check") {
    dependsOn(
        ":data:check",
        ":domain:check",
        ":plugin:check",
        ":teamcity-adapter:check",
    )
}

tasks.register("dokkaGenerate") {
    group = "documentation"
    description = "Generates all portable Figma documentation sync API references."
    dependsOn(
        ":data:dokkaGenerate",
        ":domain:dokkaGenerate",
        ":plugin:dokkaGenerate",
        ":teamcity-adapter:dokkaGenerate",
    )
}

@DisableCachingByDefault(
    because = "The verification task has no reusable output artifact",
)
abstract class VerifyPublicationVersionAlignmentTask : DefaultTask() {
    @get:Input
    abstract val mavenVersion: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val packageJson: RegularFileProperty

    @TaskAction
    fun verifyVersions() {
        val npmVersion =
            Regex("\\\"version\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
                .find(packageJson.get().asFile.readText())
                ?.groupValues
                ?.get(
                    index = 1,
                )
                ?: error("Missing version in tools/package.json")
        val expectedVersion = mavenVersion.get()
        check(npmVersion == expectedVersion) {
            "Publication version mismatch: Maven=$expectedVersion, npm=$npmVersion"
        }
    }
}

val publicationGroup =
    providers
        .gradleProperty("figmaDocumentationSyncGroup")
        .getOrElse("com.marmatsan.figma-documentation-sync")
val publicationVersion =
    providers
        .gradleProperty("figmaDocumentationSyncVersion")
        .getOrElse("0.1.0-SNAPSHOT")
val configuredPublicationRepository =
    providers
        .gradleProperty("figmaDocumentationSyncPublicationRepository")
        .orNull
val stagingPublicationRepository =
    configuredPublicationRepository
        ?: layout.buildDirectory
            .dir("publication-repository")
            .get()
            .asFile.absolutePath
allprojects {
    group = publicationGroup
    version = publicationVersion
}

val verifyPublicationVersionAlignment =
    tasks.register<VerifyPublicationVersionAlignmentTask>(
        "verifyPublicationVersionAlignment",
    ) {
        group = "verification"
        description = "Checks that Maven and npm publication versions remain aligned."

        mavenVersion.set(publicationVersion)
        packageJson.set(layout.projectDirectory.file("tools/package.json"))
    }

tasks.register("publishPortablePublicationToStagingRepository") {
    group = "publishing"
    description = "Publishes the portable Figma sync artifacts to the configured staging Maven repository."

    dependsOn(
        verifyPublicationVersionAlignment,
        ":domain:publishAllPublicationsToStagingRepository",
        ":data:publishAllPublicationsToStagingRepository",
        ":plugin:publishAllPublicationsToStagingRepository",
        ":teamcity-adapter:publishAllPublicationsToStagingRepository",
    )
}

tasks.register<Exec>("verifyStagedPublication") {
    group = "verification"
    description = "Applies the staged Figma plugin from a standalone consumer build."
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
        "-PfigmaDocumentationSyncVersion=$publicationVersion",
        "-PfigmaDocumentationSyncPublicationRepository=$stagingPublicationRepository",
        "--stacktrace",
    )
}
