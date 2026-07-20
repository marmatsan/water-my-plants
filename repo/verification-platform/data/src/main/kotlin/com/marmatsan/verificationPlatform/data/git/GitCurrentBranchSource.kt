package com.marmatsan.verificationPlatform.data.git

import java.io.File

/** Reads the current developer-owned Git branch from a repository checkout. */
class GitCurrentBranchSource {
    /**
     * Returns [branchOverride] when provided, otherwise resolves symbolic
     * `HEAD` in [repositoryRoot].
     *
     * CI adapters provide the server-resolved logical branch name so detached
     * pull request checkouts can still be classified without provider knowledge
     * in domain.
     *
     * @throws IllegalStateException when neither input can identify a branch.
     */
    fun read(repositoryRoot: File, branchOverride: String? = null): String {
        branchOverride?.trim()?.takeIf(String::isNotEmpty)?.let { branch ->
            return branch
        }

        val root = repositoryRoot.canonicalFile
        return gitOrNull(root, "symbolic-ref", "--quiet", "--short", "HEAD")
            ?.takeIf(String::isNotBlank)
            ?: error(
                "Cannot resolve the Git branch from detached HEAD. " +
                    "Provide gitWorkflowBranch or GIT_WORKFLOW_BRANCH."
            )
    }

    private fun gitOrNull(root: File, vararg arguments: String): String? {
        val safeDirectory = root.absolutePath.replace('\\', '/')
        val process = ProcessBuilder(listOf("git", "-c", "safe.directory=$safeDirectory") + arguments)
            .directory(root)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { reader -> reader.readText() }
        return if (process.waitFor() == 0) output.trim() else null
    }
}
