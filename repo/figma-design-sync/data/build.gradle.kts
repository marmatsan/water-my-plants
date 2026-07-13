@file:Suppress("AvoidDuplicateDependencies")

import java.net.URI

plugins {
    id("org.jetbrains.kotlin.jvm")
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(plugins.plugins.org.jetbrains.dokka)
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(projects.domain)
    implementation("com.marmatsan.repo:catalog-core")
    implementation("com.marmatsan.repo:water-my-plants-catalog")

    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)

    // Ktor
    implementation(platform(libs.io.ktor.bom))
    implementation(libs.io.ktor.client.core)
    implementation(libs.io.ktor.client.cio)
    implementation(libs.io.ktor.client.content.negotiation)
    implementation(libs.io.ktor.serialization.kotlinx.json)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    // Kotest
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

dokka {
    moduleName.set("figmaDesignSync-data")

    dokkaPublications.html {
        includes.from("docs/dokka/README.md")
    }

    dokkaSourceSets.main {
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/figma-design-sync/data/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}
