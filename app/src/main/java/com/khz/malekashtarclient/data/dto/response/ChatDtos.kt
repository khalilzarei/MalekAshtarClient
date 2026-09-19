package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

/* ═══════════════ لیست مخاطبین چت ═══════════════ */

/**
 * پاسخ GET me/chat-contacts → data.contacts[]
 *
 * شامل ادمین‌های فعال + مربیان اصلی/کمکی کلاس‌های فعال بازیکن
 */
data class ChatContactDto(
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("role") val role: String? = null,                   // admin | coach
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("class_title") val classTitle: String? = null
)

/* ═══════════════ اتاق چت ═══════════════ */

/**
 * GET chat/rooms → data.rooms[]
 *
 * شامل:
 *  - اتاق‌های دو نفره (room_type: player_admin | player_coach | coach_admin)
 *  - اتاق‌های گروهی گروه‌های سنی (room_type: age_group) — بازیکن عضو خودکار
 */
data class ChatRoomDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("room_type") val roomType: String? = null,
    @SerializedName("target_user_id") val targetUserId: Int? = null,
    @SerializedName("target_user_name") val targetUserName: String? = null,
    @SerializedName("target_user_role") val targetUserRole: String? = null,
    @SerializedName("target_user_avatar") val targetUserAvatar: String? = null,
    @SerializedName("player_id") val playerId: Int? = null,
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("class_title") val classTitle: String? = null,
    @SerializedName("age_group_id") val ageGroupId: Int? = null,
    @SerializedName("age_group_title") val ageGroupTitle: String? = null,
    @SerializedName("is_locked") val isLocked: Boolean? = null,
    @SerializedName("member_count") val memberCount: Int? = null,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("last_message") val lastMessage: String? = null,
    @SerializedName("last_message_at") val lastMessageAt: String? = null,
    @SerializedName("unread_count") val unreadCount: Int? = null,
    @SerializedName("members") val members: List<ChatRoomMemberDto> = emptyList()
)

/** آیتم عضو اتاق */
data class ChatRoomMemberDto(
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

/* ═══════════════ پیام چت ═══════════════ */

/**
 * GET chat/rooms/{id}/messages → data.messages[]
 *
 * created_at معمولاً "YYYY-MM-DD HH:MM:SS" است — در UI فقط ۵ کاراکتر آخر برای ساعت
 */
data class ChatMessageDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("room_id") val roomId: Int? = null,
    @SerializedName("sender_id") val senderId: Int? = null,
    @SerializedName("sender") val sender: ChatMessageSenderDto? = null,
    @SerializedName("message_type") val messageType: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("is_read") val isRead: Boolean? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

/** اطلاعات فرستنده‌ی پیام */
data class ChatMessageSenderDto(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

/* ═══════════════ پاکت‌های چت ═══════════════ */

/** GET chat/rooms → data.rooms[] */
data class ChatRoomListWrapperDto(
    @SerializedName("rooms") val rooms: List<ChatRoomDto> = emptyList()
)

/** POST chat/rooms → data.room */
data class CreateRoomWrapperDto(
    @SerializedName("room") val room: ChatRoomDto? = null
)

/** GET chat/rooms/{id}/messages → data.messages[] */
data class MessagesWrapperDto(
    @SerializedName("messages") val messages: List<ChatMessageDto> = emptyList()
)
