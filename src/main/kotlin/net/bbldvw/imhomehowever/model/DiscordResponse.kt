package net.bbldvw.imhomehowever.model

import kotlinx.serialization.Serializable

@Serializable
data class DiscordResponse(
    val type: Int,
    val data: DiscordResponseData? = null,
)

@Serializable
data class DiscordResponseData(
    val content: String? = null,
)
