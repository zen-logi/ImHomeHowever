package net.bbldvw.imhomehowever.services

/**
 * メッセージキューへの操作を担当するインターフェース
 */
interface QueueService {
    /**
     * メッセージをキューに送信する
     *
     * @param message 送信するメッセージ内容
     */
    suspend fun sendMessage(message: String)
}
