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
            artifactId = "figma-documentation-sync-teamcity-adapter"

            pom {
                name.set("Figma Documentation Sync TeamCity Adapter")
                description.set("Optional TeamCity adapter for the portable Figma design sync model.")
                url.set("https://github.com/marmatsan/water-my-plants/tree/main/repo/figma-documentation-sync")
                scm {
                    connection.set("scm:git:https://github.com/marmatsan/water-my-plants.git")
                    url.set("https://github.com/marmatsan/water-my-plants")
                }
            }
        }
    }
}
