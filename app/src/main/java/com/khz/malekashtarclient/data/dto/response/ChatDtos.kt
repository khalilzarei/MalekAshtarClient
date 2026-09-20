package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

/* ═══════════════ لیست مخاطبین چت ═══════════════ */

/**
 * پاسخ GET me/chat-contacts → data.contacts[]
 *
 * شامل ادمین‌های فعال + مربیان اصلی/کمکی کلاس‌های فعال بازیکن
 *
 * توجه: avatar_url همیشه پر است (AvatarService::getAvatarUrl با fallback به پیش‌فرض)
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
 * یا POST chat/rooms → data.room
 *
 * فیلدها دقیقاً مطابق ChatService::hydrateRoom در سمت سرور هستند.
 *
 * room_type یکی از:
 *  - player_admin  (بازیکن ↔ ادمین)
 *  - player_coach  (بازیکن ↔ مربی)
 *  - coach_admin   (ادمین ↔ مربی — بازیکن ندارد)
 *  - age_group     (گفتگوی گروهی گروه سنی)
 *
 * نکات کلیدی:
 *  - last_message یک شیء کامل است (نه string) — در mapper به ChatMessageDto تبدیل می‌شود
 *  - is_locked در سرور int 0/1 است → در mapper به Boolean تبدیل می‌شود
 *  - target_user_avatar، class_title، last_message_at توسط سرور فرستاده نمی‌شوند
 *    ولی برای سازگاری آینده نگه داشته شده‌اند (همیشه null)
 */
data class ChatRoomDto(
    // ── فیلدهای اصلی (سرور حتماً می‌فرستد) ──
    @SerializedName("id") val id: Int? = null,
    @SerializedName("room_type") val roomType: String? = null,
    @SerializedName("target_user_id") val targetUserId: Int? = null,
    @SerializedName("target_user_name") val targetUserName: String? = null,
    @SerializedName("target_user_role") val targetUserRole: String? = null,
    @SerializedName("age_group_id") val ageGroupId: Int? = null,
    @SerializedName("age_group_title") val ageGroupTitle: String? = null,
    @SerializedName("member_count") val memberCount: Int? = null,
    @SerializedName("unread_count") val unreadCount: Int? = null,
    @SerializedName("members") val members: List<ChatRoomMemberDto> = emptyList(),

    // ── فیلدهای اختیاری/nullable ──
    @SerializedName("player_id") val playerId: Int? = null,
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("status") val status: String? = null,               // 'active'
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,

    // ── فیلدهای ترکیبی/پیچیده ──
    @SerializedName("is_locked") val isLocked: Any? = null,             // int 0/1 یا bool
    @SerializedName("last_message") val lastMessage: Any? = null,       // شیء کامل ChatMessage

    // ── فیلدهای آینده‌نگر (سرور فعلاً نمی‌فرستد) ──
    @SerializedName("target_user_avatar") val targetUserAvatar: String? = null,
    @SerializedName("class_title") val classTitle: String? = null,
    @SerializedName("last_message_at") val lastMessageAt: String? = null
)

/** آیتم عضو اتاق — دقیقاً مطابق ChatService::hydrateRoom */
data class ChatRoomMemberDto(
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("role") val role: String? = null,                   // user_role از join با football_users
    @SerializedName("member_role") val memberRole: String? = null,      // owner | member
    @SerializedName("last_read_message_id") val lastReadMessageId: Int? = null
    // توجه: avatar_url در hydrateRoom فرستاده نمی‌شود (فقط در contacts)
)

/* ═══════════════ پیام چت ═══════════════ */

/**
 * GET chat/rooms/{id}/messages → data.messages[]
 *
 * طبق ChatRepository::hydrateMessage:
 *  - id, room_id, sender_id
 *  - sender { id, full_name, role, status }  ← avatar_url ندارد
 *  - message_type (text|image|video|file|system)
 *  - body, media_id, media (null)
 *  - is_read (bool محاسبه‌شده از lastReadMessageId)، read_at (null)
 *  - sent_at, created_at
 */
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

/**
 * اطلاعات فرستنده‌ی پیام — طبق ChatRepository::hydrateMessage
 *
 * فیلدها: id, full_name, role, status
 * (avatar_url ندارد — آواتار فرستنده از contacts می‌آید)
 */
data class ChatMessageSenderDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("status") val status: String? = null
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

/** POST chat/rooms/{id}/messages → data.message */
data class SendMessageWrapperDto(
    @SerializedName("message") val message: ChatMessageDto? = null
)
