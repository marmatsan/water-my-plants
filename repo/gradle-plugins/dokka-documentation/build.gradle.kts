@file:Suppress("AvoidDuplicateDependencies")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
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
    implementation(libs.org.jetbrains.dokka.gradle.plugin)
    testImplementation(projects.unitTestDsl)

    // Kotest
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
    // MockK
    testImplementation(libs.io.mockk)
}

gradlePlugin {
    val pluginName = "com.marmatsan.dokkaDocumentation"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "$pluginName.plugin.DokkaDocumentationGradleConventionPlugin"
    }
}
