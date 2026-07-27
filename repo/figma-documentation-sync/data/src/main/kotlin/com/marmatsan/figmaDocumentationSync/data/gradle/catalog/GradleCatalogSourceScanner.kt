package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.figmaDocumentationSync.data.gradle.isInsideNestedGradleBuild
import java.io.File

/** Locates Gradle and Kotlin sources while preserving main and included-build boundaries. */
internal class GradleCatalogSourceScanner {
    fun mainBuildFiles(
        rootDir: File,
    ): Sequence<File> =
        buildFiles(
            rootDir = rootDir,
        ).filterNot { file ->
            file.isInsideNestedGradleBuild(rootDir)
        }

    fun buildFiles(
        rootDir: File,
    ): Sequence<File> =
        rootDir.walkTopDown().filter { file ->
            file.isFile && file.name == BUILD_FILE_NAME
        }

    fun kotlinFiles(
        rootDir: File,
    ): Sequence<File> =
        rootDir.walkTopDown().filter { file ->
            file.isFile && file.extension == KOTLIN_FILE_EXTENSION
        }

    fun includedBuildModulePath(
        source: File,
        rootDir: File,
        modulePathPrefix: String,
    ): String {
        val moduleDir = if (source.isDirectory) source else source.parentFile
        val relativePath = rootDir.toPath().relativize(moduleDir.toPath()).toString()
        return if (relativePath.isEmpty()) {
            modulePathPrefix
        } else {
            "$modulePathPrefix:${relativePath.toModuleSegments()}"
        }
    }

    fun mainModulePath(
        source: File,
        rootDir: File,
    ): String {
        val relativePath = rootDir.toPath().relativize(source.toPath()).toString()
        return if (relativePath.isEmpty()) ROOT_MODULE else ":${relativePath.toModuleSegments()}"
    }

    private fun String.toModuleSegments(): String =
        replace(
            File.separatorChar,
            ':',
        ).replace(
            '/',
            ':',
        ).replace(
            '\\',
            ':',
        )

    companion object {
        const val ROOT_MODULE = ":"
        private const val BUILD_FILE_NAME = "build.gradle.kts"
        private const val KOTLIN_FILE_EXTENSION = "kt"
    }
}
