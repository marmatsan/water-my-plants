package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.figmaDocumentationSync.data.gradle.isInsideNestedGradleBuild
import java.io.File

/** Locates Gradle and Kotlin sources while preserving main and included-build boundaries. */
internal class GradleCatalogSourceScanner {
    /** Returns main-build Gradle module files while excluding nested builds. */
    fun mainBuildFiles(
        rootDir: File
    ): Sequence<File> =
        buildFiles(
            rootDir = rootDir
        ).filterNot { file ->
            file.isInsideNestedGradleBuild(rootDir)
        }

    /** Returns every Gradle module build file beneath [rootDir]. */
    fun buildFiles(
        rootDir: File
    ): Sequence<File> =
        rootDir.walkTopDown().filter { file ->
            file.isFile && file.name == BUILD_FILE_NAME
        }

    /** Returns every Kotlin source file beneath [rootDir]. */
    fun kotlinFiles(
        rootDir: File
    ): Sequence<File> =
        rootDir.walkTopDown().filter { file ->
            file.isFile && file.extension == KOTLIN_FILE_EXTENSION
        }

    /** Maps [source] to its logical included-build module identity. */
    fun includedBuildModulePath(
        source: File,
        rootDir: File,
        modulePathPrefix: String
    ): String {
        val moduleDir = if (source.isDirectory) source else source.parentFile
        val relativePath = rootDir.toPath().relativize(moduleDir.toPath()).toString()
        return if (relativePath.isEmpty()) {
            modulePathPrefix
        } else {
            "$modulePathPrefix:${relativePath.toModuleSegments()}"
        }
    }

    /** Maps [source] to its logical module identity in the main build. */
    fun mainModulePath(
        source: File,
        rootDir: File
    ): String {
        val relativePath = rootDir.toPath().relativize(source.toPath()).toString()
        return if (relativePath.isEmpty()) ROOT_MODULE else ":${relativePath.toModuleSegments()}"
    }

    private fun String.toModuleSegments(): String =
        replace(
            File.separatorChar,
            ':'
        ).replace(
            '/',
            ':'
        ).replace(
            '\\',
            ':'
        )

    /** Shared logical module identities used by catalog readers. */
    companion object {
        /** Logical Gradle path of the root module. */
        const val ROOT_MODULE = ":"
        private const val BUILD_FILE_NAME = "build.gradle.kts"
        private const val KOTLIN_FILE_EXTENSION = "kt"
    }
}
