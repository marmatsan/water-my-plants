@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication

plugins {
    kotlin("jvm")
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(plugins.plugins.org.jetbrains.dokka)
    `maven-publish`
}

dependencies {
    implementation(projects.domain)

    implementation(libs.com.michael.bull.kotlin.result)
    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)

    implementation(platform(libs.io.ktor.bom))
    implementation(libs.bundles.ktorClient)
    implementation(libs.io.modelcontextprotocol.kotlin.sdk.client)
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
            artifactId = "figma-documentation-sync-data"

            pom {
                name.set("Figma Documentation Sync Data")
                description.set("Portable filesystem, Gradle, catalog, and Figma adapters.")
            }
        }
    }
}
