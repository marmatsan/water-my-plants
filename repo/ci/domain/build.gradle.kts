import java.net.URI

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.dokka")
    `java-test-fixtures`
}

dependencies {
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testImplementation(platform(libs.io.cucumber.bom))
    testImplementation(libs.io.cucumber.java8)
    testImplementation(libs.io.cucumber.junit.platform.engine)
    testImplementation(libs.org.junit.platform.suite)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    systemProperty(
        "cucumber.plugin",
        "pretty,html:build/reports/cucumber/cucumber.html,json:build/reports/cucumber/cucumber.json"
    )

    System.getProperty("cucumber.filter.tags")?.let { tags ->
        systemProperty("cucumber.filter.tags", tags)
    }
    System.getProperty("cucumber.features")?.let { features ->
        systemProperty("cucumber.features", features)
    }
}

dokka {
    moduleName.set("ci-domain")

    dokkaPublications.html {
        includes.from("docs/dokka/README.md")
    }

    dokkaSourceSets.main {
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/ci/domain/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}
