package com.marmatsan.verificationPlatform.data.documentation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import java.nio.file.Files

class FileSystemDocumentationSourceTest : FunSpec(
    {
    test("reads authored Markdown and excludes templates and generated trees") {
        val root = Files.createTempDirectory("ci-documentation-source").toFile()
        try {
            root.resolve("docs/standards/example.md").apply {
                parentFile.mkdirs()
                writeText("# Standard")
            }
            root.resolve("docs/templates/standard.md").apply {
                parentFile.mkdirs()
                writeText("# Template")
            }
            root.resolve("module/build/generated.md").apply {
                parentFile.mkdirs()
                writeText("# Generated")
            }
            root.resolve("source.txt").writeText("source")

            val snapshot = FileSystemDocumentationSource().read(root)

            snapshot.documents.map { document -> document.path } shouldContain "docs/standards/example.md"
            snapshot.documents.map { document -> document.path } shouldNotContain "docs/templates/standard.md"
            snapshot.repositoryEntries shouldContain "source.txt"
            snapshot.repositoryEntries shouldNotContain "module/build/generated.md"
        } finally {
            root.deleteRecursively()
        }
    }
}
)
