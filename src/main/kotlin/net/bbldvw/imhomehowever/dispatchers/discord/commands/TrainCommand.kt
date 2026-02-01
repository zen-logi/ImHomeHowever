package net.bbldvw.imhomehowever.dispatchers.discord.commands

import kotlinx.serialization.json.*
import net.bbldvw.imhomehowever.model.DiscordResponse
import net.bbldvw.imhomehowever.model.DiscordResponseData
import net.bbldvw.imhomehowever.services.TrainService
import org.slf4j.LoggerFactory

class TrainCommand(
    private val trainService: TrainService,
) : SlashCommand {
    private val log = LoggerFactory.getLogger(TrainCommand::class.java)
    override val commandName = "imhome"

    override suspend fun execute(json: JsonElement): DiscordResponse {
        val root = json.jsonObject
        val options = root["data"]?.jsonObject?.get("options")?.jsonArray

        // 1つ目の引数: 乗る駅
        val fromStation =
            options?.find { it.jsonObject["name"]?.jsonPrimitive?.content == "from" }
                ?.jsonObject?.get("value")?.jsonPrimitive?.content ?: "東京"

        // 2つ目の引数: 降りる駅
        val toStation =
            options?.find { it.jsonObject["name"]?.jsonPrimitive?.content == "to" }
                ?.jsonObject?.get("value")?.jsonPrimitive?.content ?: "大阪"

        val viaStation =
            options?.find {
                it.jsonObject["name"]?.jsonPrimitive?.content == "via"
            }?.jsonObject?.get("value")?.jsonPrimitive?.content

        log.info("Searching route: $fromStation -> $toStation (via: $viaStation)")

        // 同期的に検索を実行
        val resultText =
            try {
                trainService.searchRoute(fromStation, toStation, viaStation)
            } catch (e: Exception) {
                log.error("Route search failed", e)
                "検索中にエラーが発生しました: ${e.message}"
            }

        log.debug("Search result: ${resultText.take(100)}...")
        return DiscordResponse(
            type = 4,
            data = DiscordResponseData(content = resultText),
        )
    }
}
