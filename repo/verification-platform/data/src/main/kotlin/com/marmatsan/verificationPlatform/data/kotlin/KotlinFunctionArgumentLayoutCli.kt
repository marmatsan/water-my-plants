package com.marmatsan.verificationPlatform.data.kotlin

import java.io.File
import kotlin.system.exitProcess

internal object KotlinFunctionArgumentLayoutCli {
    @JvmStatic
    fun main(
        args: Array<String>
    ) {
        require(args.size == EXPECTED_ARGUMENT_COUNT) {
            "Expected <check|format> <repository-root>."
        }
        val operation = args[0]
        val repositoryRoot = File(args[1]).canonicalFile
        require(repositoryRoot.isDirectory) {
            "Repository root does not exist: ${repositoryRoot.invariantSeparatorsPath}"
        }

        val sourceFiles = repositoryRoot.kotlinSourceFiles()
        val layout = KotlinFunctionArgumentLayout(
            sourceFiles = sourceFiles
        )
        when (operation) {
            "check" -> checkLayout(
                repositoryRoot = repositoryRoot,
                sourceFiles = sourceFiles,
                layout = layout
            )
            "format" -> formatLayout(
                repositoryRoot = repositoryRoot,
                sourceFiles = sourceFiles,
                layout = layout
            )
            else -> error("Unknown operation '$operation'. Expected 'check' or 'format'.")
        }
    }

    private fun checkLayout(
        repositoryRoot: File,
        sourceFiles: List<File>,
        layout: KotlinFunctionArgumentLayout
    ) {
        val violations = sourceFiles.flatMap { file ->
            layout.inspect(file).map { violation ->
                "${file.relativePathFrom(
                    repositoryRoot = repositoryRoot
                )}:${violation.line}:${violation.column}: " +
                    violation.detail
            }
        }
        if (violations.isEmpty()) {
            println("Kotlin function argument layout is valid in ${sourceFiles.size} file(s).")
            return
        }

        System.err.println("Kotlin function argument layout violations:")
        violations.forEach { violation -> System.err.println("- $violation") }
        System.err.println("Run .\\gradlew.bat formatKotlinFunctionArguments to correct them.")
        exitProcess(1)
    }

    private fun formatLayout(
        repositoryRoot: File,
        sourceFiles: List<File>,
        layout: KotlinFunctionArgumentLayout
    ) {
        var formattedFileCount = 0
        sourceFiles.forEach { file ->
            val source = file.readText()
            val formatted = layout.format(
                file = file
            )
            if (formatted != source) {
                file.writeText(formatted)
                formattedFileCount += 1
            }
        }
        println(
            "Formatted Kotlin function arguments in $formattedFileCount of " +
                "${sourceFiles.size} file(s) under ${repositoryRoot.invariantSeparatorsPath}."
        )
    }

    private fun File.kotlinSourceFiles(): List<File> = walkTopDown()
        .onEnter { directory -> directory == this || directory.name !in EXCLUDED_DIRECTORY_NAMES }
        .filter(File::isFile)
        .filter { file -> file.extension == "kt" || file.extension == "kts" }
        .sortedBy { file -> file.relativePathFrom(
            repositoryRoot = this
        ) }
        .toList()

    private fun File.relativePathFrom(
        repositoryRoot: File
    ): String =
        relativeTo(repositoryRoot).invariantSeparatorsPath

    private const val EXPECTED_ARGUMENT_COUNT = 2
    private val EXCLUDED_DIRECTORY_NAMES = setOf(
        ".git",
        ".gradle",
        ".idea",
        ".kotlin",
        "build",
        "node_modules",
        "tmp"
    )
}
