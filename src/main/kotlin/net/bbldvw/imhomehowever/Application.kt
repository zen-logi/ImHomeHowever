package net.bbldvw.imhomehowever

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.bbldvw.imhomehowever.config.appModule
import net.bbldvw.imhomehowever.controllers.DiscordWebhookController
import net.bbldvw.imhomehowever.controllers.SqsWorkerController
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

/**
 * アプリケーションのエントリーポイント
 *
 * 環境変数 PORT からポート番号を読み取り、指定がなければ 8080 を使用
 * AWS Lambda Web Adapter との互換性のため、動的にポート設定可能
 */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/**
 * Ktorのモジュール定義
 */
fun Application.module() {
    val log = environment.log
    log.debug("Starting Application.module()")

    // Koin DI 設定
    val appConfig = net.bbldvw.imhomehowever.config.AppConfig(environment.config)
    install(Koin) {
        val configModule =
            module {
                single { appConfig }
                single { environment.config }
            }
        modules(appModule, configModule)
    }
    log.debug("Koin installed")

    // ログ出力設定
    install(CallLogging)

    // エラーハンドリング設定
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception: ", cause)
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error: ${cause.message}")
        }
    }

    // JSON シリアライゼーション
    install(ContentNegotiation) {
        json()
    }

    // ルーティング
    routing {
        // ヘルスチェック
        get("/") {
            call.respondText("Hello, Discord Bot is running!")
        }
        get("/ping") {
            call.respondText("pong")
        }
        post("/api/test") {
            call.respondText("OK")
        }

        // SQS Worker Controller
        val sqsWorkerController by application.inject<SqsWorkerController>()
        with(sqsWorkerController) {
            registerRoutes()
        }

        // Discord Webhook Controller
        val discordController by application.inject<DiscordWebhookController>()
        with(discordController) {
            registerRoutes()
        }
    }
}
