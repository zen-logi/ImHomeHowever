package net.bbldvw.imhomehowever.controllers

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.bbldvw.imhomehowever.services.SqsMessageProcessor
import org.slf4j.LoggerFactory

/**
 * SQS Worker 用のコントローラー
 *
 * Lambda Web Adapter 経由で呼び出される /events エンドポイントを管理
 *
 * @property sqsMessageProcessor SQS メッセージ処理サービス
 */
class SqsWorkerController(
    private val sqsMessageProcessor: SqsMessageProcessor,
) {
    private val log = LoggerFactory.getLogger(SqsWorkerController::class.java)

    /**
     * ルートを登録
     */
    fun Route.registerRoutes() {
        // Lambda Web Adapter は /events に POST する
        post("/events") {
            handleSqsEvent()
        }

        // 互換性のため別パスもサポート
        post("/api/worker/sqs") {
            handleSqsEvent()
        }
    }

    /**
     * SQS イベントを処理
     */
    private suspend fun RoutingContext.handleSqsEvent() {
        val body = call.receiveText()
        log.debug("Received SQS event")

        sqsMessageProcessor.processSqsEvent(body)

        call.respond(HttpStatusCode.OK, "SQS Processed")
    }
}
