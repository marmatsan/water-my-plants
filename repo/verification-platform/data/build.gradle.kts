import org.gradle.api.tasks.PathSensitivity
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(projects.domain)
    implementation(gradleApi())
    implementation(libs.com.pinterest.ktlint.rule.engine)
    implementation(libs.com.pinterest.ktlint.ruleset.standard)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(testFixtures(projects.domain))
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

val repositoryRootDirectory = layout.projectDirectory.dir("../../..")

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    systemProperty(
        "waterMyPlants.repositoryRoot",
        repositoryRootDirectory.asFile.absolutePath,
    )
}

val repositoryKotlinSources =
    fileTree(repositoryRootDirectory) {
        include(
            "**/*.kt",
            "**/*.kts",
        )
        exclude(
            "**/.git/**",
            "**/.gradle/**",
            "**/.idea/**",
            "**/.kotlin/**",
            "**/build/**",
            "**/node_modules/**",
            "tmp/**",
        )
    }

tasks.register<JavaExec>("checkRepositoryKotlinStyle") {
    group = "verification"
    description = "Checks repository Kotlin sources with standard and repository-owned KtLint rules."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(
        "com.marmatsan.verificationPlatform.data.kotlin.RepositoryKotlinStyleCli",
    )
    args(
        "check",
        repositoryRootDirectory.asFile.absolutePath,
    )
    inputs
        .files(repositoryKotlinSources)
        .withPathSensitivity(PathSensitivity.RELATIVE)
}

tasks.register<JavaExec>("formatRepositoryKotlinStyle") {
    group = "formatting"
    description = "Formats repository Kotlin sources with standard and repository-owned KtLint rules."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(
        "com.marmatsan.verificationPlatform.data.kotlin.RepositoryKotlinStyleCli",
    )
    args(
        "format",
        repositoryRootDirectory.asFile.absolutePath,
    )
    outputs.upToDateWhen { false }
}

tasks.named("check") {
    dependsOn("dokkaGenerate")
}

dokka {
    moduleName.set("verification-platform-data")

    dokkaPublications.html {
        failOnWarning.set(true)
        includes.from(
            "docs/dokka/README.md",
        )
    }

    dokkaSourceSets.main {
        documentedVisibilities.set(
            setOf(
                VisibilityModifier.Public,
                VisibilityModifier.Internal,
            ),
        )
        reportUndocumented.set(true)

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/verification-platform/data/src/main/kotlin",
                ),
            )
            remoteLineSuffix.set("#L")
        }
    }
}
