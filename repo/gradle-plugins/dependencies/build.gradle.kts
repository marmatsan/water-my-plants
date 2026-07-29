@file:Suppress("AvoidDuplicateDependencies")

plugins {
    `kotlin-dsl`
    `maven-publish`
}

tasks.withType<Test> {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

dependencies {
    implementation(libs.com.marmatsan.repo.catalog.api)
    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)

    testImplementation(libs.bundles.kotest)
    testRuntimeOnly(libs.org.junit.platform.launcher)
    testImplementation(libs.io.mockk)
}
