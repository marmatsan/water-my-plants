package com.marmatsan.dependencies.gradle

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.DependencyHandlerScope

class VersionCatalogDependencyHandler internal constructor(
    private val dependencies: DependencyHandlerScope,
    private val libs: VersionCatalog
) {
    fun implementation(
        libraryGroup: String,
        artifact: String
    ): Dependency? = dependencies.implementation(
        dependencyNotation = libs.requireDependencyNotation(
            libraryGroup = libraryGroup,
            artifact = artifact
        )
    )

    fun implementationBundle(
        bundle: String
    ) {
        libs.requireBundle(
            alias = bundle
        ).get().forEach { dependency ->
            dependencies.implementation(dependency)
        }
    }

    fun implementationPlatform(
        libraryGroup: String,
        artifact: String
    ): Dependency? = dependencies.implementation(
        dependency = dependencies.platform(
            libs.requireDependencyNotation(
                libraryGroup = libraryGroup,
                artifact = artifact
            )
        )
    )

    fun testImplementation(
        libraryGroup: String,
        artifact: String
    ): Dependency? = dependencies.testImplementation(
        dependencyNotation = libs.requireDependencyNotation(
            libraryGroup = libraryGroup,
            artifact = artifact
        )
    )

    fun testImplementationPlatform(
        libraryGroup: String,
        artifact: String
    ): Dependency? = dependencies.testImplementation(
        dependency = dependencies.platform(
            libs.requireDependencyNotation(
                libraryGroup = libraryGroup,
                artifact = artifact
            )
        )
    )

    fun testRuntimeOnly(
        libraryGroup: String,
        artifact: String
    ): Dependency? = dependencies.testRuntimeOnly(
        dependencyNotation = libs.requireDependencyNotation(
            libraryGroup = libraryGroup,
            artifact = artifact
        )
    )

    fun ksp(
        libraryGroup: String,
        artifact: String
    ): Dependency? = dependencies.ksp(
        dependencyNotation = libs.requireDependencyNotation(
            libraryGroup = libraryGroup,
            artifact = artifact
        )
    )
}

fun DependencyHandlerScope.withVersionCatalog(
    libs: VersionCatalog
): VersionCatalogDependencyHandler =
    VersionCatalogDependencyHandler(
        dependencies = this,
        libs = libs
    )

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

fun DependencyHandlerScope.testRuntimeOnly(
    dependencyNotation: String
): Dependency? = add(
    "testRuntimeOnly",
    dependencyNotation
)

fun DependencyHandlerScope.ksp(
    dependencyNotation: String
): Dependency? = add(
    "ksp",
    dependencyNotation
)
