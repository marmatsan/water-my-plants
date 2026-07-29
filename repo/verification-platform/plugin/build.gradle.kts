@file:Suppress("AvoidDuplicateDependencies")

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
    alias(plugins.plugins.org.jetbrains.dokka)
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(projects.domain)
    implementation(libs.com.michael.bull.kotlin.result)
    implementation(projects.data) {
        exclude(
            group = "com.pinterest.ktlint"
        )
    }
    testImplementation(libs.com.marmatsan.repo.unit.test.dsl)
    testImplementation(libs.bundles.kotestBundle)
    testRuntimeOnly(libs.org.junit.platform.launcher)
}

gradlePlugin {
    plugins.register("com.marmatsan.verificationPlatform") {
        id = "com.marmatsan.verificationPlatform"
        implementationClass = "com.marmatsan.verificationPlatform.plugin.VerificationPlatformPlugin"
        displayName = "Repository Verification Platform"
        description = "Generates the typed repository verification plan consumed by CI adapters."
    }
}
