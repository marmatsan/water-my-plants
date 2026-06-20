plugins {
    id("org.jetbrains.kotlin.jvm")
    alias(plugins.plugins.org.jetbrains.kotlin.plugin.serialization)
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
    implementation(projects.figmaDesignSync.domain)
    implementation(projects.dependencies)

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
