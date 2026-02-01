package net.bbldvw.imhomehowever.dispatchers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.bbldvw.imhomehowever.dispatchers.discord.InteractionHandler
import net.dv8tion.jda.api.interactions.InteractionType
import org.slf4j.LoggerFactory

class DiscordWebhookDispatcher(
    private val handlers: List<InteractionHandler>,
) {
    private val log = LoggerFactory.getLogger(DiscordWebhookDispatcher::class.java)
    private val jsonParser = Json { ignoreUnknownKeys = true }

    suspend fun dispatch(json: String): String {
        val root = jsonParser.parseToJsonElement(json)
        val typeInt =
            root.jsonObject["type"]?.jsonPrimitive?.intOrNull ?: run {
                log.error("Invalid JSON format")
                return """{"error": "Invalid JSON format"}"""
            }
        val type =
            InteractionType.fromKey(typeInt) ?: run {
                log.error("Unknown interaction type: $typeInt")
                return """{"error": "Unknown interaction type"}"""
            }

        log.debug("Dispatching interaction type: {}", type)
        val handler =
            handlers.find { it.targetType == type }
                ?: run {
                    log.error("No handler for interaction type $typeInt")
                    return """{"error": "No handler for interaction type $typeInt"}"""
                }

        val response = handler.handle(root)
        log.debug("Handler response: {}", response)
        return jsonParser.encodeToString(response)
    }
}
