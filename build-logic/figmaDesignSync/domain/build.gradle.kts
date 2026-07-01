@file:Suppress("AvoidDuplicateDependencies")

import java.net.URI

plugins {
    id("org.jetbrains.kotlin.jvm")
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
    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)

    // Kotest
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

dokka {
    moduleName.set("figmaDesignSync-domain")

    dokkaPublications.html {
        includes.from("docs/dokka/README.md")
    }

    dokkaSourceSets.main {
        samples.from(file("src/main/kotlin/com/marmatsan/figmaDesignSync/domain/samples/DomainKDocSamples.kt"))

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "build-logic/figmaDesignSync/domain/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}
