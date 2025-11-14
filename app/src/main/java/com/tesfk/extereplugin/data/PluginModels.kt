package com.tesfk.extereplugin.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Domain model that represents everything that will be exported into the `.plugin` artifact.
 */
data class PluginBlueprint(
    val name: String = "",
    val packageId: String = "",
    val description: String = "",
    val version: String = "1.0.0",
    val author: String = "",
    val entryPoint: String = DEFAULT_ENTRY_POINT,
    val events: List<PluginEvent> = listOf(
        PluginEvent("message", "text", "Получение текста от пользователя"),
        PluginEvent("command", "slash", "Обработка команд Exteregram")
    ),
    val capabilities: List<PluginCapability> = listOf(
        PluginCapability("network", true),
        PluginCapability("storage", false),
        PluginCapability("ui-overlay", false)
    ),
    val tags: List<String> = listOf("beta")
) {
    fun safeFileName(): String {
        return (packageId.ifBlank { name })
            .lowercase()
            .replace("[^a-z0-9_.-]".toRegex(), "-")
            .ifBlank { "plugin" }
    }

    fun asDraft(): PluginDraft =
        PluginDraft(name, packageId, description, version, author, entryPoint)

    companion object {
        const val DEFAULT_ENTRY_POINT = """def handle(event, context):\n" +
            "    reply = event.get('text', '')\n" +
            "    return {\"status\": \"ok\", \"reply\": reply }\n"
    }
}

data class PluginEvent(
    val channel: String,
    val type: String,
    val description: String
)

data class PluginCapability(
    val name: String,
    val required: Boolean
)

data class PluginDraft(
    val name: String = "",
    val packageId: String = "",
    val description: String = "",
    val version: String = "1.0.0",
    val author: String = "",
    val entryPoint: String = PluginBlueprint.DEFAULT_ENTRY_POINT
)

data class PluginValidationResult(
    val isValid: Boolean,
    val issues: List<String>
)

object PluginValidator {
    fun validate(blueprint: PluginBlueprint): PluginValidationResult {
        val issues = buildList {
            if (blueprint.name.length < 3) add("Название должно быть длиннее 3 символов")
            if (!blueprint.packageId.matches("[a-zA-Z0-9_.-]+".toRegex())) {
                add("Package ID может содержать только латиницу, цифры, точку и дефис")
            }
            if (blueprint.entryPoint.isBlank()) add("Нужен python-код точки входа")
        }
        return PluginValidationResult(issues.isEmpty(), issues)
    }
}

object PluginSerializer {
    fun serialize(blueprint: PluginBlueprint): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
            .format(Date())
        val metadata = buildString {
            appendLine("name: \"${blueprint.name}\"")
            appendLine("package: \"${blueprint.packageId}\"")
            appendLine("version: \"${blueprint.version}\"")
            appendLine("author: \"${blueprint.author}\"")
            appendLine("description: \"" + blueprint.description.replace("\"", "\\\"") + "\"")
            appendLine("generated_at: $timestamp")
            appendLine("tags: [${blueprint.tags.joinToString { "\"$it\"" }}]")
            appendLine("capabilities:")
            blueprint.capabilities.forEach {
                appendLine("  - name: ${it.name}\n    required: ${it.required}")
            }
            appendLine("events:")
            blueprint.events.forEach {
                appendLine("  - channel: ${it.channel}")
                appendLine("    type: ${it.type}")
                appendLine("    description: \"${it.description}\"")
            }
            appendLine("---")
            appendLine("# python entry point")
            appendLine(blueprint.entryPoint.trim())
        }
        return metadata.trimEnd() + "\n"
    }
}
