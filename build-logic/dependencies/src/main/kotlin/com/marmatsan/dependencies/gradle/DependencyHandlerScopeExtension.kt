package com.marmatsan.dependencies.gradle

import org.gradle.kotlin.dsl.DependencyHandlerScope

fun DependencyHandlerScope.implementation(dependencyNotation: String): org.gradle.api.artifacts.Dependency? =
    add("implementation", dependencyNotation)

fun DependencyHandlerScope.implementation(dependency: org.gradle.api.artifacts.Dependency): org.gradle.api.artifacts.Dependency? =
    add("implementation", dependency)

fun DependencyHandlerScope.testImplementation(dependencyNotation: String): org.gradle.api.artifacts.Dependency? =
    add("testImplementation", dependencyNotation)

fun DependencyHandlerScope.testImplementation(dependency: org.gradle.api.artifacts.Dependency): org.gradle.api.artifacts.Dependency? =
    add("testImplementation", dependency)

fun DependencyHandlerScope.testRuntimeOnly(dependencyNotation: String): org.gradle.api.artifacts.Dependency? =
    add("testRuntimeOnly", dependencyNotation)

fun DependencyHandlerScope.ksp(dependencyNotation: String): org.gradle.api.artifacts.Dependency? =
    add("ksp", dependencyNotation)