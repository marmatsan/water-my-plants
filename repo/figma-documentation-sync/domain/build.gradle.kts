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
    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)

    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.bundles.kotestBundle)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(
                components["java"]
            )
            artifactId = "domain"

            pom {
                name.set("Figma Documentation Sync Domain")
                description.set("Portable models and ports for Figma design synchronization.")
            }
        }
    }
}

dokka {
    dokkaSourceSets.main {
        samples.from(
            file("src/main/kotlin/com/marmatsan/figmaDocumentationSync/domain/samples/DomainKDocSamples.kt")
        )
    }
}
