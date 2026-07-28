@file:Suppress("AvoidDuplicateDependencies")

plugins {
    alias(plugins.plugins.org.jetbrains.kotlin.jvm)
    alias(plugins.plugins.com.marmatsan.unitTest)
    alias(plugins.plugins.com.marmatsan.android) apply false
    alias(plugins.plugins.com.marmatsan.bddTest) apply false
    alias(plugins.plugins.com.marmatsan.compose) apply false
    alias(plugins.plugins.com.marmatsan.dokkaDocumentation) apply false
    alias(plugins.plugins.com.marmatsan.protobuf) apply false
}

tasks.register("verifyPluginConsumption") {
    group = "verification"
    description = "Verifies staged marker resolution and the portable unit-test convention contract."

    doLast {
        check(pluginManager.hasPlugin("com.marmatsan.unitTest"))
        val testDependencies = configurations.getByName("testImplementation").dependencies
        check(testDependencies.any { it.group == "com.marmatsan.repo" && it.name == "unit-test-dsl" })
        check(testDependencies.any { it.group == "io.kotest" && it.name == "kotest-runner-junit5" })
        check(testDependencies.any { it.group == "io.mockk" && it.name == "mockk" })
    }
}
