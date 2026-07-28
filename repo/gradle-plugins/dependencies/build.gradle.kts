@file:Suppress("AvoidDuplicateDependencies")

plugins {
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

dependencies {
    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)

    // Kotest
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.platform.launcher)
    // MockK
    testImplementation(libs.io.mockk)
}
