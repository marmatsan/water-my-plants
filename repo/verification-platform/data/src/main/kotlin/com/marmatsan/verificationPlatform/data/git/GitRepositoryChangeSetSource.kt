package com.marmatsan.verificationPlatform.data.git

import com.marmatsan.verificationPlatform.domain.model.RepositoryChangeSet
import java.io.File

/** Reads the committed change set used by CI without depending on a CI provider. */
class GitRepositoryChangeSetSource {
    /**
     * Resolves the committed diff ending at `HEAD` in [repositoryRoot].
     *
     * [comparisonBaseOverride] takes precedence when supplied. Otherwise the
     * adapter compares a branch with its merge base against `origin/main`, or
     * compares `main` with its first parent when `HEAD` equals `origin/main`.
     *
     * @throws IllegalStateException when Git cannot resolve the requested
     * revisions or diff.
     */
    fun read(
        repositoryRoot: File,
        comparisonBaseOverride: String? = null
    ): RepositoryChangeSet {
        val root = repositoryRoot.canonicalFile
        val head = git(
            root,
            "rev-parse",
            "HEAD"
        )
        val base = comparisonBaseOverride?.takeIf(String::isNotBlank) ?: defaultBase(
            root = root,
            head = head
        )
        val changedFiles = git(
            root,
            "diff",
            "--name-only",
            "--diff-filter=ACMRD",
            "$base..$head"
        ).lineSequence().map(
            transform = ::normalize
        ).filter(String::isNotBlank).toList()

        return RepositoryChangeSet(
            comparisonBase = base,
            head = head,
            changedFiles = changedFiles
        )
    }

    private fun defaultBase(
        root: File,
        head: String
    ): String {
        git(
            root,
            "rev-parse",
            "--verify",
            "origin/main"
        )
        val main = git(
            root,
            "rev-parse",
            "origin/main"
        )
        return if (head == main) {
            git(
                root,
                "rev-parse",
                "$head^"
            )
        } else {
            git(
                root,
                "merge-base",
                "HEAD",
                "origin/main"
            )
        }
    }

    private fun git(
        root: File,
        vararg arguments: String
    ): String {
        val safeDirectory = root.absolutePath.replace(
            '\\',
            '/'
        )
        val process = ProcessBuilder(
            listOf(
                "git",
                "-c",
                "safe.directory=$safeDirectory"
            ) + arguments
        )
            .directory(root)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        check(exitCode == 0) {
            "Failed to run git ${arguments.joinToString(" ")}: ${output.trim()}"
        }
        return output.trim()
    }

    private fun normalize(
        path: String
    ): String = path.trim().replace(
        '\\',
        '/'
    )
}
