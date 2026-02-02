package net.bbldvw.imhomehowever.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.bbldvw.imhomehowever.controllers.DiscordWebhookController
import net.bbldvw.imhomehowever.controllers.SqsWorkerController
import net.bbldvw.imhomehowever.dispatchers.DiscordWebhookDispatcher
import net.bbldvw.imhomehowever.dispatchers.discord.*
import net.bbldvw.imhomehowever.dispatchers.discord.commands.*
import net.bbldvw.imhomehowever.services.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.slf4j.LoggerFactory
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient

private val log = LoggerFactory.getLogger("net.bbldvw.imhomehowever.config.Config")

val appModule =
    module {
        // AppConfig (既に登録されていればそれを使用、なければ環境変数から生成)
        single { getOrNull<AppConfig>() ?: AppConfig() }

        // JDA (パフォーマンスのため遅延初期化)
        single<JDA> {
            val config = get<AppConfig>()
            val token = config.discordToken

            val jda =
                JDABuilder.createLight(token)
                    .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .setEnabledIntents(emptyList())
                    .build()

            // 非同期でコマンド登録（起動をブロックしない）
            @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
            GlobalScope.launch {
                val guildId = config.guildId
                if (!guildId.isNullOrBlank()) {
                    try {
                        jda.awaitReady()
                        val guild = jda.getGuildById(guildId)
                        if (guild != null) {
                            guild.updateCommands().addCommands(
                                net.dv8tion.jda.api.interactions.commands.build.Commands.slash("imhome", "帰宅経路を検索します")
                                    .addOptions(
                                        net.dv8tion.jda.api.interactions.commands.build.OptionData(
                                            net.dv8tion.jda.api.interactions.commands.OptionType.STRING,
                                            "from",
                                            "出発駅（例: 東京）",
                                            true,
                                        ),
                                        net.dv8tion.jda.api.interactions.commands.build.OptionData(
                                            net.dv8tion.jda.api.interactions.commands.OptionType.STRING,
                                            "to",
                                            "到着駅（例: 新宿）",
                                            true,
                                        ),
                                        net.dv8tion.jda.api.interactions.commands.build.OptionData(
                                            net.dv8tion.jda.api.interactions.commands.OptionType.STRING,
                                            "via",
                                            "経由駅（例: 京王線）",
                                            false,
                                        ),
                                    ),
                            ).queue()
                            log.info("Slash command 'imhome' registered for guild $guildId")
                        }
                    } catch (e: Exception) {
                        log.error("Failed to register slash command", e)
                    }
                }
            }
            jda
        }

        // HttpClient
        single {
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
                }
            }
        }

        // AWS SQS
        single {
            SqsClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build()
        }

        // Services & Dispatchers
        singleOf(::DefaultTrainService) bind TrainService::class
        singleOf(::TrainCommand) bind SlashCommand::class
        singleOf(::PingHandler) bind InteractionHandler::class
        single<InteractionHandler> { CommandHandler(getAll<SlashCommand>()) }
        single { DiscordWebhookDispatcher(getAll<InteractionHandler>()) }
        single<QueueService> { SqsQueueService(get(), get<AppConfig>().queueUrl) }
        singleOf(::DefaultDiscordClient) bind DiscordClient::class
        singleOf(::SqsMessageProcessor)
        singleOf(::DiscordWebhookController)
        singleOf(::SqsWorkerController)
    }
