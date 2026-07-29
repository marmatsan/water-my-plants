package com.marmatsan.figmaDocumentationSync.data.writer.generation

import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator

/** Owns deterministic filesystem replacement and portable manifest paths for runner directories. */
internal class CanonicalMcpRunnerDirectoryStore {
    /** Replaces [directory] with an empty directory. */
    fun recreate(
        directory: Path
    ) {
        if (Files.exists(directory)) {
            Files.walk(directory).use { paths ->
                paths.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists)
            }
        }
        Files.createDirectories(directory)
    }

    /** Writes every ordered source entry below [directory]. */
    fun writeSources(
        directory: Path,
        sources: Map<String, String>
    ) {
        sources.forEach { (fileName, source) ->
            Files.writeString(
                directory.resolve(fileName),
                source
            )
        }
    }

    /** Resolves [path] relative to [toolsDirectory] when both paths share a filesystem root. */
    fun portablePath(
        toolsDirectory: String,
        path: String
    ): String {
        val tools = toolsDirectory.toNormalizedPath()
        val target = path.toNormalizedPath()
        return runCatching { tools.relativize(target).toString() }
            .getOrDefault(target.toString())
            .replace(
                '\\',
                '/'
            )
    }

    private fun String.toNormalizedPath(): Path =
        Path
            .of(this)
            .toAbsolutePath()
            .normalize()
}
