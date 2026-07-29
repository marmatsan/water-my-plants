@file:Suppress("AvoidDuplicateDependencies")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(projects.dependencies)
}

gradlePlugin {
    val pluginName = "com.marmatsan.bddTest"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "$pluginName.plugin.BddTestGradleConventionPlugin"
    }
}
