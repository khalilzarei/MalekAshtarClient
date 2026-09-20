package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

data class ChatMessageDto(

    @SerializedName("id") val id: Int? = null,

    @SerializedName("room_id") val roomId: Int? = null,

    @SerializedName("sender_id") val senderId: Int? = null,

    @SerializedName("sender") val sender: ChatMessageSenderDto? = null,

    @SerializedName("message_type") val messageType: String? = null,

    @SerializedName("body") val body: String? = null,

    @SerializedName("media_id") val mediaId: Int? = null,

    @SerializedName("media") val media: Any? = null,

    @SerializedName("is_read") val isRead: Any? = null,

    @SerializedName("read_at") val readAt: String? = null,

    @SerializedName("sent_at") val sentAt: String? = null,

    @SerializedName("created_at") val createdAt: String? = null
)

data class ChatMessageSenderDto(

    @SerializedName("id") val id: Int? = null,

    @SerializedName("full_name") val fullName: String? = null,

    @SerializedName("role") val role: String? = null,

    @SerializedName("status") val status: String? = null,

    @SerializedName("avatar") val avatar: String? = null,

    @SerializedName("avatar_url") val avatarUrl: String? = null
)
