package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Parent section and variable folder used by one dependency version group.
 *
 * @property parentNodeId Figma parent section that receives the version group.
 * @property variableFolder variable folder containing the group's alias and value modes.
 */
data class FigmaVersionSectionTarget(
    val parentNodeId: String,
    val variableFolder: String
)
