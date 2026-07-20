package com.marmatsan.figmaDocumentationSync.data.datasource.impact

import com.marmatsan.figmaDocumentationSync.domain.model.impact.RepositoryChangeSet
import com.marmatsan.figmaDocumentationSync.domain.port.impact.RepositoryChangeSetPort
import me.tatarka.inject.annotations.Inject
import java.io.File

/** Git adapter that compares the current checkout with `origin/main`. */
@Inject
class GitRepositoryChangeSetDataSource : RepositoryChangeSetPort {
    override fun read(
        repositoryRootPath: String,
    ): RepositoryChangeSet {
        val repositoryRoot = File(repositoryRootPath).canonicalFile
        git(
            repositoryRoot,
            "rev-parse",
            "--verify",
            "origin/main",
        )

        val head =
            git(
                repositoryRoot,
                "rev-parse",
                "HEAD",
            )
        val main =
            git(
                repositoryRoot,
                "rev-parse",
                "origin/main",
            )
        val base =
            if (head == main) {
                git(
                    repositoryRoot,
                    "rev-parse",
                    "$head^",
                )
            } else {
                git(
                    repositoryRoot,
                    "merge-base",
                    "HEAD",
                    "origin/main",
                )
            }
        val paths =
            git(
                repositoryRoot,
                "diff",
                "--name-only",
                "--diff-filter=ACMR",
                "$base..$head",
            ).lineSequence()
                .map(
                    transform = ::normalizePath,
                ).filter(String::isNotBlank)
                .toList()

        return RepositoryChangeSet(
            comparisonBase = base,
            changedPaths = paths,
        )
    }

    private fun git(
        repositoryRoot: File,
        vararg arguments: String,
    ): String {
        val safeDirectory =
            repositoryRoot.absolutePath.replace(
                '\\',
                '/',
            )
        val process =
            ProcessBuilder(
                listOf(
                    "git",
                    "-c",
                    "safe.directory=$safeDirectory",
                ) + arguments,
            ).directory(repositoryRoot)
                .redirectErrorStream(true)
                .start()
        val output = process.inputStream.bufferedReader().use { reader -> reader.readText() }
        val exitCode = process.waitFor()
        check(exitCode == 0) {
            "Failed to run git ${arguments.joinToString(" ")}: ${output.trim()}"
        }
        return output.trim()
    }

    private fun normalizePath(
        path: String,
    ): String =
        path.trim().replace(
            '\\',
            '/',
        )
}
