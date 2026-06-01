plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(plugins.plugins.org.jetbrains.kotlinx.kover)
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
    // JUnit5
    testImplementation(platform(libs.org.junit.bom))
    testImplementation(libs.org.junit.jupiter.api)
    testRuntimeOnly(libs.org.junit.jupiter.engine)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
    // AssertK
    testImplementation(libs.com.willowtreeapps.assertk)
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
