package com.marmatsan.verificationPlatform.data.git

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import java.io.File
import java.nio.file.Files

class GitRepositoryDiffCheckerTest :
    FunSpec(
        {
            test("accepts clean commits and rejects trailing whitespace") {
                val root = Files.createTempDirectory("ci-git-diff-checker").toFile()
                try {
                    git(
                        root,
                        "init",
                    )
                    git(
                        root,
                        "config",
                        "user.email",
                        "ci@example.invalid",
                    )
                    git(
                        root,
                        "config",
                        "user.name",
                        "CI Test",
                    )

                    root
                        .resolve(
                            relative = "example.txt",
                        ).writeText("baseline\n")
                    commit(
                        root = root,
                        message = "baseline",
                    )
                    val baseline =
                        git(
                            root,
                            "rev-parse",
                            "HEAD",
                        )

                    root
                        .resolve(
                            relative = "example.txt",
                        ).writeText("clean\n")
                    commit(
                        root = root,
                        message = "clean",
                    )
                    val cleanHead =
                        git(
                            root,
                            "rev-parse",
                            "HEAD",
                        )

                    shouldNotThrowAny {
                        GitRepositoryDiffChecker().check(
                            repositoryRoot = root,
                            comparisonBase = baseline,
                            head = cleanHead,
                        )
                    }

                    root
                        .resolve(
                            relative = "example.txt",
                        ).writeText("trailing whitespace   \n")
                    commit(
                        root = root,
                        message = "invalid",
                    )
                    val invalidHead =
                        git(
                            root,
                            "rev-parse",
                            "HEAD",
                        )

                    shouldThrow<IllegalStateException> {
                        GitRepositoryDiffChecker().check(
                            repositoryRoot = root,
                            comparisonBase = cleanHead,
                            head = invalidHead,
                        )
                    }
                } finally {
                    root.deleteRecursively()
                }
            }
        },
    ) {
    companion object {
        private fun commit(
            root: File,
            message: String,
        ) {
            git(
                root,
                "add",
                ".",
            )
            git(
                root,
                "commit",
                "-m",
                message,
            )
        }

        private fun git(
            root: File,
            vararg arguments: String,
        ): String {
            val process =
                ProcessBuilder(listOf("git") + arguments)
                    .directory(root)
                    .redirectErrorStream(true)
                    .start()
            val output = process.inputStream.bufferedReader().use { reader -> reader.readText() }
            check(process.waitFor() == 0) {
                "Git command failed: git ${arguments.joinToString(" ")}\n${output.trim()}"
            }
            return output.trim()
        }
    }
}
