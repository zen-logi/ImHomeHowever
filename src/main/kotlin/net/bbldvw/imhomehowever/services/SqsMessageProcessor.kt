package net.bbldvw.imhomehowever.services

import kotlinx.serialization.json.*
import net.bbldvw.imhomehowever.dispatchers.DiscordWebhookDispatcher
import org.slf4j.LoggerFactory

/**
 * SQS メッセージを処理するサービス
 *
 * @property dispatcher Discord インタラクションのディスパッチャー
 * @property discordClient Discord への通信クライアント
 */
class SqsMessageProcessor(
    private val dispatcher: DiscordWebhookDispatcher,
    private val discordClient: DiscordClient,
) {
    private val log = LoggerFactory.getLogger(SqsMessageProcessor::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * SQS イベントをパースし、各レコードを処理
     *
     * @param sqsEventBody SQS イベントの JSON 文字列
     */
    suspend fun processSqsEvent(sqsEventBody: String) {
        log.debug("Processing SQS event: ${sqsEventBody.take(500)}")

        val sqsEvent =
            try {
                json.parseToJsonElement(sqsEventBody).jsonObject
            } catch (e: Exception) {
                log.error("Failed to parse SQS event: ${e.message}")
                return
            }

        val records = sqsEvent["Records"]?.jsonArray ?: return
        log.debug("Processing ${records.size} records")

        records.forEach { record ->
            val interactionBody = record.jsonObject["body"]?.jsonPrimitive?.content ?: ""
            if (interactionBody.isNotEmpty()) {
                processInteraction(interactionBody)
            }
        }
    }

    /**
     * Discord インタラクションを処理し、結果をフォローアップ送信
     */
    private suspend fun processInteraction(interactionBody: String) {
        val payload =
            try {
                json.parseToJsonElement(interactionBody).jsonObject
            } catch (e: Exception) {
                log.error("Failed to parse interaction body: ${e.message}")
                return
            }

        val applicationId = payload["application_id"]?.jsonPrimitive?.content
        val token = payload["token"]?.jsonPrimitive?.content

        log.debug("applicationId=$applicationId, token=${token?.take(20)}...")

        if (applicationId == null || token == null) {
            log.error("Missing applicationId or token")
            return
        }

        try {
            val result = dispatcher.dispatch(interactionBody)
            log.debug("Dispatch result: $result")

            val resultJson = json.parseToJsonElement(result).jsonObject
            val content =
                resultJson["data"]?.jsonObject?.get("content")?.jsonPrimitive?.content
                    ?: "結果の取得に失敗しました"

            log.debug("Sending follow-up: $content")
            discordClient.sendFollowUp(applicationId, token, content)
            log.info("Follow-up sent successfully")
        } catch (e: Exception) {
            log.error("Exception processing interaction: ${e.message}", e)
            try {
                discordClient.sendFollowUp(applicationId, token, "❌ エラーが発生しました: ${e.message}")
            } catch (e2: Exception) {
                log.error("Failed to send error follow-up: ${e2.message}")
            }
        }
    }
}
