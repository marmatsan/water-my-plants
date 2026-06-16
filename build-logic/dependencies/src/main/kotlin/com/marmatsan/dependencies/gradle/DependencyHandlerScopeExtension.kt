package com.marmatsan.dependencies.gradle

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.DependencyHandlerScope

fun DependencyHandlerScope.implementation(
    dependencyNotation: String
): Dependency? = add(
    "implementation",
    dependencyNotation
)

fun DependencyHandlerScope.implementation(
    dependency: Dependency
): Dependency? = add(
    "implementation",
    dependency
)

fun DependencyHandlerScope.implementation(
    libs: VersionCatalog,
    libraryGroup: String,
    artifact: String
): Dependency? = implementation(
    dependencyNotation = libs.requireDependencyNotation(
        libraryGroup = libraryGroup,
        artifact = artifact
    )
)

fun DependencyHandlerScope.implementationPlatform(
    libs: VersionCatalog,
    libraryGroup: String,
    artifact: String
): Dependency? = implementation(
    dependency = platform(
        libs.requireDependencyNotation(
            libraryGroup = libraryGroup,
            artifact = artifact
        )
    )
)

fun DependencyHandlerScope.testImplementation(
    dependencyNotation: String
): Dependency? = add(
    "testImplementation",
    dependencyNotation
)

fun DependencyHandlerScope.testImplementation(
    dependency: Dependency
): Dependency? = add(
    "testImplementation",
    dependency
)

fun DependencyHandlerScope.testImplementation(
    libs: VersionCatalog,
    libraryGroup: String,
    artifact: String
): Dependency? = testImplementation(
    dependencyNotation = libs.requireDependencyNotation(
        libraryGroup = libraryGroup,
        artifact = artifact
    )
)

fun DependencyHandlerScope.testImplementationPlatform(
    libs: VersionCatalog,
    libraryGroup: String,
    artifact: String
): Dependency? = testImplementation(
    dependency = platform(
        libs.requireDependencyNotation(
            libraryGroup = libraryGroup,
            artifact = artifact
        )
    )
)

fun DependencyHandlerScope.testRuntimeOnly(
    dependencyNotation: String
): Dependency? = add(
    "testRuntimeOnly",
    dependencyNotation
)

fun DependencyHandlerScope.testRuntimeOnly(
    libs: VersionCatalog,
    libraryGroup: String,
    artifact: String
): Dependency? = testRuntimeOnly(
    dependencyNotation = libs.requireDependencyNotation(
        libraryGroup = libraryGroup,
        artifact = artifact
    )
)

fun DependencyHandlerScope.ksp(
    dependencyNotation: String
): Dependency? = add(
    "ksp",
    dependencyNotation
)

fun DependencyHandlerScope.ksp(
    libs: VersionCatalog,
    libraryGroup: String,
    artifact: String
): Dependency? = ksp(
    dependencyNotation = libs.requireDependencyNotation(
        libraryGroup = libraryGroup,
        artifact = artifact
    )
)
