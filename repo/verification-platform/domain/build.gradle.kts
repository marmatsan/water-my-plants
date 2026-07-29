@file:Suppress("AvoidDuplicateDependencies")

import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(plugins.plugins.org.jetbrains.dokka)
    `java-library`
    `java-test-fixtures`
    `maven-publish`
}

components.named<AdhocComponentWithVariants>("java") {
    withVariantsFromConfiguration(configurations["testFixturesApiElements"]) {
        skip()
    }
    withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) {
        skip()
    }
}

dependencies {
    api(libs.com.michael.bull.kotlin.result)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.bundles.kotest)
    testImplementation(platform(libs.io.cucumber.bom))
    testImplementation(libs.bundles.cucumber)
    testImplementation(libs.org.junit.platform.suite)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    systemProperty(
        "cucumber.junit-platform.naming-strategy",
        "long",
    )
    systemProperty(
        "cucumber.plugin",
        "pretty,html:build/reports/cucumber/cucumber.html,json:build/reports/cucumber/cucumber.json",
    )

    System.getProperty("cucumber.filter.tags")?.let { tags ->
        systemProperty(
            "cucumber.filter.tags",
            tags,
        )
    }
    System.getProperty("cucumber.features")?.let { features ->
        systemProperty(
            "cucumber.features",
            features,
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "domain"
        }
    }
}
