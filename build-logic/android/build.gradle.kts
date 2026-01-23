plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    /* Modules */
    implementation(projects.dependencies)

    /* Libraries */
    compileOnly(libs.build.gradle)
    implementation(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    val pluginName = "com.marmatsan.android"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "${pluginName}.plugin.AndroidPlugin"
    }
}