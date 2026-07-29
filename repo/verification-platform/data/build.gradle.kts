@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.PathSensitivity

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(plugins.plugins.org.jetbrains.dokka)
    `maven-publish`
}

dependencies {
    implementation(projects.domain)
    implementation(gradleApi())
    implementation(libs.com.michael.bull.kotlin.result)
    implementation(libs.bundles.ktlint)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(testFixtures(projects.domain))
    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.bundles.kotest)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

val repositoryRootDirectory = layout.projectDirectory.dir("../../..")

tasks.withType<Test>().configureEach {
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    systemProperty(
        "waterMyPlants.repositoryRoot",
        repositoryRootDirectory.asFile.absolutePath
    )
}

val repositoryKotlinSources =
    fileTree(repositoryRootDirectory) {
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

tasks.register<JavaExec>("checkRepositoryKotlinStyle") {
    group = "verification"
    description = "Checks repository Kotlin sources with standard and repository-owned KtLint rules."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(
        "com.marmatsan.verificationPlatform.data.kotlin.RepositoryKotlinStyleCli"
    )
    args(
        "check",
        repositoryRootDirectory.asFile.absolutePath
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
        "com.marmatsan.verificationPlatform.data.kotlin.RepositoryKotlinStyleCli"
    )
    args(
        "format",
        repositoryRootDirectory.asFile.absolutePath
    )
    outputs.upToDateWhen { false }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "data"
        }
    }
}
