package net.bbldvw.imhomehowever.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

/**
 * AWS SQS を使用したキューサービスの実装
 *
 * @property sqsClient SQS クライアント
 * @property queueUrl 送信先のキュー URL
 */
class SqsQueueService(
    private val sqsClient: SqsClient,
    private val queueUrl: String,
) : QueueService {
    private val log = LoggerFactory.getLogger(SqsQueueService::class.java)

    override suspend fun sendMessage(message: String) {
        withContext(Dispatchers.IO) {
            log.debug("Sending message to SQS: ${message.take(100)}...")
            val request =
                SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message)
                    .build()

            sqsClient.sendMessage(request)
            log.info("Message sent to SQS successfully")
        }
    }
}
