@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.net.URI

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
    alias(plugins.plugins.org.jetbrains.dokka)
    `maven-publish`
}

java {
    withSourcesJar()
}

dependencies {
    testImplementation(libs.bundles.kotest)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dokka {
    moduleName.set("unit-test-dsl")

    dokkaPublications.html {
        failOnWarning.set(true)
    }

    dokkaSourceSets.main {
        documentedVisibilities.set(
            setOf(
                VisibilityModifier.Public,
                VisibilityModifier.Internal
            )
        )
        reportUndocumented.set(true)

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/unit-testing/unit-test-dsl/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}

tasks.named("check") {
    dependsOn("dokkaGenerate")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "unit-test-dsl"

            pom {
                name.set("Typed Unit Test DSL")
                description.set("Assertion-framework-agnostic typed given-whenever-then phases for Kotlin tests.")
                url.set("https://github.com/marmatsan/water-my-plants/tree/main/repo/unit-testing")
                scm {
                    connection.set("scm:git:https://github.com/marmatsan/water-my-plants.git")
                    url.set("https://github.com/marmatsan/water-my-plants")
                }
            }
        }
    }

    repositories {
        maven {
            name = "staging"
            url =
                uri(
                    providers.gradleProperty("unitTestingPublicationRepository").orNull
                        ?: rootProject.layout.buildDirectory
                            .dir("publication-repository")
                            .get()
                            .asFile
                )
        }
    }
}
