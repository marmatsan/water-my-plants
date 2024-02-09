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
    implementation(libs.kotlin.android.extensions)
}

gradlePlugin {
    plugins.register("com.marmatsan.android") {
        id = "com.marmatsan.android"
        implementationClass = "com.marmatsan.android.AndroidPlugin"
    }
}