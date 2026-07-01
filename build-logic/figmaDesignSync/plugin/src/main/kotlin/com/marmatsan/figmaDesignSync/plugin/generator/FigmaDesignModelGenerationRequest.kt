package com.marmatsan.figmaDesignSync.plugin.generator

import java.io.File
import java.time.Instant

/**
 * Complete input snapshot required to generate `design-model.json`.
 *
 * File and directory values live in the plugin layer because they are Gradle
 * task inputs. The generator immediately translates them to domain source
 * objects before crossing into ports.
 *
 * @property branch Current Git branch recorded in the generated metadata.
 * @property gitSha Current Git commit recorded in the generated metadata.
 * @property generatedAt Timestamp written for traceability.
 * @property versionsFile Source `build-logic/versions.properties` file.
 * @property rootSettingsFile Root `settings.gradle.kts`.
 * @property buildLogicSettingsFile Included-build `build-logic/settings.gradle.kts`.
 * @property projectRootDirectory Repository root.
 * @property buildLogicRootDirectory `build-logic` included-build root.
 */
internal data class FigmaDesignModelGenerationRequest(
    val branch: String,
    val gitSha: String,
    val generatedAt: Instant,
    val versionsFile: File,
    val rootSettingsFile: File,
    val buildLogicSettingsFile: File,
    val projectRootDirectory: File,
    val buildLogicRootDirectory: File
)
