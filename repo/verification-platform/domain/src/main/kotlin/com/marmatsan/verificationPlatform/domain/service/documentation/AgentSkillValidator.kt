package com.marmatsan.verificationPlatform.domain.service.documentation

/** Validates repository-scoped skill identity and trigger metadata. */
internal class AgentSkillValidator {
    /** Validates one `.agents/skills/<name>/SKILL.md` manifest when [path] selects that contract. */
    fun validate(
        path: String,
        frontmatter: DocumentationFrontmatter?,
        findings: DocumentationFindings
    ) {
        if (!path.startsWith(SKILL_ROOT) || !path.endsWith(SKILL_FILENAME)) return
        val match = SKILL_PATH_PATTERN.matchEntire(path)
        if (match == null) {
            findings.errors += "[$path] Skill must use .agents/skills/<kebab-case-name>/SKILL.md."
            return
        }
        if (frontmatter == null) {
            findings.errors += "[$path] Skill must start with YAML frontmatter."
            return
        }
        val metadata = frontmatter.metadata
        REQUIRED_FIELDS.forEach { field ->
            if (metadata[field].isNullOrBlank()) {
                findings.errors += "[$path] Missing skill frontmatter field '$field'."
            }
        }
        val expectedName = match.groups["name"]!!.value
        val actualName = metadata["name"].orEmpty()
        if (actualName.isNotBlank() && actualName != expectedName) {
            findings.errors +=
                "[$path] Skill name '$actualName' does not match its directory '$expectedName'."
        }
        val unsupportedFields = metadata.keys - SUPPORTED_FIELDS
        unsupportedFields.sorted().forEach { field ->
            findings.errors += "[$path] Unsupported skill frontmatter field '$field'."
        }
    }

    private companion object {
        const val SKILL_ROOT = ".agents/skills/"
        const val SKILL_FILENAME = "/SKILL.md"
        val SKILL_PATH_PATTERN =
            Regex(
                """\.agents/skills/(?<name>[a-z0-9]+(?:-[a-z0-9]+)*)/SKILL\.md"""
            )
        val REQUIRED_FIELDS =
            setOf(
                "name",
                "description"
            )
        val SUPPORTED_FIELDS = REQUIRED_FIELDS
    }
}
