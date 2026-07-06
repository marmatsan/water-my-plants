package com.marmatsan.figmaDesignSync.data.gradle

import java.io.File

internal fun File.isInsideNestedGradleBuild(rootDir: File): Boolean {
    val normalizedRoot = rootDir.canonicalFile

    return generateSequence(parentFile?.canonicalFile) { file -> file.parentFile?.canonicalFile }
        .takeWhile { file -> file != normalizedRoot.parentFile }
        .any { file -> file != normalizedRoot && file.isGradleBuildRoot() }
}

internal fun File.nestedGradleBuildRoots(): Sequence<File> =
    walkTopDown()
        .filter(File::isDirectory)
        .filter { file -> file.canonicalFile != canonicalFile }
        .filter(File::isGradleBuildRoot)

private fun File.isGradleBuildRoot(): Boolean =
    resolve("settings.gradle.kts").isFile || resolve("settings.gradle").isFile
