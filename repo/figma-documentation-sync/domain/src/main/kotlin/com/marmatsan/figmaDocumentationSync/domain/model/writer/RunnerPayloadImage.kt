package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Uploaded PNG asset that carries the canonical model and writer payload.
 *
 * @property fileName canonical payload image name.
 * @property byteLength encoded PNG byte length.
 * @property sha256 payload image hash.
 * @property textKeyword PNG text keyword containing the transport payload.
 */
data class RunnerPayloadImage(
    val fileName: String,
    val byteLength: Int,
    val sha256: String,
    val textKeyword: String
)
