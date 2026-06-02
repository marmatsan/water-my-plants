package com.marmatsan.protobuf.plugin

import com.google.protobuf.gradle.ProtobufExtension
import com.marmatsan.dependencies.gradle.requireLibraryNotation
import com.marmatsan.dependencies.gradle.implementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class ProtobufPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Applied plugins
        project.pluginManager.apply("com.google.protobuf")

        val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

        project.extensions.configure<ProtobufExtension>("protobuf") {
            protoc {
                artifact = libs.requireLibraryNotation("com.google.protobuf.protoc")
            }

            generateProtoTasks {
                all().forEach { task ->
                    task.builtins {
                        create("kotlin") {
                            option("lite")
                        }
                        create("java") {
                            option("lite")
                        }
                    }
                }
            }
        }

        // Applied libs
        project.dependencies {
            implementation(libs.requireLibraryNotation("com.google.protobuf.kotlin"))
        }
    }
}
