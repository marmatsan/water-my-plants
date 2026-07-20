package com.marmatsan.verificationPlatform.data.git

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

class GitCurrentBranchSourceTest : FunSpec(
    {
    test("reads the symbolic branch from a local checkout") {
        val root = Files.createTempDirectory("git-current-branch").toFile()
        try {
            git(
                root,
                "init"
            )
            git(
                root,
                "switch",
                "-c",
                "feature/plant-reminders"
            )

            GitCurrentBranchSource().read(root) shouldBe "feature/plant-reminders"
        } finally {
            root.deleteRecursively()
        }
    }

    test("uses a CI branch override for detached provider checkouts") {
        val root = Files.createTempDirectory("git-current-branch-override").toFile()
        try {
            GitCurrentBranchSource().read(
                root,
                "refs/pull/96/head"
            ) shouldBe
                "refs/pull/96/head"
        } finally {
            root.deleteRecursively()
        }
    }
}
) {
    companion object {
        private fun git(
            root: File,
            vararg arguments: String
        ) {
            val process = ProcessBuilder(listOf("git") + arguments)
                .directory(root)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { reader -> reader.readText() }
            check(process.waitFor() == 0) {
                "Git command failed: git ${arguments.joinToString(" ")}\n${output.trim()}"
            }
        }
    }
}
