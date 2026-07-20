package com.marmatsan.verificationPlatform.data.git

import java.io.File

/** Verifies committed repository whitespace with Git's native diff checker. */
class GitRepositoryDiffChecker {
    /**
     * Runs `git diff --check` from [comparisonBase] through [head].
     *
     * @throws IllegalArgumentException when either revision is blank.
     * @throws IllegalStateException when Git reports whitespace errors or
     * cannot resolve the requested revisions.
     */
    fun check(
        repositoryRoot: File,
        comparisonBase: String,
        head: String,
    ) {
        require(comparisonBase.isNotBlank()) { "A comparison base is required for repository diff verification." }
        require(head.isNotBlank()) { "A head revision is required for repository diff verification." }

        val root = repositoryRoot.canonicalFile
        val safeDirectory =
            root.absolutePath.replace(
                '\\',
                '/',
            )
        val arguments =
            listOf(
                "git",
                "-c",
                "safe.directory=$safeDirectory",
                "diff",
                "--check",
                "$comparisonBase..$head",
            )
        val process =
            ProcessBuilder(arguments)
                .directory(root)
                .redirectErrorStream(true)
                .start()
        val output = process.inputStream.bufferedReader().use { reader -> reader.readText() }
        val exitCode = process.waitFor()
        check(
            exitCode == 0,
        ) {
            "Repository diff verification failed: ${output.trim()}"
        }
    }
}
