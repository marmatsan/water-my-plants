package com.marmatsan.figmaDesignSync.domain.model.figma

/**
 * Stable reference to a Figma node used by generated sync metadata.
 *
 * [fileKey] identifies the Figma file and [nodeId] identifies the concrete
 * node inside that file. The domain keeps this as plain data so it does not
 * depend on Figma API clients.
 *
 * Example:
 * ```
 * FigmaNodeReference(
 *     fileKey = "exampleFileKey",
 *     nodeId = "123:456"
 * )
 * ```
 *
 * @property fileKey Figma design file key.
 * @property nodeId Figma node id inside [fileKey].
 */
data class FigmaNodeReference(
    val fileKey: String,
    val nodeId: String
)
