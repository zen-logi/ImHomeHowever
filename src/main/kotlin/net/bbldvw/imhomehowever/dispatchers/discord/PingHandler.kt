package net.bbldvw.imhomehowever.dispatchers.discord

import kotlinx.serialization.json.JsonElement
import net.bbldvw.imhomehowever.model.DiscordResponse
import net.dv8tion.jda.api.interactions.InteractionType

/**
 * PING リクエストを処理するハンドラー
 */
class PingHandler : InteractionHandler {
    override val targetType = InteractionType.PING

    override suspend fun handle(json: JsonElement): DiscordResponse {
        // Discord PING には {"type": 1} で応答する
        return DiscordResponse(type = 1)
    }
}
