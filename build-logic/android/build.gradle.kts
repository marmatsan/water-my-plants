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
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    val pluginName = "com.marmatsan.android"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass = "${pluginName}.plugin.AndroidPlugin"
    }
}
