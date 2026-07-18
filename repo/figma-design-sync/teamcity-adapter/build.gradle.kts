@file:Suppress("AvoidDuplicateDependencies")

plugins {
    id("org.jetbrains.kotlin.jvm")
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
    implementation(projects.data)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)
    implementation(libs.org.snakeyaml.engine)

    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}
