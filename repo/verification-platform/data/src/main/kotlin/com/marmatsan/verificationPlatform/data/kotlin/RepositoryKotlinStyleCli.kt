package com.marmatsan.verificationPlatform.data.kotlin

import java.io.File
import kotlin.system.exitProcess

/** Command-line boundary for checking or formatting the repository Kotlin style contract. */
internal object RepositoryKotlinStyleCli {
    /** Executes `check` or `format` for the repository root supplied in [args]. */
    @JvmStatic
    fun main(
        args: Array<String>,
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
        val style =
            RepositoryKotlinStyle(
                sourceFiles = sourceFiles,
                editorConfigFile = repositoryRoot.resolve(".editorconfig"),
            )
        when (operation) {
            "check" -> {
                checkStyle(
                    repositoryRoot = repositoryRoot,
                    sourceFiles = sourceFiles,
                    style = style,
                )
            }

            "format" -> {
                formatStyle(
                    repositoryRoot = repositoryRoot,
                    sourceFiles = sourceFiles,
                    style = style,
                )
            }

            else -> {
                error("Unknown operation '$operation'. Expected 'check' or 'format'.")
            }
        }
    }

    private fun checkStyle(
        repositoryRoot: File,
        sourceFiles: List<File>,
        style: RepositoryKotlinStyle,
    ) {
        val violations =
            sourceFiles.flatMap { file ->
                style.inspect(file).map { violation ->
                    "${file.relativePathFrom(
                        repositoryRoot = repositoryRoot,
                    )}:${violation.line}:${violation.column}: " +
                        "${violation.ruleId}: ${violation.detail}"
                }
            }
        if (violations.isEmpty()) {
            println("Kotlin style is valid in ${sourceFiles.size} file(s).")
            return
        }

        System.err.println("Kotlin style violations:")
        violations.forEach { violation -> System.err.println("- $violation") }
        System.err.println("Run .\\gradlew.bat formatKotlinStyle to correct autocorrectable violations.")
        exitProcess(1)
    }

    private fun formatStyle(
        repositoryRoot: File,
        sourceFiles: List<File>,
        style: RepositoryKotlinStyle,
    ) {
        var formattedFileCount = 0
        sourceFiles.forEach { file ->
            val source = file.readText()
            val formatted =
                style.format(
                    file = file,
                )
            if (formatted != source) {
                file.writeText(formatted)
                formattedFileCount += 1
            }
        }
        println(
            "Formatted Kotlin style in $formattedFileCount of " +
                "${sourceFiles.size} file(s) under ${repositoryRoot.invariantSeparatorsPath}.",
        )
    }

    private fun File.kotlinSourceFiles(): List<File> =
        walkTopDown()
            .onEnter { directory -> directory == this || directory.name !in EXCLUDED_DIRECTORY_NAMES }
            .filter(File::isFile)
            .filter { file -> file.extension == "kt" || file.extension == "kts" }
            .sortedBy { file ->
                file.relativePathFrom(
                    repositoryRoot = this,
                )
            }.toList()

    private fun File.relativePathFrom(
        repositoryRoot: File,
    ): String =
        relativeTo(repositoryRoot).invariantSeparatorsPath

    private const val EXPECTED_ARGUMENT_COUNT = 2
    private val EXCLUDED_DIRECTORY_NAMES =
        setOf(
            ".git",
            ".gradle",
            ".idea",
            ".kotlin",
            "build",
            "node_modules",
            "tmp",
        )
}
