package com.khz.malekashtarclient.ui.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatRoom(
    val id: Long,
    val is_group: Boolean,
    val title: String?,
    val image: String?,
    val users: List<ChatUser> = emptyList(),
    val last_message: ChatMessage? = null,
    val unread_count: Int = 0
)

@Serializable
data class ChatUser(
    val id: Long,
    val full_name: String,
    val avatar: String?,
    val role: String,
    val member_role: String
)

@Serializable
data class ChatMessage(
    val id: Long,
    val chat_room_id: Long,
    val sender_id: Long,
    val message: String,
    val type: String = "text",
    val created_at: String?,
    val sender: ChatUser? = null
)

@Serializable
data class CreatePrivateChatRequest(
    val target_user_id: Long
)

@Serializable
data class CreateGroupChatRequest(
    val is_group: Boolean = true,
    val title: String,
    val user_ids: List<Long>
)

@Serializable
data class SendMessageRequest(
    val message: String,
    val type: String = "text"
)

@Serializable
data class ChatResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)