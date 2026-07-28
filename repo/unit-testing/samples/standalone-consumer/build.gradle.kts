@file:Suppress("AvoidDuplicateDependencies")

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
}

dependencies {
    implementation(libs.com.marmatsan.repo.unit.test.dsl)
}

tasks.register("verifyDslConsumption") {
    group = "verification"
    description = "Compiles typed behavior chains from the staged DSL artifact."
    dependsOn("compileKotlin")
}
