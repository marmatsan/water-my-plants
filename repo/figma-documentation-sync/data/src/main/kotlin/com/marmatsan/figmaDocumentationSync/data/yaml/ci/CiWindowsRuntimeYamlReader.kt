package com.marmatsan.figmaDocumentationSync.data.yaml.ci

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime
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
    /** Reads and validates the Windows runtime YAML [file]. */
    fun read(
        file: File,
    ): CiWindowsRuntime {
        val settings =
            LoadSettings
                .builder()
                .setLabel(file.path)
                .build()
        val root =
            file
                .inputStream()
                .use { input ->
                    Load(settings).loadFromInputStream(input)
                }.asStringMap(
                    context = "root",
                )
        val validation =
            root.requiredMap(
                key = "validation",
            )

        return CiWindowsRuntime(
            schemaVersion = root.requiredInt("schemaVersion"),
            validation =
                CiWindowsRuntime.Validation(
                    lastValidatedOn =
                        LocalDate.parse(
                            validation.requiredString("lastValidatedOn"),
                        ),
                    warnAfterDays = validation.requiredInt("warnAfterDays"),
                ),
            platform = root.requiredString("platform"),
            services =
                root
                    .requiredList(
                        key = "services",
                    ).map(
                        transform = ::readService,
                    ),
        )
    }

    private fun readService(
        value: Any?,
    ): CiWindowsRuntime.Service {
        val service =
            value.asStringMap(
                context = "service",
            )
        return CiWindowsRuntime.Service(
            id = service.requiredString("id"),
            name = service.requiredString("name"),
            description = service.requiredString("description"),
            service = service.requiredString("service"),
            startup = service.requiredString("startup"),
            identity = service.requiredString("identity"),
        )
    }
}
