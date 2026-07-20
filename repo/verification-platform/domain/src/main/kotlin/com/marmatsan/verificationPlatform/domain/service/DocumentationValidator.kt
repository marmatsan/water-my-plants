package com.marmatsan.verificationPlatform.domain.service

import com.marmatsan.verificationPlatform.domain.model.DocumentationCoverageRule
import com.marmatsan.verificationPlatform.domain.model.DocumentationCoverageViolation
import com.marmatsan.verificationPlatform.domain.model.DocumentationRepositorySnapshot
import com.marmatsan.verificationPlatform.domain.model.DocumentationValidationResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** Validates typed Markdown, local links, canonical sources, and change coverage. */
class DocumentationValidator {
    /**
     * Validates one repository [snapshot].
     *
     * Coverage validation is enabled when [changedPaths] is not `null`. The
     * caller supplies [currentDate] so review-expiry behavior remains
     * deterministic in tests.
     *
     * @param snapshot Markdown content and repository entries to inspect.
     * @param coverageRules implementation-to-documentation mappings.
     * @param changedPaths committed paths being verified, or `null` to skip
     * coverage evaluation.
     * @param currentDate date used to calculate expired review windows.
     * @return every validation finding without provider-specific exceptions.
     */
    fun validate(
        snapshot: DocumentationRepositorySnapshot,
        coverageRules: List<DocumentationCoverageRule> = emptyList(),
        changedPaths: List<String>? = null,
        currentDate: LocalDate = LocalDate.now()
    ): DocumentationValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val validatedDocuments = mutableListOf<String>()
        val repositoryEntries = snapshot.repositoryEntries.map(::normalize).toSet()

        snapshot.documents.sortedBy { document -> document.path }.forEach { document ->
            val path = normalize(document.path)
            val expectedType = expectedType(path)
            val frontmatter = readFrontmatter(document.content)

            if (expectedType == null) {
                if (frontmatter?.metadata?.get("type") in TYPED_DOCUMENT_TYPES) {
                    errors.add("[$path] Typed document is outside its canonical directory.")
                }
            } else {
                validatedDocuments.add(path)
                if (frontmatter == null) {
                    errors.add("[$path] Typed document must start with YAML frontmatter.")
                } else {
                    validateMetadata(
                        path = path,
                        expectedType = expectedType,
                        frontmatter = frontmatter,
                        repositoryEntries = repositoryEntries,
                        currentDate = currentDate,
                        errors = errors,
                        warnings = warnings
                    )
                    validateHeadings(path, expectedType, frontmatter.body, errors)
                }
            }

            validateLinks(path, document.content, repositoryEntries, errors)
        }

        val coverageViolations = changedPaths?.let { paths ->
            validateCoverage(paths, coverageRules)
        }.orEmpty()

        return DocumentationValidationResult(
            validatedDocuments = validatedDocuments,
            errors = errors,
            warnings = warnings,
            coverageViolations = coverageViolations
        )
    }

    private fun validateMetadata(
        path: String,
        expectedType: String,
        frontmatter: Frontmatter,
        repositoryEntries: Set<String>,
        currentDate: LocalDate,
        errors: MutableList<String>,
        warnings: MutableList<String>
    ) {
        REQUIRED_METADATA_FIELDS.forEach { field ->
            if (frontmatter.metadata[field].isNullOrBlank()) {
                errors.add("[$path] Missing frontmatter field '$field'.")
            }
        }

        val actualType = frontmatter.metadata["type"]
        if (actualType != expectedType) {
            errors.add("[$path] Frontmatter type '${actualType.orEmpty()}' does not match path type '$expectedType'.")
        }

        val status = frontmatter.metadata["status"].orEmpty()
        if (status !in SUPPORTED_STATUSES) {
            errors.add("[$path] Unsupported status '$status'.")
        }

        PLACEHOLDER_FIELDS.forEach { field ->
            if (PLACEHOLDER_PATTERN.containsMatchIn(frontmatter.metadata[field].orEmpty())) {
                errors.add("[$path] Frontmatter field '$field' still contains template text.")
            }
        }

        val reviewDate = frontmatter.metadata["last-reviewed"]?.let { value ->
            try {
                LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (_: DateTimeParseException) {
                null
            }
        }
        if (reviewDate == null) {
            errors.add("[$path] last-reviewed must use YYYY-MM-DD.")
        }

        val reviewCycleDays = frontmatter.metadata["review-cycle-days"]?.toIntOrNull()
        if (reviewCycleDays == null || reviewCycleDays <= 0) {
            errors.add("[$path] review-cycle-days must be a positive integer.")
        } else if (reviewDate != null && reviewDate.plusDays(reviewCycleDays.toLong()).isBefore(currentDate)) {
            warnings.add(
                "[$path] Documentation review expired on ${reviewDate.plusDays(reviewCycleDays.toLong())}."
            )
        }

        if (frontmatter.sources.isEmpty()) {
            errors.add("[$path] At least one canonical source is required.")
        }
        if (status != "superseded") {
            frontmatter.sources.forEach { source ->
                if (!EXTERNAL_SOURCE_PATTERN.containsMatchIn(source) &&
                    !sourceExists(source, repositoryEntries)
                ) {
                    errors.add("[$path] Canonical source does not exist: $source")
                }
            }
        }
    }

    private fun validateHeadings(
        path: String,
        expectedType: String,
        body: String,
        errors: MutableList<String>
    ) {
        if (!LEVEL_ONE_HEADING_PATTERN.containsMatchIn(body)) {
            errors.add("[$path] A level-one title is required after frontmatter.")
        }
        val headings = LEVEL_TWO_HEADING_PATTERN.findAll(body)
            .map { match -> match.groups["name"]!!.value.trim().lowercase() }
            .toSet()

        if (expectedType == "runbook") {
            RUNBOOK_SECTIONS.forEach { (name, aliases) ->
                if (headings.none(aliases::contains)) {
                    errors.add("[$path] Runbook section '$name' is required.")
                }
            }
        }
        if (expectedType == "adr") {
            ADR_SECTIONS.forEach { heading ->
                if (heading !in headings) {
                    errors.add("[$path] ADR section '$heading' is required.")
                }
            }
        }
    }

    private fun validateLinks(
        documentPath: String,
        content: String,
        repositoryEntries: Set<String>,
        errors: MutableList<String>
    ) {
        MARKDOWN_LINK_PATTERN.findAll(content).forEach { match ->
            val target = match.groups["target"]!!.value.trim('<', '>')
            if (EXTERNAL_LINK_PATTERN.containsMatchIn(target)) return@forEach

            val targetPath = target.split('#', '?', limit = 2).first()
            if (targetPath.isBlank()) return@forEach

            val resolved = resolve(documentPath, targetPath)
            if (resolved == null || normalize(resolved) !in repositoryEntries) {
                errors.add("[$documentPath] Broken local Markdown link: $target")
            }
        }
    }

    private fun validateCoverage(
        changedPaths: List<String>,
        rules: List<DocumentationCoverageRule>
    ): List<DocumentationCoverageViolation> {
        val normalizedPaths = changedPaths.map(::normalize).filter(String::isNotBlank).distinct()
        return rules.mapNotNull { rule ->
            val changedSources = normalizedPaths.filter { path -> matchesAny(path, rule.sourcePaths) }
            if (changedSources.isEmpty()) return@mapNotNull null

            val documentationChanged = normalizedPaths.any { path -> matchesAny(path, rule.documentationPaths) }
            if (documentationChanged) return@mapNotNull null

            DocumentationCoverageViolation(
                rule = rule.id,
                changedSources = changedSources,
                requiredDocumentation = rule.documentationPaths
            )
        }
    }

    private fun readFrontmatter(content: String): Frontmatter? {
        val match = FRONTMATTER_PATTERN.find(content) ?: return null
        val metadata = linkedMapOf<String, String>()
        val sources = mutableListOf<String>()
        var currentKey: String? = null

        match.groups["yaml"]!!.value.lineSequence().forEach { line ->
            val keyMatch = METADATA_KEY_PATTERN.matchEntire(line)
            if (keyMatch != null) {
                val key = keyMatch.groups["key"]!!.value
                currentKey = key
                metadata[key] = keyMatch.groups["value"]?.value.orEmpty().trim().trim('"', '\'')
            } else if (currentKey == "sources") {
                SOURCE_ITEM_PATTERN.matchEntire(line)?.groups?.get("value")?.value?.let { value ->
                    sources.add(value.trim().trim('"', '\''))
                }
            }
        }

        return Frontmatter(
            metadata = metadata,
            sources = sources,
            body = content.substring(match.range.last + 1)
        )
    }

    private fun expectedType(path: String): String? = when {
        path == "docs/documentation.md" -> "standard"
        ADR_PATH_PATTERN.matches(path) -> "adr"
        path.endsWith("/README.md") -> null
        TYPED_PATH_PATTERN.containsMatchIn(path) -> when (
            TYPED_PATH_PATTERN.find(path)!!.groups["kind"]!!.value
        ) {
            "standards" -> "standard"
            "guides" -> "guide"
            "reference" -> "reference"
            "runbooks" -> "runbook"
            else -> null
        }
        else -> null
    }

    private fun sourceExists(source: String, repositoryEntries: Set<String>): Boolean {
        val normalized = normalize(source)
        return if (normalized.any { character -> character == '*' || character == '?' }) {
            repositoryEntries.any { entry -> wildcardMatches(entry, normalized) }
        } else {
            normalized in repositoryEntries
        }
    }

    private fun matchesAny(path: String, patterns: List<String>): Boolean =
        patterns.any { pattern -> wildcardMatches(path, normalize(pattern)) }

    private fun wildcardMatches(path: String, pattern: String): Boolean {
        val regex = buildString {
            append('^')
            pattern.forEach { character ->
                when (character) {
                    '*' -> append(".*")
                    '?' -> append('.')
                    else -> append(Regex.escape(character.toString()))
                }
            }
            append('$')
        }
        return Regex(regex, RegexOption.IGNORE_CASE).matches(normalize(path))
    }

    private fun resolve(documentPath: String, targetPath: String): String? {
        val parts = if (targetPath.startsWith('/')) {
            targetPath.trimStart('/').split('/')
        } else {
            documentPath.substringBeforeLast('/', "").split('/').filter(String::isNotEmpty) +
                targetPath.split('/')
        }
        val resolved = mutableListOf<String>()
        parts.forEach { part ->
            when (part) {
                "", "." -> Unit
                ".." -> if (resolved.isEmpty()) return null else resolved.removeLast()
                else -> resolved.add(part)
            }
        }
        return resolved.joinToString("/")
    }

    private fun normalize(path: String): String = path.trim().replace('\\', '/').trimStart('.', '/')

    private data class Frontmatter(
        val metadata: Map<String, String>,
        val sources: List<String>,
        val body: String
    )

    private companion object {
        val TYPED_DOCUMENT_TYPES = setOf("standard", "guide", "runbook", "reference", "adr")
        val SUPPORTED_STATUSES = setOf("draft", "active", "accepted", "deprecated", "superseded")
        val REQUIRED_METADATA_FIELDS = listOf(
            "title",
            "type",
            "scope",
            "owner",
            "status",
            "last-reviewed",
            "review-cycle-days"
        )
        val PLACEHOLDER_FIELDS = listOf("title", "scope", "owner")
        val RUNBOOK_SECTIONS = listOf(
            "Purpose" to setOf("purpose"),
            "Prerequisites" to setOf("prerequisites"),
            "Verification" to setOf("verification", "verify"),
            "Recovery" to setOf("recovery", "failure recovery"),
            "Prohibited Actions" to setOf("prohibited actions"),
            "Sources" to setOf("sources")
        )
        val ADR_SECTIONS = setOf("context", "decision", "consequences", "alternatives", "supersession")
        val FRONTMATTER_PATTERN = Regex(
            """\A---\r?\n(?<yaml>.*?)\r?\n---(?:\r?\n|\z)""",
            RegexOption.DOT_MATCHES_ALL
        )
        val METADATA_KEY_PATTERN = Regex("""(?<key>[a-z][a-z0-9-]*):(?:\s*(?<value>.*))?""")
        val SOURCE_ITEM_PATTERN = Regex("""\s+-\s+(?<value>.+?)\s*""")
        val PLACEHOLDER_PATTERN = Regex("replace|repository-or|stable-area|placeholder", RegexOption.IGNORE_CASE)
        val EXTERNAL_SOURCE_PATTERN = Regex("^(https?:|generated:)")
        val LEVEL_ONE_HEADING_PATTERN = Regex("""^#\s+\S""", setOf(RegexOption.MULTILINE))
        val LEVEL_TWO_HEADING_PATTERN = Regex("""^##\s+(?<name>.+?)\s*$""", setOf(RegexOption.MULTILINE))
        val MARKDOWN_LINK_PATTERN = Regex("""(?<!!)\[[^]]*]\((?<target>[^ )]+)""")
        val EXTERNAL_LINK_PATTERN = Regex("^(https?:|mailto:|#)")
        val ADR_PATH_PATTERN = Regex("""docs/decisions/adr-[0-9]{4}-[a-z0-9]+(?:-[a-z0-9]+)*\.md""")
        val TYPED_PATH_PATTERN = Regex("""(?:^|/)docs/(?<kind>standards|guides|reference|runbooks)/.+\.md$""")
    }
}
