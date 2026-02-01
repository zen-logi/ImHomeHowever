package net.bbldvw.imhomehowever.dispatchers.discord.commands

import kotlinx.serialization.json.JsonElement
import net.bbldvw.imhomehowever.model.DiscordResponse

/**
 * Discord Slashコマンド
 */
interface SlashCommand {
    /**
     * コマンド名
     */
    val commandName: String

    /**
     * コマンドの実行
     */
    suspend fun execute(json: JsonElement): DiscordResponse
}
