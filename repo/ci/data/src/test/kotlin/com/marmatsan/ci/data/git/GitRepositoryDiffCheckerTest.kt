package com.marmatsan.ci.data.git

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import java.io.File
import java.nio.file.Files

class GitRepositoryDiffCheckerTest : FunSpec({
    test("accepts clean commits and rejects trailing whitespace") {
        val root = Files.createTempDirectory("ci-git-diff-checker").toFile()
        try {
            git(root, "init")
            git(root, "config", "user.email", "ci@example.invalid")
            git(root, "config", "user.name", "CI Test")

            root.resolve("example.txt").writeText("baseline\n")
            commit(root, "baseline")
            val baseline = git(root, "rev-parse", "HEAD")

            root.resolve("example.txt").writeText("clean\n")
            commit(root, "clean")
            val cleanHead = git(root, "rev-parse", "HEAD")

            shouldNotThrowAny {
                GitRepositoryDiffChecker().check(root, baseline, cleanHead)
            }

            root.resolve("example.txt").writeText("trailing whitespace   \n")
            commit(root, "invalid")
            val invalidHead = git(root, "rev-parse", "HEAD")

            shouldThrow<IllegalStateException> {
                GitRepositoryDiffChecker().check(root, cleanHead, invalidHead)
            }
        } finally {
            root.deleteRecursively()
        }
    }
}) {
    companion object {
        private fun commit(root: File, message: String) {
            git(root, "add", ".")
            git(root, "commit", "-m", message)
        }

        private fun git(root: File, vararg arguments: String): String {
            val process = ProcessBuilder(listOf("git") + arguments)
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
