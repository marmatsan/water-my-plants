import java.net.URI

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(projects.domain)
    implementation(gradleApi())
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(testFixtures(projects.domain))
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
}

dokka {
    moduleName.set("ci-data")

    dokkaPublications.html {
        includes.from("docs/dokka/README.md")
    }

    dokkaSourceSets.main {
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/ci/data/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}
