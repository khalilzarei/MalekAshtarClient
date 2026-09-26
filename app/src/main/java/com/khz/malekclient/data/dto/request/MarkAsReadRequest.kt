package com.khz.malekclient.data.dto.request

import com.google.gson.annotations.SerializedName

/**
 * بدنه‌ی POST chat/rooms/{id}/read
 */
data class MarkAsReadRequest(
    @SerializedName("last_read_message_id") val lastReadMessageId: Int
)
