package net.bbldvw.imhomehowever.dispatchers.discord

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.bbldvw.imhomehowever.dispatchers.discord.commands.SlashCommand
import net.bbldvw.imhomehowever.model.DiscordResponse
import net.bbldvw.imhomehowever.model.DiscordResponseData
import net.dv8tion.jda.api.interactions.InteractionType

class CommandHandler(private val commands: List<SlashCommand>) : InteractionHandler {
    override val targetType = InteractionType.COMMAND

    override suspend fun handle(json: JsonElement): DiscordResponse {
        // 1. コマンド名を取り出す
        val commandName =
            json.jsonObject["data"]?.jsonObject?.get("name")?.jsonPrimitive?.content
                ?: return DiscordResponse(type = 4, data = DiscordResponseData(content = "Error: Command name missing"))

        // 2. 対応するコマンドを探して実行
        val command =
            commands.find { it.commandName == commandName }
                ?: return DiscordResponse(type = 4, data = DiscordResponseData(content = "Error: Command not found"))

        return command.execute(json)
    }
}
