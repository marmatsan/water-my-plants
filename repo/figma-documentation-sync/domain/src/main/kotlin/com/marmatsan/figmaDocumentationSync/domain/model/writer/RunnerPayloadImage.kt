package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Uploaded PNG asset that carries the canonical model and writer payload. */
data class RunnerPayloadImage(
    val fileName: String,
    val byteLength: Int,
    val sha256: String,
    val textKeyword: String,
)
