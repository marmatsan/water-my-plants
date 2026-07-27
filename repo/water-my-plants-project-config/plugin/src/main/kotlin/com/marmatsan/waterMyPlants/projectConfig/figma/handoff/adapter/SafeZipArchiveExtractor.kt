package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.adapter

import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.port.ArtifactArchiveExtractor
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream

/** Extracts ZIP archives while preventing entries from escaping the destination root. */
internal class SafeZipArchiveExtractor : ArtifactArchiveExtractor {
    /** Extracts [archive] after normalizing and validating every entry below [destination]. */
    override fun extract(
        archive: File,
        destination: File,
    ) {
        val root = destination.toPath().toAbsolutePath().normalize()
        Files.createDirectories(root)
        ZipInputStream(archive.inputStream().buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val target = root.resolve(entry.name).normalize()
                require(target.startsWith(root)) { "Unsafe ZIP entry '${entry.name}'." }
                if (entry.isDirectory) {
                    Files.createDirectories(target)
                } else {
                    Files.createDirectories(target.parent)
                    Files.copy(
                        zip,
                        target,
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }
}
