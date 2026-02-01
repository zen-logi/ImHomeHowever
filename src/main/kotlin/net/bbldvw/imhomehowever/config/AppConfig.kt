package net.bbldvw.imhomehowever.config

import io.ktor.server.config.ApplicationConfig

data class AppConfig(
    val discordToken: String,
    val discordPublicKey: String,
    val channelId: String,
    val guildId: String? = null,
    val queueUrl: String,
) {
    constructor() : this(
        discordToken =
            System.getenv("DISCORD_TOKEN")
                ?: throw IllegalStateException("DISCORD_TOKEN not found"),
        discordPublicKey =
            System.getenv("PUBLIC_KEY")
                ?: throw IllegalStateException("PUBLIC_KEY not found"),
        channelId =
            System.getenv("CHANNEL_ID")
                ?: throw IllegalStateException("CHANNEL_ID not found"),
        guildId = System.getenv("GUILD_ID"),
        queueUrl =
            System.getenv("QUEUE_URL")
                ?: throw IllegalStateException("QUEUE_URL not found"),
    )

    constructor(config: ApplicationConfig) : this(
        discordToken =
            config.propertyOrNull("discord.token")?.getString()
                ?: System.getenv("DISCORD_TOKEN")
                ?: throw IllegalStateException("DISCORD_TOKEN not found"),
        discordPublicKey =
            config.propertyOrNull("discord.publicKey")?.getString()
                ?: System.getenv("PUBLIC_KEY")
                ?: throw IllegalStateException("PUBLIC_KEY not found"),
        channelId =
            config.propertyOrNull("discord.channelId")?.getString()
                ?: System.getenv("CHANNEL_ID")
                ?: throw IllegalStateException("CHANNEL_ID not found"),
        guildId =
            config.propertyOrNull("discord.guildId")?.getString()
                ?: System.getenv("GUILD_ID"),
        queueUrl =
            config.propertyOrNull("discord.queueUrl")?.getString()
                ?: System.getenv("QUEUE_URL")
                ?: throw IllegalStateException("QUEUE_URL not found"),
    )
}
