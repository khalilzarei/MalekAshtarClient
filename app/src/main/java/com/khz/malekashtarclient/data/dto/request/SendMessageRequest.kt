package com.khz.malekashtarclient.data.dto.request

import com.google.gson.annotations.SerializedName
import com.khz.malekashtarclient.core.util.Constants

/**
 * بدنه‌ی POST chat/rooms/{id}/messages
 *
 * در این اپ فقط پیام متنی ارسال می‌شود.
 */
data class SendMessageRequest(
    @SerializedName("message_type") val messageType: String = Constants.MESSAGE_TYPE_TEXT,
    @SerializedName("body") val body: String
)
