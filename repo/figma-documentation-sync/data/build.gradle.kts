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

    // Ktor
    implementation(platform(libs.io.ktor.bom))
    implementation(libs.io.ktor.client.core)
    implementation(libs.io.ktor.client.cio)
    implementation(libs.io.ktor.client.content.negotiation)
    implementation(libs.io.ktor.serialization.kotlinx.json)
    implementation(libs.io.modelcontextprotocol.kotlin.sdk.client)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)
    implementation(libs.org.snakeyaml.engine)

    // Kotest
    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(
                components["java"],
            )
            artifactId = "figma-documentation-sync-data"

            pom {
                name.set("Figma Documentation Sync Data")
                description.set("Portable filesystem, Gradle, catalog, and Figma adapters.")
                url.set("https://github.com/marmatsan/water-my-plants/tree/main/repo/figma-documentation-sync")
                scm {
                    connection.set("scm:git:https://github.com/marmatsan/water-my-plants.git")
                    url.set("https://github.com/marmatsan/water-my-plants")
                }
            }
        }
    }
}
