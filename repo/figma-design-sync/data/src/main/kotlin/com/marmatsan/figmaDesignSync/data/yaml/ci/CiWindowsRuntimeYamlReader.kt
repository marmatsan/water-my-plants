package com.marmatsan.figmaDesignSync.data.yaml.ci

import com.marmatsan.figmaDesignSync.domain.model.ci.CiWindowsRuntime
import me.tatarka.inject.annotations.Inject
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.io.File
import java.time.LocalDate

/**
 * Parses the repository-owned Windows CI runtime YAML.
 */
@Inject
class CiWindowsRuntimeYamlReader {
    fun read(file: File): CiWindowsRuntime {
        val settings = LoadSettings.builder()
            .setLabel(file.path)
            .build()
        val root = file.inputStream().use { input ->
            Load(settings).loadFromInputStream(input)
        }.asStringMap("root")
        val validation = root.requiredMap("validation")

        return CiWindowsRuntime(
            schemaVersion = root.requiredInt("schemaVersion"),
            validation = CiWindowsRuntime.Validation(
                lastValidatedOn = LocalDate.parse(validation.requiredString("lastValidatedOn")),
                warnAfterDays = validation.requiredInt("warnAfterDays")
            ),
            platform = root.requiredString("platform"),
            services = root.requiredList("services").map(::readService)
        )
    }

    private fun readService(value: Any?): CiWindowsRuntime.Service {
        val service = value.asStringMap("service")
        return CiWindowsRuntime.Service(
            id = service.requiredString("id"),
            name = service.requiredString("name"),
            description = service.requiredString("description"),
            service = service.requiredString("service"),
            startup = service.requiredString("startup"),
            identity = service.requiredString("identity")
        )
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

    private fun Map<String, Any?>.requiredList(key: String): List<Any?> =
        get(key) as? List<*> ?: error("Expected YAML list '$key'")

    private fun Map<String, Any?>.requiredString(key: String): String =
        get(key) as? String ?: error("Expected YAML string '$key'")

    private fun Map<String, Any?>.requiredInt(key: String): Int =
        (get(key) as? Number)?.toInt() ?: error("Expected YAML integer '$key'")
}
