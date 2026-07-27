package com.marmatsan.verificationPlatform.data.documentation

import com.marmatsan.verificationPlatform.domain.model.documentation.DocumentationFile
import com.marmatsan.verificationPlatform.domain.model.documentation.DocumentationRepositorySnapshot
import java.io.File

/** Reads the Markdown and path snapshot required by documentation validation. */
class FileSystemDocumentationSource {
    /**
     * Reads validation inputs beneath [repositoryRoot].
     *
     * Generated, dependency, temporary, and Git directories are pruned before
     * traversal. Documentation templates remain repository entries but are not
     * validated as authored documents.
     *
     * @throws IllegalArgumentException when [repositoryRoot] is not a directory.
     */
    fun read(
        repositoryRoot: File,
    ): DocumentationRepositorySnapshot {
        val root = repositoryRoot.canonicalFile
        require(root.isDirectory) { "Documentation repository root is not a directory: $root" }

        val entries = mutableSetOf<String>()
        val documents = mutableListOf<DocumentationFile>()
        root
            .walkTopDown()
            .onEnter { directory ->
                !isExcluded(
                    root = root,
                    directory = directory,
                )
            }.forEach { entry ->
                if (entry == root) return@forEach
                val path = entry.relativeTo(root).invariantSeparatorsPath
                entries.add(
                    element = path,
                )
                if (entry.isFile &&
                    entry.extension.equals(
                        "md",
                        ignoreCase = true,
                    ) &&
                    !path.startsWith(
                        prefix = "docs/templates/",
                    ) &&
                    !path.contains("/docs/templates/")
                ) {
                    documents.add(
                        element =
                            DocumentationFile(
                                path = path,
                                content = entry.readText(),
                            ),
                    )
                }
            }

        return DocumentationRepositorySnapshot(
            documents = documents.sortedBy(DocumentationFile::path),
            repositoryEntries = entries,
        )
    }

    private fun isExcluded(
        root: File,
        directory: File,
    ): Boolean {
        if (directory == root) return false
        return directory
            .relativeTo(root)
            .invariantSeparatorsPath
            .split('/')
            .any { segment -> segment in EXCLUDED_DIRECTORIES }
    }

    private companion object {
        val EXCLUDED_DIRECTORIES =
            setOf(
                ".git",
                "build",
                "node_modules",
                "tmp",
            )
    }
}
