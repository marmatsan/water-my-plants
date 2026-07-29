package com.marmatsan.figmaDocumentationSync.data.datasource.impact

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

internal class GitRepositoryChangeSetDataSourceTest :
    FunSpec(
        {
            test("read returns repository paths changed from origin main") {
                val repository = Files.createTempDirectory("figma-impact-git").toFile()
                try {
                    git(
                        repository,
                        "init"
                    )
                    git(
                        repository,
                        "config",
                        "user.name",
                        "Figma impact test"
                    )
                    git(
                        repository,
                        "config",
                        "user.email",
                        "figma-impact@example.invalid"
                    )
                    File(
                        repository,
                        "base.txt"
                    ).writeText("base")
                    git(
                        repository,
                        "add",
                        "."
                    )
                    git(
                        repository,
                        "commit",
                        "-m",
                        "test: base"
                    )
                    git(
                        repository,
                        "branch",
                        "-M",
                        "main"
                    )
                    git(
                        repository,
                        "remote",
                        "add",
                        "origin",
                        repository.absolutePath
                    )
                    git(
                        repository,
                        "fetch",
                        "origin",
                        "main:refs/remotes/origin/main"
                    )
                    val baseSha =
                        git(
                            repository,
                            "rev-parse",
                            "HEAD"
                        )

                    File(
                        repository,
                        "changed.txt"
                    ).writeText("changed")
                    git(
                        repository,
                        "add",
                        "."
                    )
                    git(
                        repository,
                        "commit",
                        "-m",
                        "test: change"
                    )

                    val result = GitRepositoryChangeSetDataSource().read(repository.absolutePath)

                    result.comparisonBase shouldBe baseSha
                    result.changedPaths shouldBe listOf("changed.txt")
                } finally {
                    repository.deleteRecursively()
                }
            }
        }
    )

private fun git(
    repository: File,
    vararg arguments: String
): String {
    val process =
        ProcessBuilder(listOf("git") + arguments)
            .directory(repository)
            .redirectErrorStream(true)
            .start()
    val output = process.inputStream.bufferedReader().use { reader -> reader.readText() }
    check(process.waitFor() == 0) {
        "git ${arguments.joinToString(" ")} failed: ${output.trim()}"
    }
    return output.trim()
}
