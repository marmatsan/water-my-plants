@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication

plugins {
    kotlin("jvm")
    alias(plugins.plugins.org.jetbrains.dokka)
    `java-library`
    `maven-publish`
}

dependencies {
    api(libs.com.michael.bull.kotlin.result)
    implementation(projects.domain)
    implementation(projects.data)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)
    implementation(libs.org.snakeyaml.engine)

    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.bundles.kotest)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(
                components["java"]
            )
            artifactId = "figma-documentation-sync-teamcity-adapter"

            pom {
                name.set("Figma Documentation Sync TeamCity Adapter")
                description.set("Optional TeamCity adapter for the portable Figma design sync model.")
            }
        }
    }
}
