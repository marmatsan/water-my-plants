@file:Suppress("AvoidDuplicateDependencies")

import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
    alias(plugins.plugins.org.jetbrains.dokka)
    `java-gradle-plugin`
    `maven-publish`
}

java {
    withSourcesJar()
}

dependencies {
    implementation(projects.domain)
    implementation(libs.com.michael.bull.kotlin.result)
    implementation(projects.data) {
        exclude(
            group = "com.pinterest.ktlint",
        )
    }
    implementation(gradleApi())

    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

gradlePlugin {
    plugins.register("com.marmatsan.verificationPlatform") {
        id = "com.marmatsan.verificationPlatform"
        implementationClass = "com.marmatsan.verificationPlatform.plugin.VerificationPlatformPlugin"
        displayName = "Repository Verification Platform"
        description = "Generates the typed repository verification plan consumed by CI adapters."
    }
}

tasks.named("check") {
    dependsOn("dokkaGenerate")
}

dokka {
    moduleName.set("verification-platform-plugin")

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
                        "repo/verification-platform/plugin/src/main/kotlin",
                ),
            )
            remoteLineSuffix.set("#L")
        }
    }
}
