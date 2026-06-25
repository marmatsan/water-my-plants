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
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

dependencies {
    // Kotest
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
    // MockK
    testImplementation(libs.io.mockk)
}

gradlePlugin {
    val pluginName = "com.marmatsan.dependencies"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "${pluginName}.plugin.DependenciesPlugin"
    }
}
