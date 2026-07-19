plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(projects.domain)
    implementation(gradleApi())
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(testFixtures(projects.domain))
    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
}
