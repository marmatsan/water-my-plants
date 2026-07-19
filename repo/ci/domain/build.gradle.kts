plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    `java-test-fixtures`
}

dependencies {
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
