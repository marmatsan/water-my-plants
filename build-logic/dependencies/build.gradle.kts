plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins.register("com.marmatsan.dependencies") {
        id = "com.marmatsan.dependencies"
        implementationClass = "com.marmatsan.dependencies.plugin.DependenciesPlugin"
    }
}
