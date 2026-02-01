package net.bbldvw.imhomehowever.controllers

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.json.*
import net.bbldvw.imhomehowever.config.AppConfig
import net.bbldvw.imhomehowever.dispatchers.DiscordWebhookDispatcher
import org.slf4j.LoggerFactory
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.HexFormat

/**
 * Discord Webhookコントローラー
 */
class DiscordWebhookController(
    private val dispatcher: DiscordWebhookDispatcher,
    private val queueService: net.bbldvw.imhomehowever.services.QueueService,
    private val config: AppConfig,
) {
    private val log = LoggerFactory.getLogger(DiscordWebhookController::class.java)
    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    fun Route.registerRoutes() {
        post("/api/discord/webhook") {
            log.debug("Webhook received")
            val signature = call.request.headers["X-Signature-Ed25519"] ?: ""
            val timestamp = call.request.headers["X-Signature-Timestamp"] ?: ""
            val body = call.receiveText()

            if (!verifySignature(body, signature, timestamp, config.discordPublicKey)) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid signature")
                return@post
            }

            // 1. JSON をパースして型を確認
            val root =
                try {
                    json.parseToJsonElement(body).jsonObject
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid JSON")
                    return@post
                }

            val type = root["type"]?.jsonPrimitive?.intOrNull ?: 0

            // 2. インタラクションの種類に応じた処理
            if (type == 2) { // APPLICATION_COMMAND
                // SQS に投げて即座に Type 5 (Deferred Response) を返す
                queueService.sendMessage(body)
                call.respondText("""{"type": 5}""", ContentType.Application.Json)
            } else {
                // PING 等は Dispatcher で即時応答
                val response = dispatcher.dispatch(body)
                call.respondText(response, ContentType.Application.Json)
            }
        }
    }

    /**
     * Discord Webhookの署名を検証する
     */
    private fun verifySignature(
        body: String,
        signature: String,
        timestamp: String,
        publicKeyHex: String,
    ): Boolean {
        try {
            if (signature.isEmpty() || timestamp.isEmpty() || publicKeyHex.isEmpty()) {
                return false
            }

            val format = HexFormat.of()

            val header = format.parseHex("302a300506032b6570032100")
            val rawKey = format.parseHex(publicKeyHex)
            val x509Key = header + rawKey

            val keySpec = X509EncodedKeySpec(x509Key)
            val keyFactory = KeyFactory.getInstance("Ed25519")
            val publicKey = keyFactory.generatePublic(keySpec)

            val sig = Signature.getInstance("Ed25519")
            sig.initVerify(publicKey)
            // Discordの仕様: timestamp + body を検証する
            sig.update((timestamp + body).toByteArray(Charsets.UTF_8))

            return sig.verify(format.parseHex(signature))
        } catch (e: Exception) {
            log.error("Signature verification failed", e)
            return false
        }
    }
}
