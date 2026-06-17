plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(plugins.plugins.com.google.devtools.ksp)
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
    ksp(libs.me.tatarka.inject.kotlin.inject.compiler.ksp)

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

gradlePlugin {
    val pluginName = "com.marmatsan.figmaVersions"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "${pluginName}.plugin.FigmaVersionsGradleConventionPlugin"
    }
}
