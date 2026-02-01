package net.bbldvw.imhomehowever.services

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory

/**
 * Ktor HttpClient を使用した Discord 通信のデフォルト実装
 *
 * @property httpClient Ktor クライアント
 */
class DefaultDiscordClient(
    private val httpClient: HttpClient,
) : DiscordClient {
    private val log = LoggerFactory.getLogger(DefaultDiscordClient::class.java)

    override suspend fun sendFollowUp(
        applicationId: String,
        token: String,
        content: String,
    ) {
        val url = "https://discord.com/api/v10/webhooks/$applicationId/$token/messages/@original"

        log.debug("Sending follow-up to Discord: $url")
        httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("content", content)
                }.toString(),
            )
        }
        log.info("Follow-up sent to Discord: ${content.take(50)}...")
    }
}
