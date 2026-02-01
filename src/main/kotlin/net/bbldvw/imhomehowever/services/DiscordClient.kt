package net.bbldvw.imhomehowever.services

/**
 * Discord API との通信を担当するインターフェース
 */
interface DiscordClient {
    /**
     * フォローアップメッセージを送信する
     *
     * @param applicationId Discord アプリケーション ID
     * @param token インタラクショントークン
     * @param content 送信するメッセージ内容
     */
    suspend fun sendFollowUp(
        applicationId: String,
        token: String,
        content: String,
    )
}
