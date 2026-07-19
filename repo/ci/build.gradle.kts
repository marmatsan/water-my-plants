plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.org.jetbrains.kotlinx.serialization.json)

    testImplementation(libs.io.kotest.runner.junit5)
    testImplementation(libs.io.kotest.assertions.core)
    testRuntimeOnly(libs.org.junit.jupiter.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

gradlePlugin {
    plugins.register("com.marmatsan.ci") {
        id = "com.marmatsan.ci"
        implementationClass = "com.marmatsan.ci.plugin.CiGradlePlugin"
        displayName = "Water My Plants CI Planner"
        description = "Generates the typed repository verification plan consumed by CI adapters."
    }
}
