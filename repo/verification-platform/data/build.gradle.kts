import java.net.URI
import org.gradle.api.tasks.PathSensitivity

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(projects.domain)
    implementation(gradleApi())
    implementation(libs.com.pinterest.ktlint.rule.engine)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(testFixtures(projects.domain))
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
}

val repositoryRootDirectory = layout.projectDirectory.dir("../../..")
val repositoryKotlinSources = fileTree(repositoryRootDirectory) {
    include(
        "**/*.kt",
        "**/*.kts"
    )
    exclude(
        "**/.git/**",
        "**/.gradle/**",
        "**/.idea/**",
        "**/.kotlin/**",
        "**/build/**",
        "**/node_modules/**",
        "tmp/**"
    )
}

tasks.register<JavaExec>("checkRepositoryKotlinFunctionArguments") {
    group = "verification"
    description = "Checks repository Kotlin function parameters and arguments for vertical layout."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(
        "com.marmatsan.verificationPlatform.data.kotlin.KotlinFunctionArgumentLayoutCli"
    )
    args(
        "check",
        repositoryRootDirectory.asFile.absolutePath
    )
    inputs.files(repositoryKotlinSources)
        .withPathSensitivity(PathSensitivity.RELATIVE)
}

tasks.register<JavaExec>("formatRepositoryKotlinFunctionArguments") {
    group = "formatting"
    description = "Formats repository Kotlin function parameters and arguments vertically."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(
        "com.marmatsan.verificationPlatform.data.kotlin.KotlinFunctionArgumentLayoutCli"
    )
    args(
        "format",
        repositoryRootDirectory.asFile.absolutePath
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
            "docs/dokka/README.md"
        )
    }

    dokkaSourceSets.main {
        reportUndocumented.set(true)

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/verification-platform/data/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}
