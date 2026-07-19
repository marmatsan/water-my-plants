package com.marmatsan.figmaDocumentationSync.teamcityAdapter.configuration

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiJob
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiTrigger
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiVcsRoot
import java.io.File
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import org.w3c.dom.Document
import org.w3c.dom.Element

/** Reads the effective YAML and XML emitted by the TeamCity Kotlin DSL. */
class TeamCityGeneratedConfigurationReader {
    fun read(directory: File): CiConfiguration {
        require(directory.isDirectory) {
            "TeamCity generated configuration directory does not exist: ${directory.path}"
        }

        val pipelineDirectories = directory
            .walkTopDown()
            .filter { file -> file.isFile && file.name == PIPELINE_FILE_NAME }
            .map(File::getParentFile)
            .toList()

        return CiConfiguration(
            pipelines = pipelineDirectories
                .map(::readPipeline)
                .sortedBy(CiPipeline::id),
            vcsRoots = directory
                .walkTopDown()
                .filter { file ->
                    file.isFile &&
                        file.extension == XML_EXTENSION &&
                        file.parentFile.name == VCS_ROOTS_DIRECTORY_NAME
                }
                .map(::readVcsRoot)
                .toList()
                .sortedBy(CiVcsRoot::id)
        )
    }

    private fun readPipeline(directory: File): CiPipeline {
        val projectDocument = readXml(directory.resolve(PROJECT_CONFIG_FILE_NAME))
        val buildTypeFiles = directory.resolve(BUILD_TYPES_DIRECTORY_NAME)
            .listFiles { file -> file.isFile && file.extension == XML_EXTENSION }
            ?.sortedBy(File::getName)
            .orEmpty()

        return CiPipeline(
            id = directory.name,
            name = projectDocument.documentElement
                .getElementsByTagName(NAME_ELEMENT)
                .item(0)
                ?.textContent
                ?.trim()
                .orEmpty(),
            triggers = buildTypeFiles.flatMap { file -> readTriggers(readXml(file)) },
            jobs = readJobs(directory.resolve(PIPELINE_FILE_NAME))
        )
    }

    private fun readTriggers(document: Document): List<CiTrigger> {
        val triggers = document.getElementsByTagName(BUILD_TRIGGER_ELEMENT)
        return (0 until triggers.length).map { index ->
            val trigger = triggers.item(index) as Element
            val parameters = trigger
                .getElementsByTagName(PARAM_ELEMENT)
                .let { nodes ->
                    (0 until nodes.length).associate { parameterIndex ->
                        val parameter = nodes.item(parameterIndex) as Element
                        parameter.getAttribute(NAME_ATTRIBUTE) to parameter.getAttribute(VALUE_ATTRIBUTE)
                    }
                }

            when (val type = trigger.getAttribute(TYPE_ATTRIBUTE)) {
                VCS_TRIGGER_TYPE -> CiTrigger(
                    type = CiTrigger.Type.Vcs,
                    branchFilter = parameters[BRANCH_FILTER_PARAMETER],
                    dependencyPipelineId = null,
                    afterSuccessfulBuildOnly = null
                )

                BUILD_DEPENDENCY_TRIGGER_TYPE -> CiTrigger(
                    type = CiTrigger.Type.PipelineFinish,
                    branchFilter = parameters[BRANCH_FILTER_PARAMETER],
                    dependencyPipelineId = parameters[DEPENDS_ON_PARAMETER],
                    afterSuccessfulBuildOnly = parameters[AFTER_SUCCESS_PARAMETER]?.toBooleanStrict()
                )

                SCHEDULING_TRIGGER_TYPE -> CiTrigger(
                    type = CiTrigger.Type.Schedule,
                    branchFilter = parameters[BRANCH_FILTER_PARAMETER],
                    dependencyPipelineId = null,
                    afterSuccessfulBuildOnly = null
                )

                else -> error("Unsupported TeamCity trigger type '$type'")
            }
        }
    }

    private fun readJobs(file: File): List<CiJob> {
        val root = readYaml(file)
        return root.requiredMap(JOBS_KEY).map { (jobId, value) ->
            val job = value.asStringMap("job '$jobId'")
            CiJob(
                id = jobId,
                name = job.requiredString(NAME_KEY),
                steps = job.optionalList(STEPS_KEY).map(::readStep),
                repositoryIds = job.optionalList(REPOSITORIES_KEY).map { repository ->
                    repository.asStringMap("repository").keys.single()
                },
                artifacts = job.optionalList(FILES_PUBLICATION_KEY).map(::readArtifact),
                dependencies = job.optionalList(DEPENDENCIES_KEY).map(::readDependency),
                publishedChecks = job.optionalList(FEATURES_KEY)
                    .map { feature -> feature.asStringMap("feature") }
                    .filter { feature -> feature.optionalString(TYPE_KEY) == STATUS_PUBLISHER_TYPE }
                    .map { feature ->
                        CiJob.PublishedCheck(
                            name = feature.requiredString(CHECK_NAME_KEY)
                        )
                    }
            )
        }
    }

    private fun readStep(value: Any?): CiJob.Step {
        val step = value.asStringMap("step")
        return CiJob.Step(
            id = step.requiredString(ID_KEY),
            name = step.requiredString(NAME_KEY),
            command = step.requiredString(SCRIPT_CONTENT_KEY)
        )
    }

    private fun readArtifact(value: Any?): CiJob.Artifact {
        val artifact = value.asStringMap("artifact")
        return CiJob.Artifact(
            path = artifact.requiredString(PATH_KEY),
            publish = artifact.requiredBoolean(PUBLISH_ARTIFACT_KEY),
            shareWithJobs = artifact.requiredBoolean(SHARE_WITH_JOBS_KEY)
        )
    }

    private fun readDependency(value: Any?): CiJob.Dependency {
        val dependency = value.asStringMap("dependency")
        require(dependency.size == 1) { "Expected one TeamCity dependency per list item" }
        val (jobId, configurationValue) = dependency.entries.single()
        val configuration = configurationValue.asStringMap("dependency '$jobId'")
        return CiJob.Dependency(
            jobId = jobId,
            artifactPaths = configuration.optionalList(FILES_KEY).map { artifactPath ->
                artifactPath as? String ?: error("Expected TeamCity dependency artifact path")
            }
        )
    }

    private fun readVcsRoot(file: File): CiVcsRoot {
        val document = readXml(file)
        val root = document.documentElement
        val parameters = root.getElementsByTagName(PARAM_ELEMENT).let { nodes ->
            (0 until nodes.length).associate { index ->
                val parameter = nodes.item(index) as Element
                parameter.getAttribute(NAME_ATTRIBUTE) to parameter.getAttribute(VALUE_ATTRIBUTE)
                    .ifEmpty { parameter.textContent.trim() }
            }
        }

        return CiVcsRoot(
            id = file.nameWithoutExtension,
            name = root.getElementsByTagName(NAME_ELEMENT).item(0).textContent.trim(),
            url = parameters.getValue(URL_PARAMETER),
            defaultBranchRef = parameters.getValue(BRANCH_PARAMETER),
            branchSpec = parameters.getValue(BRANCH_SPEC_PARAMETER)
                .lineSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .toList()
        )
    }

    private fun readYaml(file: File): Map<String, Any?> {
        val settings = LoadSettings.builder()
            .setLabel(file.path)
            .build()
        return file.inputStream().use { input ->
            Load(settings).loadFromInputStream(input)
        }.asStringMap(file.name)
    }

    private fun readXml(file: File): Document {
        require(file.isFile) { "TeamCity generated file does not exist: ${file.path}" }
        val factory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
            isXIncludeAware = false
            isExpandEntityReferences = false
        }
        return factory.newDocumentBuilder().parse(file)
    }

    private fun Any?.asStringMap(context: String): Map<String, Any?> {
        val source = this as? Map<*, *> ?: error("Expected YAML mapping for $context")
        return source.entries.associate { (key, value) ->
            val stringKey = key as? String ?: error("Expected string key in $context")
            stringKey to value
        }
    }

    private fun Map<String, Any?>.requiredMap(key: String): Map<String, Any?> =
        get(key).asStringMap(key)

    private fun Map<String, Any?>.optionalList(key: String): List<Any?> =
        when (val value = get(key)) {
            null -> emptyList()
            is List<*> -> value
            else -> error("Expected YAML list '$key'")
        }

    private fun Map<String, Any?>.requiredString(key: String): String =
        get(key) as? String ?: error("Expected YAML string '$key'")

    private fun Map<String, Any?>.optionalString(key: String): String? =
        get(key)?.let { value -> value as? String ?: error("Expected YAML string '$key'") }

    private fun Map<String, Any?>.requiredBoolean(key: String): Boolean =
        get(key) as? Boolean ?: error("Expected YAML boolean '$key'")

    private companion object {
        const val PIPELINE_FILE_NAME = "pipeline.yml"
        const val PROJECT_CONFIG_FILE_NAME = "project-config.xml"
        const val BUILD_TYPES_DIRECTORY_NAME = "buildTypes"
        const val VCS_ROOTS_DIRECTORY_NAME = "vcsRoots"
        const val XML_EXTENSION = "xml"
        const val NAME_ELEMENT = "name"
        const val BUILD_TRIGGER_ELEMENT = "build-trigger"
        const val PARAM_ELEMENT = "param"
        const val NAME_ATTRIBUTE = "name"
        const val VALUE_ATTRIBUTE = "value"
        const val TYPE_ATTRIBUTE = "type"
        const val VCS_TRIGGER_TYPE = "vcsTrigger"
        const val BUILD_DEPENDENCY_TRIGGER_TYPE = "buildDependencyTrigger"
        const val SCHEDULING_TRIGGER_TYPE = "schedulingTrigger"
        const val BRANCH_FILTER_PARAMETER = "branchFilter"
        const val DEPENDS_ON_PARAMETER = "dependsOn"
        const val AFTER_SUCCESS_PARAMETER = "afterSuccessfulBuildOnly"
        const val URL_PARAMETER = "url"
        const val BRANCH_PARAMETER = "branch"
        const val BRANCH_SPEC_PARAMETER = "branchSpec"
        const val JOBS_KEY = "jobs"
        const val NAME_KEY = "name"
        const val STEPS_KEY = "steps"
        const val REPOSITORIES_KEY = "repositories"
        const val FILES_PUBLICATION_KEY = "files-publication"
        const val DEPENDENCIES_KEY = "dependencies"
        const val FEATURES_KEY = "features"
        const val TYPE_KEY = "type"
        const val ID_KEY = "id"
        const val SCRIPT_CONTENT_KEY = "script-content"
        const val PATH_KEY = "path"
        const val PUBLISH_ARTIFACT_KEY = "publish-artifact"
        const val SHARE_WITH_JOBS_KEY = "share-with-jobs"
        const val FILES_KEY = "files"
        const val STATUS_PUBLISHER_TYPE = "commit-status-publisher"
        const val CHECK_NAME_KEY = "build_custom_name"
    }
}
