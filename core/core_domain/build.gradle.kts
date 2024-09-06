import com.marmatsan.dependencies.plugin.DependenciesPlugin

plugins {
    alias(plugins.plugins.com.android.library)
    alias(plugins.plugins.com.google.protobuf)
    id("com.marmatsan.android")
}

dependencies {
    implementation(libs.com.google.protobuf)
}

protobuf {
    protoc {
        artifact =
            "com.google.protobuf:protoc:${DependenciesPlugin.Versions.PROTOBUF_LIBRARY_VERSION}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}