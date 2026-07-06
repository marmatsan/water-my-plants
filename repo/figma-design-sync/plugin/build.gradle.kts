@file:Suppress("AvoidDuplicateDependencies")

import java.net.URI

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(plugins.plugins.com.google.devtools.ksp)
    alias(plugins.plugins.org.jetbrains.dokka)
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

tasks.withType<Test> {
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

dependencies {
    implementation(projects.domain)
    implementation(projects.data)

    ksp(libs.me.tatarka.inject.kotlin.inject.compiler.ksp)

    implementation(libs.me.tatarka.inject.kotlin.inject.runtime)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    // Kotest
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    // Cucumber
    testImplementation(platform(libs.io.cucumber.bom))
    testImplementation(libs.io.cucumber.java8)
    testImplementation(libs.io.cucumber.junit.platform.engine)
    testImplementation(libs.org.junit.platform.suite)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

gradlePlugin {
    val pluginName = "com.marmatsan.figmaDesignSync"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "${pluginName}.plugin.gradle.FigmaDesignSyncGradlePlugin"
    }
}

dokka {
    moduleName.set("figmaDesignSync-plugin")

    dokkaPublications.html {
        includes.from("docs/dokka/README.md")
    }

    dokkaSourceSets.main {
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(
                URI(
                    "https://github.com/marmatsan/water-my-plants/tree/main/" +
                        "repo/figma-design-sync/plugin/src/main/kotlin"
                )
            )
            remoteLineSuffix.set("#L")
        }
    }
}
