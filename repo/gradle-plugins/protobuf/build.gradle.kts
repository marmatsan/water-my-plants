@file:Suppress("AvoidDuplicateDependencies")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(projects.dependencies)
    implementation(libs.com.google.protobuf.gradle.plugin)
}

gradlePlugin {
    val pluginName = "com.marmatsan.protobuf"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "$pluginName.plugin.ProtobufGradleConventionPlugin"
    }
}
