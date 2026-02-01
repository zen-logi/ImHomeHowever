package net.bbldvw.imhomehowever.dispatchers.discord

import kotlinx.serialization.json.JsonElement
import net.bbldvw.imhomehowever.model.DiscordResponse
import net.dv8tion.jda.api.interactions.InteractionType

/**
 * Discord インタラクションを処理するハンドラー
 */
interface InteractionHandler {
    val targetType: InteractionType

    suspend fun handle(json: JsonElement): DiscordResponse
}
