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
    /* Modules */
    implementation(projects.dependencies)

    /* Protobuf */
    implementation(libs.protobuf.gradle.plugin)

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
    val pluginName = "com.marmatsan.protobuf"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "${pluginName}.plugin.ProtobufPlugin"
    }
}