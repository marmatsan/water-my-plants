@file:Suppress("AvoidDuplicateDependencies")

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
    implementation(projects.data)
    implementation(projects.plugin)
    implementation(projects.teamcityAdapter)
    implementation("com.marmatsan.repo:catalog-core")
    implementation("com.marmatsan.repo:water-my-plants-catalog")
}

gradlePlugin {
    val pluginName = "com.marmatsan.waterMyPlantsFigmaDesignSync"
    plugins.register(pluginName) {
        id = pluginName
        implementationClass =
            "com.marmatsan.figmaDesignSync.projectConfig.WaterMyPlantsFigmaDesignSyncGradlePlugin"
    }
}
